package com.example.fitjourneyag.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
            val viewModel: com.example.fitjourneyag.ui.auth.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.auth.LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignUpClient.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        com.example.fitjourneyag.domain.model.UserRole.CLIENT -> Screen.Main.route
                        com.example.fitjourneyag.domain.model.UserRole.ADMIN -> Screen.AdminDashboard.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        // ... (SignUp and ForgotPassword remain same)
        composable(route = Screen.SignUpClient.route) {
            val viewModel: com.example.fitjourneyag.ui.auth.SignUpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.auth.SignUpScreen(
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
            val viewModel: com.example.fitjourneyag.ui.auth.ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.auth.ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = { 
                    navController.popBackStack()
                }
            )
        }

        // Client Main Container
        composable(route = Screen.Main.route) {
            com.example.fitjourneyag.ui.navigation.MainScreen(
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
        composable(route = Screen.ClientDashboard.route) {
            // Handled inside MainScreen
        }
        // ... (Diet, Workout, Calculators, Progress, Settings remain same)
        composable(route = Screen.ClientDiet.route) {
            val viewModel: com.example.fitjourneyag.ui.client.diet.DietTrackingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.diet.DietTrackingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.ClientWorkout.route) {
            val viewModel: com.example.fitjourneyag.ui.client.workout.WorkoutTrackingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.workout.WorkoutTrackingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.BmiCalculator.route) {
            val viewModel: com.example.fitjourneyag.ui.client.calculators.CalculatorsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.calculators.BmiCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.TdeeCalculator.route) {
            val viewModel: com.example.fitjourneyag.ui.client.calculators.CalculatorsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.calculators.TdeeCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.MacroCalculator.route) {
            val viewModel: com.example.fitjourneyag.ui.client.calculators.CalculatorsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.calculators.MacroCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.ClientProgress.route) {
            // Handled inside MainScreen
        }
        composable(route = Screen.WeightProgress.route) {
            val viewModel: com.example.fitjourneyag.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.progress.WeightProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.ProgressPhotos.route) {
            val viewModel: com.example.fitjourneyag.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.progress.ProgressPhotosScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.StrengthProgress.route) {
            val viewModel: com.example.fitjourneyag.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.progress.StrengthProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.BodyMeasurements.route) {
            val viewModel: com.example.fitjourneyag.ui.client.progress.ProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.progress.BodyMeasurementsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.ClientSettings.route) {
            val viewModel: com.example.fitjourneyag.ui.client.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.settings.SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // AI Coach
        composable(route = Screen.AiCoach.route) {
             val viewModel: com.example.fitjourneyag.ui.client.ai.AiCoachViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.ai.AiCoachScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVoiceCall = { navController.navigate(Screen.AiVoiceCall.route) }
            )
        }
        
        composable(route = Screen.AiVoiceCall.route) {
            val viewModel: com.example.fitjourneyag.ui.client.ai.AiCoachViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.ai.AiVoiceCallScreen(
                viewModel = viewModel,
                onEndCall = { navController.popBackStack() }
            )
        }

        composable(route = Screen.GenerateWorkoutPlan.route) {
            val viewModel: com.example.fitjourneyag.ui.client.workout.generator.WorkoutGeneratorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.workout.generator.WorkoutGeneratorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.WeeklyInsights.route) {
            val viewModel: com.example.fitjourneyag.ui.client.insights.WeeklyInsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.insights.WeeklyInsightsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ClientHabits.route) {
            val viewModel: com.example.fitjourneyag.ui.client.habits.HabitViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.client.habits.HabitScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin
        composable(route = Screen.AdminDashboard.route) {
            val viewModel: com.example.fitjourneyag.ui.admin.AdminDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.admin.AdminDashboardScreen(
                viewModel = viewModel,
                onNavigateToApi = { navController.navigate(Screen.AdminApiManagement.route) },
                onNavigateToAdmins = { navController.navigate(Screen.AdminManagement.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.AdminApiManagement.route) {
            val viewModel: com.example.fitjourneyag.ui.admin.api.AdminApiManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.admin.api.AdminApiManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AdminManagement.route) {
            val viewModel: com.example.fitjourneyag.ui.admin.management.AdminManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.fitjourneyag.di.AppViewModelProvider.Factory
            )
            com.example.fitjourneyag.ui.admin.management.AdminManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
