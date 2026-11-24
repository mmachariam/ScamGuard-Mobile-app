# ScamGuard API

The FastAPI service loads `models/portable_models.json` on startup and uses a
TF-IDF logistic regression model to analyze message text.

## Run locally

From the repository root, use PowerShell with Python 3.13 installed.

```powershell
Set-Location backend
py -3.13 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m uvicorn main:app --host 127.0.0.1 --port 8000
```

In a second terminal, check that the service reports `status: ok` and
`model_loaded: true`.

```powershell
Invoke-RestMethod http://127.0.0.1:8000/health
```

The Android Studio emulator reaches this server at `http://10.0.2.2:8000`.
Keep the API running while you use the Android app's debug build.

## Feedback storage

Startup creates `scamguard_feedback.db` in `backend/`. Feedback sends only the
submission kind, ID, risk level, and vote or report reason. The API stores
these fields with a server-generated timestamp.

Set `SCAMGUARD_FEEDBACK_DB_PATH` before starting the API to use a different
SQLite file. Point it at a fresh file in an existing temporary directory
when testing feedback.
