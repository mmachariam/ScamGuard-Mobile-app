package com.example.scamguard.data.feedback

import com.example.scamguard.data.analysis.RiskLevel
import kotlinx.coroutines.delay

enum class ReportChoice(val label: String) {
    LIKELY_SCAM("Likely scam"),
    LIKELY_LEGITIMATE("Likely legitimate"),
    NOT_SURE("Not sure")
}

data class PredictionReport(
    val originalMessage: String,
    val originalPrediction: RiskLevel,
    val predictionWasDemo: Boolean,
    val userChoice: ReportChoice,
    val details: String
)

// A demo acknowledgement is deliberately different from a real submission.
enum class FeedbackOutcome {
    DEMO_ONLY
}

interface FeedbackService {
    suspend fun submit(report: PredictionReport): FeedbackOutcome
}

/**
 * Development-only implementation.
 * It performs no network requests and writes no feedback to storage.
 */
class DemoFeedbackService(
    private val simulateFailure: Boolean = false
) : FeedbackService {

    override suspend fun submit(
        report: PredictionReport
    ): FeedbackOutcome {
        require(report.details.length <= 500) {
            "Details must contain no more than 500 characters."
        }

        // Simulated waiting allows progress and cancellation to be tested.
        delay(1_000)

        if (simulateFailure) {
            throw IllegalStateException("Simulated demo failure")
        }

        return FeedbackOutcome.DEMO_ONLY
    }
}