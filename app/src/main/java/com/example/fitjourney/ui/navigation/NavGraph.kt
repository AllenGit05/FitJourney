package com.example.fitjourney.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.fitjourney.di.AppViewModelProvider

@Composable
fun FitJourneyNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Login.route) {
            val viewModel: com.example.fitjourney.ui.auth.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            val navigateToAdmin by viewModel.navigateToAdmin.collectAsState()
            val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()

            androidx.compose.runtime.LaunchedEffect(navigateToAdmin, navigateToDashboard) {
                if (navigateToAdmin) {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    viewModel.onNavigationHandled()
                } else if (navigateToDashboard) {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    viewModel.onNavigationHandled()
                }
            }

            com.example.fitjourney.ui.auth.LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignUpClient.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { role ->
                    // Logic moved to LaunchedEffect for specific flags
                }
            )
        }

        composable(route = Screen.SignUpClient.route) {
            val viewModel: com.example.fitjourney.ui.auth.SignUpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.auth.SignUpScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUpClient.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            val viewModel: com.example.fitjourney.ui.auth.ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.auth.ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = { 
                    navController.popBackStack()
                }
            )
        }

        // Client Main Container
        composable(route = Screen.Main.route) {
            com.example.fitjourney.ui.navigation.MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToDiet = { navController.navigate(Screen.ClientDiet.route) },
                onNavigateToWorkout = { navController.navigate(Screen.ClientWorkout.route) },
                onNavigateToSettings = { navController.navigate(Screen.ClientSettings.route) },
                onNavigateToGeneratePlan = { navController.navigate(Screen.GenerateWorkoutPlan.route) },
                onNavigateToInsights = { navController.navigate(Screen.WeeklyInsights.route) },
                onNavigateToHabits = { navController.navigate(Screen.ClientHabits.route) },
                onNavigateToWeightProgress = { navController.navigate(Screen.WeightProgress.route) },
                onNavigateToProgressPhotos = { navController.navigate(Screen.ProgressPhotos.route) },
                onNavigateToStrengthProgress = { navController.navigate(Screen.StrengthProgress.route) },
                onNavigateToBodyMeasurements = { navController.navigate(Screen.BodyMeasurements.route) },
                onNavigateToBmi = { navController.navigate(Screen.BmiCalculator.route) },
                onNavigateToTdee = { navController.navigate(Screen.TdeeCalculator.route) },
                onNavigateToMacros = { navController.navigate(Screen.MacroCalculator.route) },
                onNavigateToVoiceCall = { navController.navigate(Screen.AiVoiceCall.route) }
            )
        }

        // Detailed Screens

        composable(route = Screen.ClientDiet.route) {
            val viewModel: com.example.fitjourney.ui.client.diet.DietTrackingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.diet.DietTrackingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ClientWorkout.route,
            deepLinks = listOf(
                androidx.navigation.navDeepLink { uriPattern = "fitjourney://workout" }
            )
        ) {
            val viewModel: com.example.fitjourney.ui.client.workout.WorkoutTrackingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.workout.WorkoutTrackingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.BmiCalculator.route) {
            val viewModel: com.example.fitjourney.ui.client.calculators.CalculatorsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.calculators.BmiCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.TdeeCalculator.route) {
            val viewModel: com.example.fitjourney.ui.client.calculators.CalculatorsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.calculators.TdeeCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.MacroCalculator.route) {
            val viewModel: com.example.fitjourney.ui.client.calculators.CalculatorsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.calculators.MacroCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.WeightProgress.route) {
            val viewModel: com.example.fitjourney.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.progress.WeightProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ProgressPhotos.route) {
            val viewModel: com.example.fitjourney.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.progress.ProgressPhotosScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.StrengthProgress.route) {
            val viewModel: com.example.fitjourney.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.progress.StrengthProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.BodyMeasurements.route) {
            val viewModel: com.example.fitjourney.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.progress.BodyMeasurementsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ClientSettings.route) {
            val viewModel: com.example.fitjourney.ui.client.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.settings.SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.AiCoach.route) {
            val viewModel: com.example.fitjourney.ui.client.ai.AiCoachViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.ai.AiCoachScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVoiceCall = { navController.navigate(Screen.AiVoiceCall.route) }
            )
        }

        composable(route = Screen.AiVoiceCall.route) {
            val viewModel: com.example.fitjourney.ui.client.ai.AiCoachViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.ai.AiVoiceCallScreen(
                viewModel = viewModel,
                onEndCall = { navController.popBackStack() }
            )
        }

        composable(route = Screen.GenerateWorkoutPlan.route) {
            val viewModel: com.example.fitjourney.ui.client.workout.generator.WorkoutGeneratorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.workout.generator.WorkoutGeneratorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.WeeklyInsights.route) {
            val viewModel: com.example.fitjourney.ui.client.insights.WeeklyInsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.insights.WeeklyInsightsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ClientHabits.route) {
            val viewModel: com.example.fitjourney.ui.client.habits.HabitViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.client.habits.HabitScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin
        composable(route = Screen.AdminDashboard.route) {
            val viewModel: com.example.fitjourney.ui.admin.AdminDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            var shouldLogout by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            androidx.compose.runtime.LaunchedEffect(shouldLogout) {
                if (shouldLogout) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            com.example.fitjourney.ui.admin.AdminDashboardScreen(
                viewModel = viewModel,
                onNavigateToApi = { navController.navigate(Screen.AdminApiManagement.route) },
                onNavigateToAdmins = { navController.navigate(Screen.AdminManagement.route) },
                onNavigateToProfile = { navController.navigate(Screen.AdminProfile.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.FirebaseDiagnostic.route) },
                onLogout = { shouldLogout = true }
            )
        }
        composable(route = Screen.AdminApiManagement.route) {
            val viewModel: com.example.fitjourney.ui.admin.api.AdminApiManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.admin.api.AdminApiManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AdminManagement.route) {
            val viewModel: com.example.fitjourney.ui.admin.management.AdminManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.admin.management.AdminManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AdminProfile.route) {
            val viewModel: com.example.fitjourney.ui.admin.AdminProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = AppViewModelProvider.Factory
            )
            com.example.fitjourney.ui.admin.AdminProfileScreen(
                viewModel = viewModel,
                onNavIconClick = { navController.popBackStack() }
            )
        }
        composable(route = Screen.FirebaseDiagnostic.route) {
            val viewModel: com.example.fitjourney.ui.admin.diagnostic.FirebaseDiagnosticViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory)
            com.example.fitjourney.ui.admin.diagnostic.FirebaseDiagnosticScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
