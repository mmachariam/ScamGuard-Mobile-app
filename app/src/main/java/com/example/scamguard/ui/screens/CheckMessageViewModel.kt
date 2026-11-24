package com.example.scamguard.ui.screens

import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.analysis.AnalysisService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.scamguard.data.local.CheckRepository
import com.example.scamguard.data.local.ScamGuardDatabase
import com.example.scamguard.data.analysis.ApiAnalysisService
import com.example.scamguard.data.analysis.AnalysisRequestException

const val MAX_MESSAGE_LENGTH = 2_000

data class CheckMessageUiState(
    val message: String = "",
    val inputError: String? = null,
    val isAnalyzing: Boolean = false,
    val result: AnalysisResult? = null,
    val analysisError: String? = null,
    val isShowingExplanation: Boolean = false,
    val helpfulFeedback: Boolean? = null,
    val isShowingReport: Boolean = false,
    val isSavingHistory: Boolean = false,
    val savedCheckId: Long? = null,
    val historySaveError: String? = null
) {
    val canAnalyze: Boolean
        get() = message.isNotBlank() &&
                message.length <= MAX_MESSAGE_LENGTH &&
                !isAnalyzing
}

class CheckMessageViewModel(
        application: Application
    ) : AndroidViewModel(application) {

    // Use the trained-model API for new checks.
    private val analysisService: AnalysisService = ApiAnalysisService()

    private val mutableUiState = MutableStateFlow(CheckMessageUiState())
    val uiState = mutableUiState.asStateFlow()

    // Use application context so the database does not retain an Activity.
    private val checkRepository = CheckRepository(
        ScamGuardDatabase.getInstance(application).checkDao()
    )

    private var analysisJob: Job? = null

    fun updateMessage(value: String) {
        if (mutableUiState.value.isAnalyzing) return

        if (value.length > MAX_MESSAGE_LENGTH) {
            showInputError(
                "Maximum 2,000 characters. The extra text was not added."
            )
            return
        }

        // Editing invalidates any previous result.
        mutableUiState.value = CheckMessageUiState(message = value)
    }

    fun pasteMessage(value: String?) {
        when {
            value.isNullOrBlank() -> {
                showInputError(
                    "Your clipboard does not contain message text."
                )
            }

            value.length > MAX_MESSAGE_LENGTH -> {
                showInputError(
                    "Clipboard text exceeds 2,000 characters. " +
                            "Shorten it before pasting."
                )
            }

            else -> updateMessage(value)
        }
    }

    fun clearMessage() {
        if (mutableUiState.value.isAnalyzing) return
        mutableUiState.value = CheckMessageUiState()
    }

    fun showInputError(message: String) {
        mutableUiState.value = mutableUiState.value.copy(
            inputError = message
        )
    }

    fun analyze() {
        val currentState = mutableUiState.value
        if (!currentState.canAnalyze) return

        val submittedMessage = currentState.message

        mutableUiState.value = currentState.copy(
            isAnalyzing = true,
            inputError = null,
            analysisError = null,
            result = null,
            isShowingExplanation = false,
            isShowingReport = false,
            helpfulFeedback = null,
            isSavingHistory = false,
            savedCheckId = null,
            historySaveError = null
        )

        analysisJob = viewModelScope.launch {
            try {
                val result = analysisService.analyze(submittedMessage)

                mutableUiState.value = mutableUiState.value.copy(
                    isAnalyzing = false,
                    result = result
                )

                // Save a completed result once. Opening it later does not save again.
                saveCompletedResult(result)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AnalysisRequestException) {
                mutableUiState.value = mutableUiState.value.copy(
                    isAnalyzing = false,
                    analysisError = error.message
                        ?: "Could not complete analysis. Please retry."
                )
            } catch (_: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    isAnalyzing = false,
                    analysisError = "Could not complete analysis. Please retry."
                )
            }
        }
    }
    private fun saveCompletedResult(result: AnalysisResult) {
        mutableUiState.value = mutableUiState.value.copy(
            isSavingHistory = true
        )

        // A separate job lets a completed check finish saving if the user
        // returns to the input or starts another check.
        viewModelScope.launch {
            try {
                val savedId = checkRepository.save(result)

                // Do not overwrite the state of a newer check.
                if (mutableUiState.value.result === result) {
                    mutableUiState.value = mutableUiState.value.copy(
                        isSavingHistory = false,
                        savedCheckId = savedId,
                        historySaveError = null
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (mutableUiState.value.result === result) {
                    mutableUiState.value = mutableUiState.value.copy(
                        isSavingHistory = false,
                        historySaveError =
                            "This result could not be saved to history."
                    )
                }
            }
        }
    }


    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null

        // Keep the draft available after cancellation.
        mutableUiState.value = mutableUiState.value.copy(
            isAnalyzing = false,
            analysisError = null
        )
    }

    fun returnToMessage() {
        mutableUiState.value = mutableUiState.value.copy(
            result = null,
            analysisError = null,
            isShowingExplanation = false,
            isShowingReport = false,
            helpfulFeedback = null
        )
    }

    fun openExplanation() {
        if (mutableUiState.value.result == null) return

        mutableUiState.value = mutableUiState.value.copy(
            isShowingExplanation = true
        )
    }

    fun closeExplanation() {
        mutableUiState.value = mutableUiState.value.copy(
            isShowingExplanation = false
        )
    }

    fun markHelpful(helpful: Boolean) {
        if (mutableUiState.value.result == null) return

        // Temporary UI acknowledgement only.
        // No feedback is saved locally or submitted to a backend.
        mutableUiState.value = mutableUiState.value.copy(
            helpfulFeedback = helpful
        )
    }

    fun checkAnotherMessage() {
        analysisJob?.cancel()
        analysisJob = null
        mutableUiState.value = CheckMessageUiState()
    }

    fun openReport() {
        if (mutableUiState.value.result == null) return

        mutableUiState.value = mutableUiState.value.copy(
            isShowingReport = true,
            isShowingExplanation = false
        )
    }

    fun closeReport() {
        mutableUiState.value = mutableUiState.value.copy(
            isShowingReport = false
        )
    }
}