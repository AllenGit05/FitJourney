package com.example.fitjourneyag.domain.model

enum class UserRole {
    CLIENT, ADMIN, UNKNOWN
}

data class User(
    val uid: String = "",
    val email: String = "",
    val role: UserRole = UserRole.UNKNOWN,
    val username: String = "",
    val isPremium: Boolean = false,
    val aiCredits: Int = 0,
    val dailyAiMessagesCount: Int = 0,
    val lastAiMessageDate: String = "", // Format: "YYYY-MM-DD"
    val lastCreditResetMonth: Int = -1, // 1-12 (Calendar.MONTH)
    // User Bio & Stats
    val gender: String = "",
    val dob: String = "",
    val height: String = "",
    val weight: String = "",
    val goalWeight: String = "",
    val activityLevel: String = "",
    val foodType: String = "",
    // Gamification Stats
    val xp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "", // Format: "YYYY-MM-DD"
    // Goals
    val stepGoal: Int = 10000,
    val waterGoal: Int = 2500,
    val calorieGoal: Int = 2000,
    val fitnessGoal: String = "Maintain Weight", // Lose Weight, Maintain Weight, Gain Weight
    // AI Coach Customization
    val coachPersona: String = "Aurora", // Aurora, Rex, Zen, Custom
    val customCoachPersona: String = "",
    val coachGender: String = "Female", // Male, Female
    val coachName: String = "Aurora",
    val lastGreeting: String = "",
    val lastGreetingDate: String = "", // Format: "YYYY-MM-DD"
    val profilePictureUri: String? = null
)
