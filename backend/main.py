import re
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Literal

from fastapi import FastAPI, Request
from pydantic import BaseModel, Field, StrictStr, field_validator

from portable_predictor import ScamGuardModels


# Load the trained models from the folder beside this file.
MODEL_FILE = Path(__file__).parent / "models" / "portable_models.json"
RiskLevel = Literal["HIGH", "LOW", "UNCERTAIN"]


class AnalyzeRequest(BaseModel):
    message: StrictStr = Field(min_length=1, max_length=2000)

    @field_validator("message")
    @classmethod
    def reject_blank_message(cls, value: str) -> str:
        # Reject whitespace-only input and preserve the original for display.
        if not value.strip():
            raise ValueError("Enter a message containing text.")
        return value


class WarningSign(BaseModel):
    phrase: str
    explanation: str


class AnalyzeResponse(BaseModel):
    # These field names match the Android app's response format.
    originalMessage: str
    riskLevel: RiskLevel
    summary: str
    warningSigns: list[WarningSign]
    recommendedActions: list[str]
    isDemo: bool = False


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load the model bundle once at startup and reuse it for each request.
    if not MODEL_FILE.is_file():
        raise RuntimeError(
            f"Model file not found at {MODEL_FILE}. "
            "Place portable_models.json inside the models folder."
        )

    app.state.models = ScamGuardModels(MODEL_FILE)
    yield


app = FastAPI(title="ScamGuard API", lifespan=lifespan)


def explain_phrase(phrase: str, risk: RiskLevel) -> str:
    # For a lower-risk result, describe the phrase's role without calling it a warning.
    if risk == "LOW":
        return "This wording contributed to the lower-risk result."

    # Give advice based on the highlighted wording. A keyword alone does
    # not establish what the sender is asking the user to do.
    if re.search(r"\b(?:pin|otp|passwords?|verification codes?)\b", phrase, re.IGNORECASE):
        return (
            "This wording refers to private information. "
            "If the message asks you to share it, keep it private "
            "and verify the request separately."
        )

    if re.search(r"\b(?:refunds?|payments?|fees?|deposits?)\b", phrase, re.IGNORECASE):
        return (
            "This wording relates to money. If a refund or payment was "
            "unexpected, confirm it directly with the organisation."
        )

    return (
        "This wording contributed to the alert. Read it in the full message "
        "to see what the sender is asking you to do."
    )


@app.get("/health")
def health(request: Request):
    # Keep the model identifier available for backend troubleshooting.
    return {
        "status": "ok",
        "model": "logistic-regression-v2",
        "model_loaded": hasattr(request.app.state, "models"),
    }


@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(body: AnalyzeRequest, request: Request):
    prediction = request.app.state.models.predict(body.message)
    risk = prediction["riskLevel"]

    summaries = {
        "HIGH": (
            "This message contains wording that can appear in scams. "
            "Pause and verify the request before acting."
        ),
        "LOW": (
            "No strong warning signs stood out in the text. "
            "If the message was unexpected, check who sent it before acting."
        ),
        "UNCERTAIN": (
            "We couldn’t assess this message. "
            "If you pasted only part of it, try checking the full message."
        ),
    }

    signs: list[WarningSign] = []
    seen: set[str] = set()

    if risk != "UNCERTAIN":
        details = prediction["models"]["logistic_regression"]

        # Keep the predictor's order and quote only wording present in the
        # original message. Allow spaces or line breaks between words.
        for signal in details.get("signals", []):
            words = signal["phrase"].split()
            if not words:
                continue

            pattern = r"(?<!\w)" + r"\s+".join(map(re.escape, words)) + r"(?!\w)"
            match = re.search(pattern, body.message, flags=re.IGNORECASE)
            if match is None:
                continue

            phrase = match.group()
            phrase_key = " ".join(phrase.casefold().split())
            if phrase_key in seen:
                continue

            seen.add(phrase_key)
            signs.append(
                WarningSign(
                    phrase=phrase,
                    explanation=explain_phrase(phrase, risk),
                )
            )

            if len(signs) == 3:
                break

    # Keep the advice short for lower-risk results. Give more concrete
    # precautions when the message is flagged or cannot be assessed.
    actions = [
        "Verify unexpected requests using contact details you already trust."
    ]
    if risk != "LOW":
        actions.extend(
            [
                "Keep PINs, passwords, and verification codes private.",
                "Check unexpected links or payment requests before acting.",
            ]
        )

    return AnalyzeResponse(
        originalMessage=body.message,
        riskLevel=risk,
        summary=summaries[risk],
        warningSigns=signs,
        recommendedActions=actions,
    )
