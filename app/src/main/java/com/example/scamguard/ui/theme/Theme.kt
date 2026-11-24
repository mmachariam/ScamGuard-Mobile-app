package com.example.scamguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider


private val ScamGuardColors = lightColorScheme(
    primary = ScamBlue,
    onPrimary = Color.White,
    primaryContainer = ScamLightBlue,
    onPrimaryContainer = ScamNavy,

    secondary = ScamSecondary,
    onSecondary = Color.White,
    secondaryContainer = ScamLightBlue,
    onSecondaryContainer = ScamNavy,

    background = ScamBackground,
    onBackground = ScamNavy,

    surface = Color.White,
    onSurface = ScamNavy,
    surfaceVariant = ScamLightBlue,
    onSurfaceVariant = ScamBody,

    outline = ScamSecondary,
    outlineVariant = ScamBorder,

    error = ScamHighRisk,
    onError = Color.White
)

private val ScamGuardDarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF002F66),
    primaryContainer = Color(0xFF234777),
    onPrimaryContainer = Color(0xFFD7E5FF),
    secondary = Color(0xFFB9C8E6),
    onSecondary = Color(0xFF23334E),
    secondaryContainer = Color(0xFF344562),
    onSecondaryContainer = Color(0xFFDCE7FF),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE6EDF7),
    surface = Color(0xFF131D2E),
    onSurface = Color(0xFFE6EDF7),
    surfaceVariant = Color(0xFF26344B),
    onSurfaceVariant = Color(0xFFC1CEE3),
    outline = Color(0xFF8E9DB6),
    outlineVariant = Color(0xFF3C4C66),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun ScamGuardTheme(content: @Composable () -> Unit) {
    val preference = rememberThemePreference()
    val systemDarkTheme = isSystemInDarkTheme()

    // Apply the saved choice. Only System default follows Android's setting.
    val darkTheme = when (preference.mode) {
        ThemeMode.SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    CompositionLocalProvider(
        LocalThemePreference provides preference,
        LocalScamGuardDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) ScamGuardDarkColors else ScamGuardColors,
            typography = Typography,
            content = content
        )
    }
}