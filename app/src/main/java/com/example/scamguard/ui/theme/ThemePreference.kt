package com.example.scamguard.ui.theme

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

internal enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}

internal class ThemePreference(context: Context) {
    private val preferences = context.getSharedPreferences(
        "scamguard_appearance",
        Context.MODE_PRIVATE
    )

    // Fall back to the system setting on first launch or if a saved value is unknown.
    var mode by mutableStateOf(
        ThemeMode.entries.firstOrNull {
            it.name == preferences.getString("theme_mode", null)
        } ?: ThemeMode.SYSTEM
    )
        private set

    fun select(newMode: ThemeMode) {
        // Update Compose immediately and save the choice for the next launch.
        mode = newMode
        preferences.edit().putString("theme_mode", newMode.name).apply()
    }
}

@Composable
internal fun rememberThemePreference(): ThemePreference {
    val context = LocalContext.current.applicationContext
    return remember(context) { ThemePreference(context) }
}

internal val LocalThemePreference = compositionLocalOf<ThemePreference> {
    error("Appearance settings must be inside ScamGuardTheme.")
}

// Screens and system bars can use the same resolved theme as the app.
val LocalScamGuardDarkTheme = compositionLocalOf { false }

@Composable
fun ThemePickerButton() {
    val preference = LocalThemePreference.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Appearance: ${preference.mode.label}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeMode.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (option == preference.mode) {
                                "${option.label} (selected)"
                            } else {
                                option.label
                            }
                        )
                    },
                    onClick = {
                        preference.select(option)
                        expanded = false
                    }
                )
            }
        }
    }
}