package com.example.scamguard.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.preferences.OnboardingPreferences
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * null means we have not successfully read the preference yet.
 * Keeping that separate from false prevents a Welcome flash on startup.
 */
data class OnboardingUiState(
    val welcomeCompleted: Boolean? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class OnboardingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferences = OnboardingPreferences(application)

    private val mutableUiState = MutableStateFlow(OnboardingUiState())

    // Screens can observe state but cannot modify it directly.
    val uiState = mutableUiState.asStateFlow()

    init {
        loadPreference()
    }

    private fun loadPreference() {
        mutableUiState.value = OnboardingUiState()

        viewModelScope.launch {
            try {
                // Read once at startup. This ViewModel handles subsequent changes.
                val completed = preferences.welcomeCompleted.first()

                mutableUiState.value = OnboardingUiState(
                    welcomeCompleted = completed,
                    isLoading = false
                )
            } catch (_: IOException) {
                // Do not guess the saved value when storage cannot be read.
                mutableUiState.value = OnboardingUiState(
                    isLoading = false,
                    errorMessage = "Could not load your preferences. Please retry."
                )
            }
        }
    }

    fun retryLoading() {
        if (mutableUiState.value.isLoading) return
        loadPreference()
    }

    fun getStarted() {
        val currentState = mutableUiState.value

        // Prevent repeated taps from starting duplicate writes.
        if (
            currentState.isLoading ||
            currentState.isSaving ||
            currentState.welcomeCompleted != false
        ) {
            return
        }

        mutableUiState.value = currentState.copy(
            isSaving = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                preferences.completeWelcome()

                // Open Home only after the preference has been saved.
                mutableUiState.value = OnboardingUiState(
                    welcomeCompleted = true,
                    isLoading = false
                )
            } catch (_: IOException) {
                mutableUiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "Could not save your preference. Please try again."
                )
            }
        }
    }

    fun dismissError() {
        mutableUiState.value = mutableUiState.value.copy(
            errorMessage = null
        )
    }
}