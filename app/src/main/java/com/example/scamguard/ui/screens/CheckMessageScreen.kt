package com.example.scamguard.ui.screens

import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardPrimaryButton
import androidx.activity.compose.BackHandler

@Composable
fun CheckMessageScreen(
    modifier: Modifier = Modifier,
    checkViewModel: CheckMessageViewModel = viewModel()
) {
    val uiState by checkViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    // This ViewModel belongs to the same Check navigation destination.
    val reportViewModel: ReportFeedbackViewModel = viewModel()

    val reportUiState by reportViewModel.uiState.collectAsStateWithLifecycle()

    // Both toolbar actions and system Back use the same cleanup behaviour.
    val closeReport: () -> Unit = {
        keyboardController?.hide()
        reportViewModel.reset()
        checkViewModel.closeReport()
    }

    // Back follows the current flow:
    // Explanation → Result → input.
    // During analysis it cancels the running task.
    BackHandler(
        enabled = uiState.isAnalyzing || uiState.result != null
    ) {
        when {
            uiState.isAnalyzing -> {
                checkViewModel.cancelAnalysis()
            }

            uiState.isShowingReport -> {
                closeReport()
            }

            uiState.isShowingExplanation -> {
                checkViewModel.closeExplanation()
            }

            else -> {
                checkViewModel.returnToMessage()
            }
        }
    }

    if (uiState.isAnalyzing) {
        AnalyzingScreen(
            onCancel = checkViewModel::cancelAnalysis,
            modifier = modifier
        )
        return
    }

    val result = uiState.result

    if (result != null) {
        when {
            uiState.isShowingReport -> {
                ReportFeedbackScreen(
                    result = result,
                    uiState = reportUiState,
                    onChoiceSelected = reportViewModel::selectChoice,
                    onDetailsChanged = reportViewModel::updateDetails,
                    onSubmit = {
                        keyboardController?.hide()
                        reportViewModel.submit(result)
                    },
                    onClose = closeReport,
                    modifier = modifier
                )
            }

            uiState.isShowingExplanation -> {
                ExplanationScreen(
                    result = result,
                    onBack = checkViewModel::closeExplanation,
                    onCheckAnother = checkViewModel::checkAnotherMessage,
                    modifier = modifier
                )
            }

            else -> {
                ResultScreen(
                    result = result,
                    helpfulFeedback = uiState.helpfulFeedback,
                    onBack = checkViewModel::returnToMessage,
                    onSeeExplanation = checkViewModel::openExplanation,
                    onHelpful = checkViewModel::markHelpful,
                    onCheckAnother = checkViewModel::checkAnotherMessage,
                    onReportIncorrect = {
                        // Start each report with a fresh form.
                        reportViewModel.reset()
                        checkViewModel.openReport()
                    },
                    historyStatus = when {
                        uiState.isSavingHistory -> "Saving to local history…"
                        uiState.historySaveError != null -> uiState.historySaveError
                        uiState.savedCheckId != null -> "Saved to local history"
                        else -> null
                    },
                    modifier = modifier
                )
            }
        }

        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Check a message",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Paste suspicious text from SMS, WhatsApp or email.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        ScamGuardCard {
            Text(
                text = "Protect your private information",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Remove PINs, passwords and verification codes " +
                        "before checking a message.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        // Read the clipboard only after an explicit Paste tap.
                        // Use text directly; do not open clipboard URIs or files.
                        try {
                            val clipboard = context.getSystemService(
                                ClipboardManager::class.java
                            )

                            val clip = clipboard?.primaryClip

                            val text = if (
                                clip != null && clip.itemCount > 0
                            ) {
                                clip.getItemAt(0).text?.toString()
                            } else {
                                null
                            }

                            checkViewModel.pasteMessage(text)
                        } catch (_: SecurityException) {
                            checkViewModel.showInputError(
                                "Clipboard access was unavailable. " +
                                        "Try pasting directly into the text field."
                            )
                        }
                    }
                ) {
                    Text(text = "Paste")
                }

                TextButton(
                    onClick = checkViewModel::clearMessage,
                    enabled = uiState.message.isNotEmpty() ||
                            uiState.inputError != null
                ) {
                    Text(text = "Clear")
                }
            }

            OutlinedTextField(
                value = uiState.message,
                onValueChange = checkViewModel::updateMessage,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Message text")
                },
                placeholder = {
                    Text(text = "Paste or type the full message here…")
                },
                minLines = 8,
                maxLines = 12,
                isError = uiState.inputError != null,
                supportingText = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.inputError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = "${uiState.message.length} / " +
                                    MAX_MESSAGE_LENGTH,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

        Text(
            text = "Tip: include the full message for context, " +
                    "while removing private information.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        // The draft remains available if analysis fails.
        uiState.analysisError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ScamGuardPrimaryButton(
            text = if (uiState.analysisError != null) {
                "Retry demo analysis"
            } else {
                "Analyze message"
            },
            enabled = uiState.canAnalyze,
            onClick = {
                keyboardController?.hide()
                checkViewModel.analyze()
            }
        )

        Text(
            text = "Predictions can be wrong. Always verify.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}