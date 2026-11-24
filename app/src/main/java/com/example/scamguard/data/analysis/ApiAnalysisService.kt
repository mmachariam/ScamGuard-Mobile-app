package com.example.scamguard.data.analysis

import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

/**
 * Contains a message suitable for displaying on the input screen.
 * Response bodies are not included in error messages.
 */
class AnalysisRequestException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)

/**
 * Calls the trained-model API running on the development laptop.
 * This address is for the Android Studio emulator.
 */
class ApiAnalysisService : AnalysisService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val endpoints = listOf(
        "http://10.0.2.2:8000/analyze",
        "http://127.0.0.1:8000/analyze"
    )

    override suspend fun analyze(message: String): AnalysisResult {
        require(message.isNotBlank()) {
            "Enter a message before analyzing."
        }

        require(message.length <= 2_000) {
            "Maximum 2,000 characters."
        }

        var lastException: IOException? = null

        for (targetUrl in endpoints) {
            try {
                return executeRequest(targetUrl, message)
            } catch (e: AnalysisRequestException) {
                throw e
            } catch (e: IOException) {
                lastException = e
            }
        }

        val messageForUser = if (lastException is SocketTimeoutException) {
            "Analysis timed out. Please retry."
        } else {
            "Could not reach the analysis service. " +
                    "Check that the Python API is running, then retry."
        }

        throw AnalysisRequestException(messageForUser, lastException)
    }

    private suspend fun executeRequest(targetUrl: String, message: String): AnalysisResult {
        val requestBody = JSONObject()
            .put("message", message)
            .toString()
            .toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )

        val request = Request.Builder()
            .url(targetUrl)
            .post(requestBody)
            .build()

        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!continuation.isActive) return
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!continuation.isActive) return

                        try {
                            val result = parseResponse(response, message)
                            continuation.resume(result)
                        } catch (error: Exception) {
                            if (!continuation.isActive) return

                            val failure = if (error is AnalysisRequestException) {
                                error
                            } else {
                                AnalysisRequestException(
                                    "The analysis service returned an " +
                                            "invalid response. Please retry.",
                                    error
                                )
                            }

                            continuation.resumeWithException(failure)
                        }
                    }
                }
            })
        }
    }

    private fun parseResponse(
        response: Response,
        submittedMessage: String
    ): AnalysisResult {
        if (!response.isSuccessful) {
            val messageForUser = if (response.code == 422) {
                "The service could not accept this message. " +
                        "Check the input and retry."
            } else {
                "The analysis service encountered an error. Please retry."
            }

            throw AnalysisRequestException(messageForUser)
        }

        val responseText = response.body?.string()
            ?: throw AnalysisRequestException(
                "The analysis service returned an empty response."
            )

        val json = JSONObject(responseText)
        val originalMessage = json.getString("originalMessage")

        // A mismatched response must not be displayed or saved.
        require(originalMessage == submittedMessage) {
            "The returned message did not match the request."
        }

        val signsJson = json.getJSONArray("warningSigns")

        val signs = List(signsJson.length()) { index ->
            val item = signsJson.getJSONObject(index)

            WarningSign(
                phrase = item.getString("phrase"),
                explanation = item.getString("explanation")
            )
        }

        val actionsJson = json.getJSONArray("recommendedActions")

        val actions = List(actionsJson.length()) { index ->
            actionsJson.getString(index)
        }

        return AnalysisResult(
            originalMessage = originalMessage,
            riskLevel = RiskLevel.valueOf(json.getString("riskLevel")),
            summary = json.getString("summary"),
            warningSigns = signs,
            recommendedActions = actions,
            isDemo = json.getBoolean("isDemo")
        )
    }
}
