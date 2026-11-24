package com.example.scamguard.data.analysis

import com.example.scamguard.data.feedback.ReportReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object FeedbackApi {
    private const val FEEDBACK_URL = "http://10.0.2.2:8000/feedback"

    suspend fun submitHelpful(
        submissionId: String,
        riskLevel: RiskLevel,
        helpful: Boolean
    ) = submit(
        submissionId,
        JSONObject()
            .put("kind", "helpful")
            .put("submission_id", submissionId)
            .put("risk_level", riskLevel.name)
            .put("helpful", helpful)
    )

    suspend fun submitReport(
        submissionId: String,
        riskLevel: RiskLevel,
        reason: ReportReason
    ) = submit(
        submissionId,
        JSONObject()
            .put("kind", "report")
            .put("submission_id", submissionId)
            .put("risk_level", riskLevel.name)
            .put("reason", reason.apiValue)
    )

    private suspend fun submit(submissionId: String, payload: JSONObject) = withContext(Dispatchers.IO) {
        val connection = URL(FEEDBACK_URL).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            connection.outputStream.use { stream ->
                stream.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode !in 200..299) {
                throw IOException("The server did not accept the feedback.")
            }

            // Confirm that the server saved this particular submission.
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val acknowledgement = JSONObject(response)
            if (
                acknowledgement.optString("status") != "saved" ||
                acknowledgement.optString("submission_id") != submissionId
            ) {
                throw IOException("The server did not confirm the save.")
            }
        } finally {
            connection.disconnect()
        }
    }
}
