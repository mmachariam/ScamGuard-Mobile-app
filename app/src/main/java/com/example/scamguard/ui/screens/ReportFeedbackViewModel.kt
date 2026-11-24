package com.example.scamguard.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.analysis.FeedbackApi
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.data.feedback.ReportReason
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReportStatus {
    EDITING,
    SENDING,
    SAVED,
    DEMO_COMPLETE,
    FAILED
}

data class ReportFeedbackUiState(
    val reason: ReportReason? = null,
    val status: ReportStatus = ReportStatus.EDITING,
    val submissionError: String? = null
) {
    val canSubmit: Boolean
        get() = reason != null &&
            (status == ReportStatus.EDITING || status == ReportStatus.FAILED)
}

class ReportFeedbackViewModel : ViewModel() {
    private data class Submission(
        val id: String,
        val riskLevel: RiskLevel,
        val reason: ReportReason
    )

    private val mutableUiState = MutableStateFlow(ReportFeedbackUiState())
    val uiState = mutableUiState.asStateFlow()

    private var pendingSubmission: Submission? = null
    private var submissionJob: Job? = null

    fun selectReason(reason: ReportReason) {
        val current = mutableUiState.value
        if (current.status == ReportStatus.EDITING) {
            mutableUiState.value = current.copy(reason = reason, submissionError = null)
        }
    }

    fun onPermissionDenied() {
        val current = mutableUiState.value
        if (current.canSubmit) {
            mutableUiState.value = current.copy(
                submissionError = "Allow local network access to send the report."
            )
        }
    }

    fun submit(result: AnalysisResult) {
        val current = mutableUiState.value
        if (!current.canSubmit) return
        val reason = current.reason ?: return

        if (result.isDemo) {
            mutableUiState.value = current.copy(status = ReportStatus.DEMO_COMPLETE)
            return
        }

        // The first attempt may have reached SQLite even if its acknowledgement was lost.
        val submission = pendingSubmission ?: Submission(
            id = UUID.randomUUID().toString(),
            riskLevel = result.riskLevel,
            reason = reason
        )
        pendingSubmission = submission
        mutableUiState.value = current.copy(status = ReportStatus.SENDING, submissionError = null)

        submissionJob = viewModelScope.launch {
            try {
                FeedbackApi.submitReport(submission.id, submission.riskLevel, submission.reason)
                pendingSubmission = null
                mutableUiState.value = mutableUiState.value.copy(status = ReportStatus.SAVED)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    status = ReportStatus.FAILED,
                    submissionError = "Couldn’t confirm the report was saved. Please try again."
                )
            }
        }
    }

    fun reset() {
        submissionJob?.cancel()
        submissionJob = null
        pendingSubmission = null
        mutableUiState.value = ReportFeedbackUiState()
    }
}
