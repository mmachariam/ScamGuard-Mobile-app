package com.example.scamguard.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SavedCheckScreen(
    checkId: Long,
    onBack: () -> Unit,
    onCheckAnother: () -> Unit,
    historyViewModel: HistoryViewModel = viewModel(),
    reportViewModel: ReportFeedbackViewModel = viewModel()
) {
    val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()
    val reportState by reportViewModel.uiState.collectAsStateWithLifecycle()

    var showExplanation by rememberSaveable(checkId) {
        mutableStateOf(false)
    }

    var showReport by rememberSaveable(checkId) {
        mutableStateOf(false)
    }

    var helpfulFeedback by rememberSaveable(checkId) {
        mutableStateOf<Boolean?>(null)
    }

    val closeReport: () -> Unit = {
        if (reportViewModel.uiState.value.status != ReportStatus.SENDING) {
            reportViewModel.reset()
            showReport = false
        }
    }

    BackHandler(enabled = showExplanation || showReport) {
        if (showReport) {
            closeReport()
        } else {
            showExplanation = false
        }
    }

    val savedCheck = historyState.checks.firstOrNull {
        it.id == checkId
    }

    when {
        historyState.isLoading -> {
            CircularProgressIndicator(
                modifier = Modifier.padding(24.dp)
            )
        }

        historyState.loadError != null -> {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = historyState.loadError.orEmpty())

                TextButton(onClick = historyViewModel::loadHistory) {
                    Text(text = "Retry")
                }

                TextButton(onClick = onBack) {
                    Text(text = "Back to history")
                }
            }
        }

        savedCheck == null -> {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "This saved check is no longer available.")

                TextButton(onClick = onBack) {
                    Text(text = "Back to history")
                }
            }
        }

        else -> {
            val result = savedCheck.result

            when {
                showReport -> {
                    ReportFeedbackScreen(
                        result = result,
                        uiState = reportState,
                        onReasonSelected = reportViewModel::selectReason,
                        onPermissionDenied = reportViewModel::onPermissionDenied,
                        onSubmit = { reportViewModel.submit(result) },
                        onClose = closeReport
                    )
                }

                showExplanation -> {
                    ExplanationScreen(
                        result = result,
                        onBack = { showExplanation = false },
                        onCheckAnother = onCheckAnother
                    )
                }

                else -> {
                    ResultScreen(
                        result = result,
                        helpfulFeedback = helpfulFeedback,
                        onBack = onBack,
                        onSeeExplanation = { showExplanation = true },
                        onHelpful = { helpfulFeedback = it },
                        onCheckAnother = onCheckAnother,
                        onReportIncorrect = {
                            reportViewModel.reset()
                            showReport = true
                        },
                        backLabel = "Back",
                        historyStatus = "Opened from local history"
                    )
                }
            }
        }
    }
}
