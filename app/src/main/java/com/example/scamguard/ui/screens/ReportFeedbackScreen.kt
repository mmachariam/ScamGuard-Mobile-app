package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.feedback.ReportReason
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardPrimaryButton
import com.example.scamguard.ui.components.rememberLocalApiPermission

@Composable
fun ReportFeedbackScreen(
    result: AnalysisResult,
    uiState: ReportFeedbackUiState,
    onReasonSelected: (ReportReason) -> Unit,
    onPermissionDenied: () -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val withLocalApiPermission = rememberLocalApiPermission(onPermissionDenied)
    val isSending = uiState.status == ReportStatus.SENDING
    val isEditing = uiState.status == ReportStatus.EDITING

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Report a problem",
            style = MaterialTheme.typography.headlineMedium
        )

        if (uiState.status == ReportStatus.SAVED || uiState.status == ReportStatus.DEMO_COMPLETE) {
            ScamGuardCard {
                Text(
                    text = if (result.isDemo) "Demo complete" else "Report saved",
                    style = MaterialTheme.typography.titleLarge
                )

                if (result.isDemo) {
                    Text(
                        text = "Demo feedback stays on this screen.",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            ScamGuardPrimaryButton(text = "Back to result", onClick = onClose)
        } else {
            if (result.isDemo) {
                Text(
                    text = "Demo report · feedback stays on this screen.",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            ScamGuardCard {
                Text(
                    text = result.riskLevel.label,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = result.originalMessage,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "What was the problem?",
                style = MaterialTheme.typography.titleMedium
            )

            Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                ReportReason.entries.forEach { reason ->
                    val selected = uiState.reason == reason
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                enabled = isEditing,
                                role = Role.RadioButton,
                                onClick = { onReasonSelected(reason) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = selected, onClick = null, enabled = isEditing)
                        Text(text = reason.label)
                    }
                }
            }

            if (!result.isDemo) {
                Text(
                    text = "The report sends the selected reason and risk level. " +
                        "The original message stays on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            uiState.submissionError?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            if (isSending) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text(text = "Sending report…")
                }
            }

            ScamGuardPrimaryButton(
                text = when {
                    result.isDemo -> "Preview demo submission"
                    uiState.status == ReportStatus.FAILED -> "Retry"
                    else -> "Send report"
                },
                enabled = uiState.canSubmit,
                onClick = {
                    if (result.isDemo) onSubmit() else withLocalApiPermission(onSubmit)
                }
            )

            TextButton(onClick = onClose, enabled = !isSending) {
                Text(text = "Cancel")
            }
        }
    }
}
