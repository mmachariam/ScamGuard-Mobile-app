package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardPrimaryButton
import com.example.scamguard.ui.theme.ScamHighRisk
import com.example.scamguard.ui.theme.ScamLowRisk
import com.example.scamguard.ui.theme.ScamUncertain

@Composable
fun AnalyzingScreen(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 20.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        // Show activity while waiting without guessing how much work is left.
        CircularProgressIndicator()

        Text(
            text = "Checking your message…",
            style = MaterialTheme.typography.headlineMedium
        )

        TextButton(onClick = onCancel) {
            Text(text = "Cancel check")
        }
    }
}

@Composable
fun ResultScreen(
    result: AnalysisResult,
    helpfulFeedback: Boolean?,
    onBack: () -> Unit,
    onSeeExplanation: () -> Unit,
    onHelpful: (Boolean) -> Unit,
    onCheckAnother: () -> Unit,
    onReportIncorrect: () -> Unit,
    historyStatus: String? = null,
    backLabel: String = "Back to message",
    modifier: Modifier = Modifier
) {
    val verdictColor = when (result.riskLevel) {
        RiskLevel.HIGH -> ScamHighRisk
        RiskLevel.UNCERTAIN -> ScamUncertain
        RiskLevel.LOW -> ScamLowRisk
    }

    // Use the API's risk category to choose a label people can read at a glance.
    val verdictTitle = if (result.isDemo) {
        result.riskLevel.label
    } else {
        when (result.riskLevel) {
            RiskLevel.HIGH -> "Possible scam"
            RiskLevel.LOW -> "No strong warning signs found"
            RiskLevel.UNCERTAIN -> "We couldn’t assess this message"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(text = backLabel)
        }

        Text(
            text = "Result",
            style = MaterialTheme.typography.headlineMedium
        )

        if (result.isDemo) {
            ScamGuardCard {
                Text(
                    text = "DEMO RESULT",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "This demo checks for a few keywords. " +
                            "Verify unexpected requests separately.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Show the history status when the caller provides one.
        historyStatus?.let { status ->
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        ScamGuardCard {
            // The written title communicates the result alongside its colour.
            Text(
                text = verdictTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = verdictColor
            )

            Text(
                text = result.summary,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        ScamGuardCard {
            Text(
                text = "Original message",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = result.originalMessage,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Keep the demo's empty message. Live results need phrases to show this card.
        if (result.isDemo || result.warningSigns.isNotEmpty()) {
            ScamGuardCard {
                Text(
                    text = when {
                        result.isDemo -> "Demo warning signs"
                        result.riskLevel == RiskLevel.HIGH -> "What stood out"
                        else -> "Why this result appeared"
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                if (result.warningSigns.isEmpty()) {
                    Text(
                        text = "No demo patterns matched. Verify unexpected " +
                                "requests through a contact you trust.",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Quote the phrases in the order supplied with the result.
                    result.warningSigns.forEach { sign ->
                        Text(
                            text = "• “${sign.phrase}”",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ScamGuardPrimaryButton(
                    text = "See explanation",
                    onClick = onSeeExplanation
                )
            }
        }

        ScamGuardCard {
            Text(
                text = "What to do next",
                style = MaterialTheme.typography.titleLarge
            )

            result.recommendedActions.forEachIndexed { index, action ->
                Text(
                    text = "${index + 1}. $action",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        ScamGuardCard {
            Text(
                text = "Was this result helpful?",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = helpfulFeedback == true,
                    onClick = { onHelpful(true) },
                    label = { Text(text = "Yes") }
                )

                FilterChip(
                    selected = helpfulFeedback == false,
                    onClick = { onHelpful(false) },
                    label = { Text(text = "No") }
                )
            }

            if (helpfulFeedback != null) {
                // Acknowledge the choice without claiming a submission.
                Text(
                    text = "Demo acknowledgement: you selected " +
                            (if (helpfulFeedback) "Yes." else "No.") +
                            " Your choice is temporary and has not been " +
                            "saved or submitted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        TextButton(onClick = onReportIncorrect) {
            Text(text = "Report incorrect prediction")
        }

        Text(
            text = "If a request is unexpected, verify it before acting.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        ScamGuardPrimaryButton(
            text = "Check another message",
            onClick = onCheckAnother
        )
    }
}
