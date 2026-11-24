import os
import sqlite3
from contextlib import closing
from pathlib import Path
from typing import Annotated, Literal
from uuid import UUID

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field, StrictBool

DB_PATH = Path(
    os.environ.get("SCAMGUARD_FEEDBACK_DB_PATH")
    or Path(__file__).with_name("scamguard_feedback.db")
)
router = APIRouter()

RiskLevel = Literal["HIGH", "LOW", "UNCERTAIN"]
ReportReason = Literal["wrong_risk_level", "unclear_explanation", "other"]


class HelpfulFeedback(BaseModel):
    kind: Literal["helpful"]
    submission_id: UUID
    risk_level: RiskLevel
    helpful: StrictBool


class IncorrectResultReport(BaseModel):
    kind: Literal["report"]
    submission_id: UUID
    risk_level: RiskLevel
    reason: ReportReason


FeedbackSubmission = Annotated[
    HelpfulFeedback | IncorrectResultReport,
    Field(discriminator="kind"),
]


def init_feedback_db() -> None:
    # Create the table on first startup and preserve existing submissions.
    with closing(sqlite3.connect(DB_PATH)) as db, db:
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS feedback (
                submission_id TEXT PRIMARY KEY NOT NULL,
                kind TEXT NOT NULL,
                risk_level TEXT NOT NULL,
                helpful INTEGER,
                reason TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )


@router.post("/feedback", status_code=201)
def save_feedback(submission: FeedbackSubmission):
    helpful = None
    reason = None

    if isinstance(submission, HelpfulFeedback):
        helpful = int(submission.helpful)
    else:
        reason = submission.reason

    submission_id = str(submission.submission_id)
    details = (submission.kind, submission.risk_level, helpful, reason)

    # Give each request its own connection and commit before confirming success.
    with closing(sqlite3.connect(DB_PATH)) as db, db:
        db.execute(
            """
            INSERT INTO feedback (
                submission_id, kind, risk_level, helpful, reason
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(submission_id) DO NOTHING
            """,
            (submission_id, *details),
        )

        # A retry with the same ID and data should create only one row.
        stored = db.execute(
            """
            SELECT kind, risk_level, helpful, reason
            FROM feedback
            WHERE submission_id = ?
            """,
            (submission_id,),
        ).fetchone()

        if stored != details:
            raise HTTPException(
                status_code=409,
                detail="This submission ID was already used for different feedback.",
            )

    return {"status": "saved", "submission_id": submission_id}
