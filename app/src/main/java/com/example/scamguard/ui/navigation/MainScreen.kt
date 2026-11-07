package com.example.scamguard.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scamguard.ui.components.ScamGuardCard
import com.example.scamguard.ui.components.ScamGuardIcons
import com.example.scamguard.ui.screens.HomeScreen
import com.example.scamguard.ui.screens.CheckMessageScreen
import androidx.compose.foundation.layout.imePadding
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.scamguard.ui.screens.HistoryScreen
import com.example.scamguard.ui.screens.SavedCheckScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamguard.ui.screens.CheckMessageViewModel
import com.example.scamguard.ui.screens.SettingsScreen
import com.example.scamguard.ui.screens.PrivacyScreen




// Central route names prevent spelling differences between navigation calls.
private object Routes {
    const val HOME = "home"
    const val CHECK = "check"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
}

private data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val mainTabs = listOf(
    MainTab(Routes.HOME, "Home", ScamGuardIcons.Home),
    MainTab(Routes.CHECK, "Check", ScamGuardIcons.Check),
    MainTab(Routes.HISTORY, "History", ScamGuardIcons.History)
)

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // Keep one Check state above the navigation destinations.
    // Home and saved results can now explicitly start a fresh check.
    val checkViewModel: CheckMessageViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Settings and Privacy use Back instead of displaying the main tabs.
    val showBottomBar = mainTabs.any { it.route == currentRoute }

    fun openTab(route: String) {
        navController.navigate(route) {
            // Return to the Home root rather than stacking repeated tab visits.
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }

            launchSingleTop = true
            restoreState = true
        }
    }

    fun startNewCheck() {
        // Cancel an unfinished analysis and clear its temporary screen state.
        // Previously completed checks remain in Room history.
        checkViewModel.checkAnotherMessage()
        openTab(Routes.CHECK)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            // Keep the bottom navigation and screen content above the keyboard.
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    mainTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { openTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    // The visible tab label supplies its name.
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(text = tab.label)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onCheckMessage = { startNewCheck() },
                    onViewHistory = { openTab(Routes.HISTORY) },
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS) {
                            launchSingleTop = true
                        }
                    },
                    onOpenSavedCheck = { id ->
                        navController.navigate("saved-check/$id") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = "saved-check/{checkId}",
                arguments = listOf(
                    navArgument("checkId") {
                        type = NavType.LongType
                    }
                )
            ) { entry ->
                val checkId = requireNotNull(entry.arguments).getLong("checkId")

                SavedCheckScreen(
                    checkId = checkId,
                    onBack = {
                        // Returns to whichever screen opened this result: Home or History.
                        navController.popBackStack()
                    },
                    onCheckAnother = {
                        navController.popBackStack()
                        startNewCheck()
                    }
                )
            }

            composable(Routes.CHECK) {
                CheckMessageScreen(
                    checkViewModel = checkViewModel
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    onOpenCheck = { id ->
                        navController.navigate("saved-check/$id") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenPrivacy = {
                        navController.navigate(Routes.PRIVACY) {
                            launchSingleTop = true
                        }
                    },
                    onManageHistory = {
                        openTab(Routes.HISTORY)
                    }
                )
            }

            composable(Routes.PRIVACY) {
                PrivacyScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onManageHistory = {
                        openTab(Routes.HISTORY)
                    }
                )
            }
        }
    }
}

/**
 * A shared layout for the small supporting pages and temporary destinations.
 * Its optional actions are shown only when callbacks are supplied.
 */
@Composable
private fun InformationScreen(
    title: String,
    heading: String,
    description: String,
    onBack: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        ScamGuardCard {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = description,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}