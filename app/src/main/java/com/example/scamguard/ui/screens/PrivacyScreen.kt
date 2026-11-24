package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onManageHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(text = "Back")
        }

        Text(
            text = "Privacy & data",
            style = MaterialTheme.typography.headlineMedium
        )

        PrivacyDetails()

        TextButton(onClick = onManageHistory) {
            Text(text = "Manage saved history")
        }
    }
}

/**
 * Shared privacy content prevents Welcome and Settings from displaying
 * different descriptions of the same app behaviour.
 */
@Composable
fun PrivacyDetails(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrivacySection(
            title = "What you provide",
            body = "You manually paste or type message text. ScamGuard " +
                    "does not read your SMS or contacts and does not require " +
                    "an account."
        )

        PrivacySection(
            title = "How messages are analyzed",
            body = "When you tap Analyze message, your text is sent to " +
                    "ScamGuard's analysis server. The server uses the trained " +
                    "TF-IDF logistic regression model.\n\n" +
                    "The current API processes messages in memory and has no " +
                    "message-history database. Completed results are saved " +
                    "locally by the Android app."
        )

        PrivacySection(
            title = "What is stored",
            body = "ScamGuard stores your Welcome and appearance preferences " +
                    "and completed checks in app storage on this device.\n\n" +
                    "Saved checks contain the original message, verdict, " +
                    "explanation, recommended actions, timestamp and demo label."
        )

        PrivacySection(
            title = "Managing your history",
            body = "Saved checks remain until you remove them or clear " +
                    "the app's storage. Delete individual checks in History " +
                    "or clear all history from History or Settings.\n\n" +
                    "Deleting history does not reset your Welcome preference."
        )

        PrivacySection(
            title = "Feedback and reports",
            body = "Helpful-result choices and incorrect-prediction reports " +
                    "are temporary demo interactions. They are not submitted " +
                    "to a server or saved in the history database."
        )

        PrivacySection(
            title = "Before checking a message",
            body = "Remove PINs, passwords, verification codes and other " +
                    "private information. Predictions can be wrong. Always " +
                    "verify unexpected requests independently."
        )
    }
}

@Composable
private fun PrivacySection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}