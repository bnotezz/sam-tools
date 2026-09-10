package com.samtools.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samtools.data.preferences.UserPreferences
import com.samtools.ui.screens.home.HomeScreen
import com.samtools.ui.screens.savetodownloads.SaveToDownloadsScreen
import com.samtools.ui.screens.settings.SettingsScreen
import com.samtools.ui.screens.shortcuts.ShortcutsScreen

@Composable
fun SamToolsNavGraph(
    preferences: UserPreferences,
    onUpdateSubfolder: (String) -> Unit,
    onToggleInstantSave: (Boolean) -> Unit,
    onToggleOverwriteDuplicates: (Boolean) -> Unit,
    onToggleTailscaleConnect: (Boolean) -> Unit,
    onToggleTailscaleDisconnect: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes)) },
                        label = {
                            Text(
                                text = stringResource(screen.titleRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Downloads.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    preferences = preferences,
                    onNavigateToShortcuts = { navController.navigate(Screen.Shortcuts.route) },
                    onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) }
                )
            }
            composable(Screen.Shortcuts.route) {
                ShortcutsScreen(
                    preferences = preferences,
                    onToggleTailscaleConnect = onToggleTailscaleConnect,
                    onToggleTailscaleDisconnect = onToggleTailscaleDisconnect
                )
            }
            composable(Screen.Downloads.route) {
                SaveToDownloadsScreen(
                    preferences = preferences,
                    onUpdateSubfolder = onUpdateSubfolder,
                    onToggleInstantSave = onToggleInstantSave,
                    onToggleOverwriteDuplicates = onToggleOverwriteDuplicates
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
