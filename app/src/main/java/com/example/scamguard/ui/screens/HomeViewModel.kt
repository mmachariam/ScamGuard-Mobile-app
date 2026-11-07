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

data class HomeUiState(
    val recentChecks: List<SavedCheck> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CheckRepository(
        ScamGuardDatabase.getInstance(application).checkDao()
    )

    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState = mutableUiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        loadRecentChecks()
    }

    fun loadRecentChecks() {
        observationJob?.cancel()

        mutableUiState.value = mutableUiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        observationJob = viewModelScope.launch {
            try {
                // Fetch only the latest three records for the Home screen.
                repository.observeRecent(limit = 3).collect { checks ->
                    mutableUiState.value = HomeUiState(
                        recentChecks = checks,
                        isLoading = false
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not load recent checks."
                )
            }
        }
    }
}