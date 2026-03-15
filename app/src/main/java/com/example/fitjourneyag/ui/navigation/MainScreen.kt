package com.example.fitjourneyag.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitjourneyag.ui.client.ClientDashboardScreen
import com.example.fitjourneyag.ui.client.ai.AiCoachScreen
import com.example.fitjourneyag.ui.client.progress.ProgressScreen
import com.example.fitjourneyag.ui.client.track.TrackScreen
import com.example.fitjourneyag.ui.theme.FJBackground
import com.example.fitjourneyag.ui.theme.FJGold
import com.example.fitjourneyag.ui.theme.FJSurface

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : NavItem(Screen.ClientDashboard.route, Icons.Default.Dashboard, "Dashboard")
    object Track : NavItem(Screen.ClientTrack.route, Icons.Default.EditNote, "Track")
    object Progress : NavItem(Screen.ClientProgress.route, Icons.Default.ShowChart, "Progress")
    object Coach : NavItem(Screen.AiCoach.route, Icons.Default.AutoAwesome, "AI Coach")
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToWeightProgress: () -> Unit,
    onNavigateToProgressPhotos: () -> Unit,
    onNavigateToStrengthProgress: () -> Unit,
    onNavigateToBodyMeasurements: () -> Unit,
    onNavigateToBmi: () -> Unit,
    onNavigateToTdee: () -> Unit,
    onNavigateToMacros: () -> Unit,
    onNavigateToVoiceCall: () -> Unit
) {
    val navController = rememberNavController()
    val navItems = listOf(
        NavItem.Dashboard,
        NavItem.Track,
        NavItem.Progress,
        NavItem.Coach
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val shouldShowBottomBar = currentDestination?.hierarchy?.any { it.route == Screen.AiCoach.route } != true

            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = FJSurface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp)) },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FJGold,
                                selectedTextColor = FJGold,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = FJGold.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = FJBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ClientDashboard.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.ClientDashboard.route) {
                val viewModel: com.example.fitjourneyag.ui.client.ClientDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
                )
                ClientDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDiet = onNavigateToDiet,
                    onNavigateToWorkout = onNavigateToWorkout,
                    onNavigateToProgress = { 
                        navController.navigate(Screen.ClientProgress.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToChat = {
                        navController.navigate(Screen.AiCoach.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToGeneratePlan = onNavigateToGeneratePlan,
                    onNavigateToInsights = onNavigateToInsights,
                    onNavigateToHabits = onNavigateToHabits
                )
            }
            
            composable(Screen.ClientTrack.route) {
                val viewModel: com.example.fitjourneyag.ui.client.track.TrackViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
                )
                TrackScreen(
                    viewModel = viewModel,
                    onNavigateToDiet = onNavigateToDiet,
                    onNavigateToWorkout = onNavigateToWorkout,
                    onNavigateToBmi = onNavigateToBmi,
                    onNavigateToTdee = onNavigateToTdee,
                    onNavigateToMacros = onNavigateToMacros,
                    onNavigateToHabits = onNavigateToHabits
                )
            }
            
            composable(Screen.ClientProgress.route) {
                val viewModel: com.example.fitjourneyag.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
                )
                ProgressScreen(
                    viewModel = viewModel,
                    onNavigateToWeight = onNavigateToWeightProgress,
                    onNavigateToPhotos = onNavigateToProgressPhotos,
                    onNavigateToStrength = onNavigateToStrengthProgress,
                    onNavigateToMeasurements = onNavigateToBodyMeasurements
                )
            }
            
            composable(Screen.AiCoach.route) {
                val viewModel: com.example.fitjourneyag.ui.client.ai.AiCoachViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
                )
                AiCoachScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.ClientDashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToVoiceCall = onNavigateToVoiceCall
                )
            }
        }
    }
}
