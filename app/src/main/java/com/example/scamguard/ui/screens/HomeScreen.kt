package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.data.local.SavedCheck
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardIcons
import com.example.scamguard.ui.components.ScamGuardPrimaryButton
import com.example.scamguard.ui.theme.ScamHighRisk
import com.example.scamguard.ui.theme.ScamLowRisk
import com.example.scamguard.ui.theme.ScamUncertain
import java.text.DateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun HomeScreen(
    onCheckMessage: () -> Unit,
    onViewHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSavedCheck: (Long) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ScamGuard",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            FilledTonalIconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = ScamGuardIcons.Settings,
                    contentDescription = "Settings"
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Check before you trust.",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Suspicious message? Take a moment to understand it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        ScamGuardCard {
            Icon(
                imageVector = ScamGuardIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Something feels off?",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Check suspicious text from SMS, WhatsApp or email.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            ScamGuardPrimaryButton(
                text = "Check a message",
                onClick = onCheckMessage
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Recent checks",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(onClick = onViewHistory) {
                    Text(text = "View all")
                }
            }

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp)
                    )
                }

                uiState.errorMessage != null -> {
                    ScamGuardCard {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )

                        TextButton(
                            onClick = homeViewModel::loadRecentChecks
                        ) {
                            Text(text = "Retry")
                        }
                    }
                }

                uiState.recentChecks.isEmpty() -> {
                    ScamGuardCard {
                        Text(
                            text = "No checks yet",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Your saved message checks will appear here.",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                else -> {
                    // Room updates these cards automatically after saves/deletions.
                    uiState.recentChecks.forEach { check ->
                        RecentCheckCard(
                            check = check,
                            onOpen = { onOpenSavedCheck(check.id) }
                        )
                    }
                }
            }
        }

        ScamGuardCard {
            Text(
                text = "Safety tip",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Never share your PIN, password or verification code. " +
                        "Verify unexpected requests using a contact you already trust.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Predictions can be wrong. Always verify.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun RecentCheckCard(
    check: SavedCheck,
    onOpen: () -> Unit
) {
    val riskColor = when (check.result.riskLevel) {
        RiskLevel.HIGH -> ScamHighRisk
        RiskLevel.UNCERTAIN -> ScamUncertain
        RiskLevel.LOW -> ScamLowRisk
    }

    // Use the current observable locale for recent-check timestamps.
    val locale = LocalConfiguration.current.locales[0]

    val timestamp = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
        locale
    ).format(Date(check.createdAt))

    ScamGuardCard {
        Text(
            text = check.result.riskLevel.label,
            style = MaterialTheme.typography.titleMedium,
            color = riskColor
        )

        if (check.result.isDemo) {
            Text(
                text = "DEMO RESULT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = check.result.originalMessage,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = timestamp,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        TextButton(onClick = onOpen) {
            Text(text = "Open result")
        }
    }
}