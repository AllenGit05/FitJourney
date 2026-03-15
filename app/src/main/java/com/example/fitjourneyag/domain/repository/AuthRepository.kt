package com.example.fitjourneyag.domain.repository

import com.example.fitjourneyag.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUpClient(user: User, password: String): Result<User>
    suspend fun resetPassword(email: String, backupPin: String, newPassword: String): Result<Unit>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun logout()
    
    // Gamification
    suspend fun recordDailyActivity()
    suspend fun grantXp(amount: Int)
}
