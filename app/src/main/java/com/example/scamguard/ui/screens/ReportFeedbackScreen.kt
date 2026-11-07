package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.feedback.ReportChoice
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardPrimaryButton

@Composable
fun ReportFeedbackScreen(
    result: AnalysisResult,
    uiState: ReportFeedbackUiState,
    onChoiceSelected: (ReportChoice) -> Unit,
    onDetailsChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Report incorrect prediction",
            style = MaterialTheme.typography.headlineMedium
        )

        when (uiState.status) {
            ReportStatus.SENDING -> {
                CircularProgressIndicator()

                Text(
                    text = "Preparing demo acknowledgement…",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Simulated progress only. No message or feedback " +
                            "is being sent to a server."
                )

                TextButton(onClick = onClose) {
                    Text(text = "Cancel demo")
                }
            }

            ReportStatus.DEMO_COMPLETE -> {
                ScamGuardCard {
                    Text(
                        text = "Demo complete — nothing submitted",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "You selected: " +
                                uiState.choice?.label.orEmpty(),
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = "Your message and feedback were not sent " +
                                "or saved. The original prediction is unchanged.",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                ScamGuardPrimaryButton(
                    text = "Back to result",
                    onClick = onClose
                )
            }

            else -> {
                Text(
                    text = "DEMO MODE · feedback submission is not connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                ScamGuardCard {
                    Text(
                        text = if (result.isDemo) {
                            "Original demo prediction"
                        } else {
                            "Original prediction"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = result.riskLevel.label,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = result.originalMessage,
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = "What do you think this message is?",
                    style = MaterialTheme.typography.titleMedium
                )

                // Make each entire row a single accessible radio option.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    ReportChoice.entries.forEach { choice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.choice == choice,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onChoiceSelected(choice)
                                    }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = uiState.choice == choice,
                                // The parent row handles clicks and semantics.
                                onClick = null
                            )

                            Text(text = choice.label)
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.details,
                    onValueChange = onDetailsChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Additional details (optional)")
                    },
                    placeholder = {
                        Text(text = "Explain what seems incorrect…")
                    },
                    minLines = 4,
                    maxLines = 8,
                    isError = uiState.detailsError != null,
                    supportingText = {
                        Column {
                            uiState.detailsError?.let { error ->
                                Text(text = error)
                            }

                            Text(
                                text = "${uiState.details.length} / " +
                                        MAX_REPORT_DETAILS
                            )
                        }
                    }
                )

                Text(
                    text = "When a feedback backend is connected, submitting " +
                            "will share the original message, prediction, your " +
                            "choice and these details for review.\n\n" +
                            "This demo does not share or save that information. " +
                            "Remove private information from your details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                uiState.submissionError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                ScamGuardPrimaryButton(
                    text = if (uiState.status == ReportStatus.FAILED) {
                        "Retry demo"
                    } else {
                        "Preview demo submission"
                    },
                    enabled = uiState.canSubmit,
                    onClick = onSubmit
                )

                TextButton(onClick = onClose) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}