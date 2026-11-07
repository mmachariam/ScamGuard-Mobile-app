package com.example.scamguard.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.local.CheckRepository
import com.example.scamguard.data.local.SavedCheck
import com.example.scamguard.data.local.ScamGuardDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val checks: List<SavedCheck> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val isDeleting: Boolean = false,
    val operationError: String? = null
)

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CheckRepository(
        ScamGuardDatabase.getInstance(application).checkDao()
    )

    private val mutableUiState = MutableStateFlow(HistoryUiState())
    val uiState = mutableUiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        loadHistory()
    }

    fun loadHistory() {
        observationJob?.cancel()

        mutableUiState.value = mutableUiState.value.copy(
            isLoading = true,
            loadError = null
        )

        observationJob = viewModelScope.launch {
            try {
                // Room emits a new list after inserts and deletions.
                repository.observeAll().collect { checks ->
                    mutableUiState.value = mutableUiState.value.copy(
                        checks = checks,
                        isLoading = false,
                        loadError = null
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    isLoading = false,
                    loadError = "Could not load history. Please retry."
                )
            }
        }
    }

    fun deleteCheck(id: Long) {
        performDeletion { repository.deleteById(id) }
    }

    fun clearHistory() {
        performDeletion { repository.clearAll() }
    }

    private fun performDeletion(action: suspend () -> Unit) {
        if (mutableUiState.value.isDeleting) return

        mutableUiState.value = mutableUiState.value.copy(
            isDeleting = true,
            operationError = null
        )

        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    operationError = "Could not delete history. Please retry."
                )
            } finally {
                mutableUiState.value = mutableUiState.value.copy(
                    isDeleting = false
                )
            }
        }
    }
}