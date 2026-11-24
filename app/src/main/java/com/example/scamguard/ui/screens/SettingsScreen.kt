package com.example.scamguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.theme.ThemePickerButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onManageHistory: () -> Unit,
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsStateWithLifecycle()

    var showClearConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

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
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        ThemePickerButton()

        ScamGuardCard {
            Text(
                text = "Local history",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.loadError != null -> {
                    Text(
                        text = uiState.loadError.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = historyViewModel::loadHistory
                    ) {
                        Text(text = "Retry")
                    }
                }

                else -> {
                    val count = uiState.checks.size

                    Text(
                        text = when (count) {
                            0 -> "No saved checks."
                            1 -> "1 saved check."
                            else -> "$count saved checks."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = "Completed checks are saved on this device. " +
                        "You can delete individual entries in History " +
                        "or clear the entire list here.",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = onManageHistory) {
                Text(text = "Manage history")
            }

            TextButton(
                enabled = !uiState.isLoading &&
                        uiState.loadError == null &&
                        uiState.checks.isNotEmpty() &&
                        !uiState.isDeleting,
                onClick = { showClearConfirmation = true }
            ) {
                Text(
                    text = "Clear all history",
                    color = if (uiState.checks.isNotEmpty()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }

            if (uiState.isDeleting) {
                Text(
                    text = "Clearing history…",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // A failed deletion must not be presented as successful.
            uiState.operationError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        ScamGuardCard {
            Text(
                text = "Privacy & data",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Understand what ScamGuard stores and " +
                        "how it handles your messages.",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            TextButton(onClick = onOpenPrivacy) {
                Text(text = "Read privacy information")
            }
        }

        ScamGuardCard {
            Text(
                text = "About ScamGuard",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Understand before you respond.",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "University project · Development build\n\n" +
                        "Text analysis uses a trained TF-IDF logistic regression model " +
                        "through the analysis API.",
                modifier = Modifier.padding(top = 12.dp),
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

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = {
                Text(text = "Clear all history?")
            },
            text = {
                Text(
                    text = "This removes all saved checks from local " +
                            "history. It cannot be undone.\n\n" +
                            "Your Welcome preference will remain unchanged."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        historyViewModel.clearHistory()
                    }
                ) {
                    Text(text = "Clear all")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmation = false }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}