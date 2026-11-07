package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardPrimaryButton

@Composable
fun ExplanationScreen(
    result: AnalysisResult,
    onBack: () -> Unit,
    onCheckAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightBackground = MaterialTheme.colorScheme.primaryContainer
    val highlightForeground = MaterialTheme.colorScheme.onPrimaryContainer

    // Highlight only phrases actually present in the submitted message.
    val highlightedMessage = remember(
        result,
        highlightBackground,
        highlightForeground
    ) {
        buildAnnotatedString {
            append(result.originalMessage)

            result.warningSigns.forEach { sign ->
                if (sign.phrase.isNotEmpty()) {
                    var start = result.originalMessage.indexOf(
                        string = sign.phrase,
                        ignoreCase = true
                    )

                    // Highlight repeated occurrences as well as the first match.
                    while (start >= 0) {
                        val end = start + sign.phrase.length

                        addStyle(
                            style = SpanStyle(
                                background = highlightBackground,
                                color = highlightForeground,
                                fontWeight = FontWeight.SemiBold
                            ),
                            start = start,
                            end = end
                        )

                        start = result.originalMessage.indexOf(
                            string = sign.phrase,
                            startIndex = end,
                            ignoreCase = true
                        )
                    }
                }
            }
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
            Text(text = "Back to result")
        }

        Text(
            text = when (result.riskLevel) {
                RiskLevel.HIGH -> "Why was this flagged?"
                RiskLevel.UNCERTAIN -> "What needs a closer look?"
                RiskLevel.LOW -> "What does this result mean?"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        if (result.isDemo) {
            Text(
                text = "Demo explanation · based on keyword matches, " +
                        "not a trained AI model.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ScamGuardCard {
            Text(
                text = "Your message",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = highlightedMessage,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        ScamGuardCard {
            Text(
                text = "Understanding this result",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyLarge
            )

            if (result.warningSigns.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "There are no matching phrases to highlight. " +
                            "These limited rules can miss scams, and a message " +
                            "can be harmful without using any of these keywords.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                result.warningSigns.forEach { sign ->
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "“${sign.phrase}”",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = sign.explanation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ScamGuardCard {
            Text(
                text = "Recommended next actions",
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

        Text(
            text = "Predictions can be wrong. Always verify.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        ScamGuardPrimaryButton(
            text = "Check another message",
            onClick = onCheckAnother
        )
    }
}