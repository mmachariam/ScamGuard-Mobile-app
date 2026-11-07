package com.example.scamguard.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.feedback.DemoFeedbackService
import com.example.scamguard.data.feedback.FeedbackOutcome
import com.example.scamguard.data.feedback.FeedbackService
import com.example.scamguard.data.feedback.PredictionReport
import com.example.scamguard.data.feedback.ReportChoice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val MAX_REPORT_DETAILS = 500

enum class ReportStatus {
    EDITING,
    SENDING,
    DEMO_COMPLETE,
    FAILED
}

data class ReportFeedbackUiState(
    val choice: ReportChoice? = null,
    val details: String = "",
    val detailsError: String? = null,
    val status: ReportStatus = ReportStatus.EDITING,
    val submissionError: String? = null
) {
    val canSubmit: Boolean
        get() = choice != null &&
                details.length <= MAX_REPORT_DETAILS &&
                status != ReportStatus.SENDING &&
                status != ReportStatus.DEMO_COMPLETE
}

class ReportFeedbackViewModel : ViewModel() {

    // Set simulateFailure to true temporarily to test the failure screen.
    private val feedbackService: FeedbackService =
        DemoFeedbackService(simulateFailure = false)

    private val mutableUiState = MutableStateFlow(ReportFeedbackUiState())
    val uiState = mutableUiState.asStateFlow()

    private var submissionJob: Job? = null

    fun selectChoice(choice: ReportChoice) {
        val current = mutableUiState.value

        if (
            current.status == ReportStatus.SENDING ||
            current.status == ReportStatus.DEMO_COMPLETE
        ) return

        mutableUiState.value = current.copy(
            choice = choice,
            status = ReportStatus.EDITING,
            submissionError = null
        )
    }

    fun updateDetails(value: String) {
        val current = mutableUiState.value

        if (
            current.status == ReportStatus.SENDING ||
            current.status == ReportStatus.DEMO_COMPLETE
        ) return

        if (value.length > MAX_REPORT_DETAILS) {
            mutableUiState.value = current.copy(
                detailsError = "Maximum 500 characters. " +
                        "The extra text was not added."
            )
            return
        }

        mutableUiState.value = current.copy(
            details = value,
            detailsError = null,
            status = ReportStatus.EDITING,
            submissionError = null
        )
    }

    fun submit(result: AnalysisResult) {
        val current = mutableUiState.value
        if (!current.canSubmit) return

        val choice = current.choice ?: return

        // Capture the original verdict without modifying the analysis result.
        val report = PredictionReport(
            originalMessage = result.originalMessage,
            originalPrediction = result.riskLevel,
            predictionWasDemo = result.isDemo,
            userChoice = choice,
            details = current.details
        )

        mutableUiState.value = current.copy(
            status = ReportStatus.SENDING,
            submissionError = null
        )

        submissionJob = viewModelScope.launch {
            try {
                when (feedbackService.submit(report)) {
                    FeedbackOutcome.DEMO_ONLY -> {
                        mutableUiState.value = mutableUiState.value.copy(
                            status = ReportStatus.DEMO_COMPLETE
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                // Cancellation must not become a failed submission.
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    status = ReportStatus.FAILED,
                    submissionError = "The demo could not finish. " +
                            "Nothing was submitted. You can retry."
                )
            }
        }
    }

    fun reset() {
        // Cancel any pending demo work and discard the temporary report.
        submissionJob?.cancel()
        submissionJob = null
        mutableUiState.value = ReportFeedbackUiState()
    }
}