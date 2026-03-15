package com.example.fitjourneyag.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUpClient : Screen("signup_client")
    object ForgotPassword : Screen("forgot_password")
    // Navigation Containers
    object Main : Screen("main")
    
    // Client Routes
    object ClientDashboard : Screen("client_dashboard")
    object ClientTrack : Screen("client_track")
    object ClientDiet : Screen("client_diet")
    object ClientWorkout : Screen("client_workout")
    object ClientProgress : Screen("client_progress")
    object WeightProgress : Screen("weight_progress")
    object ProgressPhotos : Screen("progress_photos")
    object StrengthProgress : Screen("strength_progress")
    object BodyMeasurements : Screen("body_measurements")
    object ClientSettings : Screen("client_settings")
    object AiCoach : Screen("ai_coach")
    object AiVoiceCall : Screen("ai_voice_call")
    object BmiCalculator : Screen("bmi_calculator")
    object TdeeCalculator : Screen("tdee_calculator")
    object MacroCalculator : Screen("macro_calculator")
    object GenerateWorkoutPlan : Screen("generate_workout_plan")
    object WeeklyInsights : Screen("weekly_insights")
    object ClientHabits : Screen("client_habits")

    // Admin Routes
    object AdminDashboard : Screen("admin_dashboard")
    object AdminApiManagement : Screen("admin_api_management")
    object AdminManagement : Screen("admin_management")
}
