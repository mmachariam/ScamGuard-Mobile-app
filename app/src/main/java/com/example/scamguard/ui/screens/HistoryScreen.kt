package com.example.scamguard.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.ui.components.ScamGuardCard
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun HistoryScreen(
    onOpenCheck: (Long) -> Unit,
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsStateWithLifecycle()

    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("ALL") }

    // Dialog state stores an ID rather than a mutable database record.
    var pendingDeleteId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var showClearConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    val filteredChecks = uiState.checks.filter { check ->
        val matchesSearch = check.result.originalMessage.contains(
            other = search,
            ignoreCase = true
        )

        val matchesRisk = filter == "ALL" ||
                check.result.riskLevel.name == filter

        matchesSearch && matchesRisk
    }

    // Linked grouping preserves the newest-first order from the database.
    // Read observable locale state so formatting follows language changes.
    val locale = LocalConfiguration.current.locales[0]

    val dateFormatter = SimpleDateFormat(
        "EEEE, d MMM yyyy",
        locale
    )

    val timeFormatter = DateFormat.getTimeInstance(
        DateFormat.SHORT,
        locale
    )

    val groupedChecks = filteredChecks.groupBy { check ->
        dateFormatter.format(Date(check.createdAt))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "History",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                enabled = uiState.checks.isNotEmpty() &&
                        !uiState.isDeleting,
                onClick = { showClearConfirmation = true }
            ) {
                Text(text = "Clear all")
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Search saved messages") },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter == "ALL",
                onClick = { filter = "ALL" },
                label = { Text(text = "All") }
            )

            listOf(
                RiskLevel.HIGH,
                RiskLevel.UNCERTAIN,
                RiskLevel.LOW
            ).forEach { risk ->
                FilterChip(
                    selected = filter == risk.name,
                    onClick = { filter = risk.name },
                    label = { Text(text = risk.label) }
                )
            }
        }

        uiState.operationError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.loadError != null -> {
                Text(
                    text = uiState.loadError.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(onClick = historyViewModel::loadHistory) {
                    Text(text = "Retry")
                }
            }

            uiState.checks.isEmpty() -> {
                ScamGuardCard {
                    Text(
                        text = "No checks yet",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Complete a message check to save it here.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            filteredChecks.isEmpty() -> {
                ScamGuardCard {
                    Text(
                        text = "No matching checks",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Try a different search or risk filter.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedChecks.forEach { (date, checks) ->
                        item {
                            Text(
                                text = date,
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(
                            items = checks,
                            key = { check -> check.id }
                        ) { check ->
                            ScamGuardCard {
                                Text(
                                    text = check.result.riskLevel.label,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                if (check.result.isDemo) {
                                    Text(
                                        text = "DEMO RESULT",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                Text(
                                    text = check.result.originalMessage,
                                    modifier = Modifier.padding(top = 8.dp),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = timeFormatter.format(
                                        Date(check.createdAt)
                                    ),
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(
                                        onClick = { onOpenCheck(check.id) }
                                    ) {
                                        Text(text = "Open")
                                    }

                                    TextButton(
                                        enabled = !uiState.isDeleting,
                                        onClick = {
                                            pendingDeleteId = check.id
                                        }
                                    ) {
                                        Text(text = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(text = "Delete this check?") },
            text = {
                Text(text = "This removes the saved check from local history.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteId = null
                        historyViewModel.deleteCheck(id)
                    }
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(text = "Clear all history?") },
            text = {
                Text(
                    text = "All saved checks will be removed from local " +
                            "history. This cannot be undone."
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