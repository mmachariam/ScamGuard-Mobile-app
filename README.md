# ScamGuard

ScamGuard is an Android app for checking suspicious messages. Copy text from an
SMS, a chat such as WhatsApp, or an email and paste it manually into the app.
ScamGuard shows a high, low, or uncertain risk assessment, explains highlighted
wording when available, and suggests what to do next.

## How it works

- The Kotlin and Jetpack Compose app in `app/` sends the submitted message text
  to `POST /analyze` on the FastAPI service in `backend/`. The service loads its
  TF-IDF logistic regression model at startup from
  [`backend/models/portable_models.json`](backend/models/portable_models.json).
- The app saves completed checks in an on-device Room database with the original
  message, the analysis result, and the time of the check. Reopen or delete
  individual checks in **History**. Clear all saved checks from **History** or
  **Settings**.
- You can vote on whether a result was helpful or report a problem from new and
  saved API results. The app sends only the submission kind, submission ID, risk
  level, and vote or report reason to `POST /feedback`. By default, the server
  stores those fields with a timestamp in `backend/scamguard_feedback.db`.

## Run locally

### Start the FastAPI backend

From the repository root, run these commands in PowerShell with Python 3.13
installed.

```powershell
Set-Location backend
py -3.13 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m uvicorn main:app --host 127.0.0.1 --port 8000
```

In a second terminal, check that `status` is `ok` and `model_loaded` is `true`.

```powershell
Invoke-RestMethod http://127.0.0.1:8000/health
```

For more on running the API and configuring feedback storage, see
[backend/README.md](backend/README.md).

### Run the Android app

1. Open the repository root in Android Studio. Install Android SDK Platform 37,
   use a full JDK 25 for Gradle, and sync the project.
2. Start an Android Studio emulator running API 24 or later and run the `app`
   module with the `debug` build variant.
3. Keep the backend running on your computer. The app reaches it from the
   emulator at `http://10.0.2.2:8000`. Open **Check a message**, paste text, and
   tap **Analyze message**. Allow local network access if prompted.

## Android unit tests

With the Android SDK configured and `JAVA_HOME` pointing to JDK 25, run from
the repository root in PowerShell.

```powershell
.\gradlew.bat :app:testDebugUnitTest
```
