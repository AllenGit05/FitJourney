package com.example.fitjourney.data.local.entity

import androidx.room.*
import com.example.fitjourney.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val role: String, // Store as String for enum compatibility
    val username: String,
    val isPremium: Boolean,
    val aiCredits: Int,
    val dailyAiMessagesCount: Int,
    val lastAiMessageDate: String,
    val lastCreditResetMonth: Int,
    val gender: String,
    val dob: String,
    val height: String,
    val weight: String,
    val goalWeight: String,
    val activityLevel: String,
    val foodType: String,
    val xp: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: String,
    val stepGoal: Int,
    val waterGoal: Int,
    val calorieGoal: Int,
    val fitnessGoal: String,
    val coachPersona: String,
    val customCoachPersona: String,
    val coachGender: String,
    val coachName: String,
    val lastGreeting: String,
    val lastGreetingDate: String,
    val profilePictureUri: String? = null,
    val englishAccent: String = "en-in",
    val isSynced: Boolean = false,
    val syncedAt: Long = 0L,
    val firestoreId: String = "",
    val isDeleted: Boolean = false
)
