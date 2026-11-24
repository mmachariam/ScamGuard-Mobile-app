"""Dependency-free inference for Python 3.13, matching the exported pipelines.

No scikit-learn installation is needed to serve this JSON model. This supports
only this export's word TF-IDF (raw TF, smoothed IDF, L2), binary LR and NB.
It is deliberately separate from the running FastAPI app's response schema.
"""
import argparse
from collections import Counter
import json
import math
from pathlib import Path
import re

def sigmoid(value):
    if value >= 0:
        return 1 / (1 + math.exp(-value))
    e = math.exp(value)
    return e / (1 + e)

class ScamGuardModels:
    def __init__(self, path=None):
        path = Path(path) if path else Path(__file__).with_name("models") / "portable_models.json"
        self.data = json.loads(path.read_text(encoding="utf-8"))
        if self.data["format"] != "scamguard-word-tfidf-binary-v1":
            raise ValueError("Unsupported model export.")
        self.tokenizer = re.compile(self.data["token_pattern"])
        self.vocabulary = self.data["vocabulary"]
        self.names = {index: term for term, index in self.vocabulary.items()}

    def _features(self, text):
        tokens = self.tokenizer.findall(text.lower())
        terms = tokens + [a + " " + b for a, b in zip(tokens, tokens[1:])]
        counts = Counter(self.vocabulary[t] for t in terms if t in self.vocabulary)
        values = {i: count * self.data["idf"][i] for i, count in counts.items()}
        norm = math.sqrt(sum(value * value for value in values.values()))
        return {i: value / norm for i, value in values.items()} if norm else {}

    def predict(self, message, agreement=False):
        if not isinstance(message, str) or not message.strip():
            raise ValueError("Enter a non-empty message.")
        if len(message) > 2000:
            raise ValueError("Messages must be 2,000 characters or fewer.")
        x = self._features(" ".join(message.split()))
        members = {}
        for name in ["logistic_regression", "naive_bayes"]:
            params = self.data[name]
            if not x:
                members[name] = dict(riskLevel="UNCERTAIN", reason="No known vocabulary", signals=[])
                continue
            coef = params["coefficients"]
            score = params["intercept"] + sum(value * coef[i] for i, value in x.items())
            positive = score > 0
            contributions = [(value * coef[i], self.names[i]) for i, value in x.items()]
            contributions = sorted(contributions, reverse=positive)
            signals = [dict(phrase=phrase, contribution=value) for value, phrase in contributions
                       if (value > 0 if positive else value < 0)][:5]
            members[name] = dict(riskLevel="HIGH" if positive else "LOW",
                                 positiveClassScore=sigmoid(score), signals=signals)
        risk = members["logistic_regression"]["riskLevel"]
        if agreement and members["naive_bayes"]["riskLevel"] != risk:
            risk = "UNCERTAIN"
        return dict(riskLevel=risk, policy="agreement" if agreement else "logistic_regression",
                    isDemo=False, scoreIsCalibrated=False, models=members,
                    notice="Experimental spam/phishing classifier. Predictions can be wrong; verify independently.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("message"); parser.add_argument("--models", default=None)
    parser.add_argument("--agreement", action="store_true")
    args = parser.parse_args()
    print(json.dumps(ScamGuardModels(args.models).predict(args.message, args.agreement), indent=2))
