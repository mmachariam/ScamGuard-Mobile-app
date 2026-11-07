package com.example.scamguard.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declare the delegate once so the app shares one DataStore for this file.
private val Context.onboardingDataStore by preferencesDataStore(
    name = "scamguard_onboarding"
)

/**
 * Handles preference storage only.
 * It does not decide which screen to display.
 */
class OnboardingPreferences(context: Context) {

    // Application context avoids holding a reference to an Activity.
    private val dataStore = context.applicationContext.onboardingDataStore

    private val welcomeCompletedKey = booleanPreferencesKey(
        name = "welcome_completed"
    )

    // A missing preference means the user has not completed Welcome.
    val welcomeCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[welcomeCompletedKey] ?: false
    }

    suspend fun completeWelcome() {
        // edit saves the preference asynchronously and atomically.
        dataStore.edit { preferences ->
            preferences[welcomeCompletedKey] = true
        }
    }
}