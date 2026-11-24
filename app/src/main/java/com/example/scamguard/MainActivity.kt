package com.example.scamguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamguard.ui.navigation.MainScreen
import com.example.scamguard.ui.screens.OnboardingViewModel
import com.example.scamguard.ui.screens.WelcomeScreen
import com.example.scamguard.ui.theme.ScamGuardTheme
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.scamguard.ui.screens.PrivacyDetails
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.example.scamguard.ui.theme.LocalScamGuardDarkTheme



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ScamGuardTheme {
                val darkTheme = LocalScamGuardDarkTheme.current

                SideEffect {
                    // Match the system bar icons to the app's selected theme.
                    // Android's "light bars" setting uses dark icons.
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                ScamGuardApp()
            }
        }
    }
}

@Composable
private fun ScamGuardApp(
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    // Observe ViewModel state while respecting the Activity lifecycle.
    val uiState by onboardingViewModel.uiState.collectAsStateWithLifecycle()

    // Preserve the privacy dialog's visibility across device rotation.
    var showPrivacy by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when {
            uiState.isLoading || uiState.isSaving -> {
                // Wait for storage operations before choosing a screen.
                LoadingScreen(
                    message = if (uiState.isSaving) {
                        "Getting things ready…"
                    } else {
                        "Opening ScamGuard…"
                    },
                    modifier = screenModifier
                )
            }

            uiState.welcomeCompleted == null -> {
                // A failed read must not be treated as a first launch.
                PreferenceErrorScreen(
                    message = uiState.errorMessage
                        ?: "Could not load your preferences.",
                    onRetry = onboardingViewModel::retryLoading,
                    modifier = screenModifier
                )
            }

            uiState.welcomeCompleted == true -> {
                // MainScreen contains Home and the bottom navigation.
                MainScreen(
                    modifier = screenModifier
                        // System-bar padding was already applied above.
                        // Prevent MainScreen's Scaffold from adding it twice.
                        .consumeWindowInsets(innerPadding)
                )
            }

            else -> {
                WelcomeScreen(
                    // The ViewModel saves the preference before opening Home.
                    onGetStarted = onboardingViewModel::getStarted,
                    onPrivacyClick = { showPrivacy = true },
                    modifier = screenModifier
                )
            }
        }
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = {
                Text(text = "Privacy & data")
            },
            text = {
                // Limit the dialog height while keeping all privacy content reachable.
                PrivacyDetails(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) {
                    Text(text = "Close")
                }
            }
        )
    }

    // Show save errors separately. Read errors use the retry screen above.
    if (
        uiState.errorMessage != null &&
        uiState.welcomeCompleted != null
    ) {
        AlertDialog(
            onDismissRequest = onboardingViewModel::dismissError,
            title = {
                Text(text = "Could not get started")
            },
            text = {
                Text(text = uiState.errorMessage.orEmpty())
            },
            confirmButton = {
                TextButton(
                    onClick = onboardingViewModel::dismissError
                ) {
                    Text(text = "OK")
                }
            }
        )
    }
}

@Composable
private fun LoadingScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        CircularProgressIndicator()

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PreferenceErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )

        TextButton(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}