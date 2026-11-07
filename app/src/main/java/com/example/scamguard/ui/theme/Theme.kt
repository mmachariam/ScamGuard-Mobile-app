package com.example.scamguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun ScamGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ScamGuardColors,
        typography = Typography,
        content = content
    )
}