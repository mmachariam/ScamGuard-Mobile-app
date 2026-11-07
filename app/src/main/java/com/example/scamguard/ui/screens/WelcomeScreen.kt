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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardLogo
import com.example.scamguard.ui.components.ScamGuardPrimaryButton
import com.example.scamguard.ui.theme.ScamGuardTheme

/**
 * Welcome only displays content and exposes user actions.
 * Its parent decides navigation and, later, saves first-launch preferences.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // Scrolling keeps all controls reachable on small screens and large fonts.
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ScamGuardLogo()

        Text(
            text = "ScamGuard",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Understand before you respond.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Make sense of suspicious messages before taking action.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        ScamGuardCard {
            WelcomeFeature(
                title = "Check suspicious text",
                description = "Paste a message from SMS, WhatsApp or email."
            )

            Spacer(modifier = Modifier.height(20.dp))

            WelcomeFeature(
                title = "Understand warning signs",
                description = "See plain-language explanations of possible risks."
            )

            Spacer(modifier = Modifier.height(20.dp))

            WelcomeFeature(
                title = "Know what to do next",
                description = "Get practical advice to help you respond safely."
            )
        }

        Text(
            text = "Predictions can be wrong. Always verify.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        ScamGuardPrimaryButton(
            text = "Get started",
            onClick = onGetStarted
        )

        TextButton(onClick = onPrivacyClick) {
            Text(text = "Privacy & data")
        }
    }
}

/** Repeated feature text uses the same typography and spacing. */
@Composable
private fun WelcomeFeature(
    title: String,
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    ScamGuardTheme {
        WelcomeScreen(
            onGetStarted = {},
            onPrivacyClick = {}
        )
    }
}