package com.example.fitjourney.domain.repository

import com.example.fitjourney.domain.model.User
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

    suspend fun isAdminEmail(email: String): Boolean
    fun isLoggedIn(): Boolean
    fun getCurrentUserEmail(): String?
    suspend fun changeEmail(newEmail: String, currentPassword: String): Result<Unit>
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit>
    suspend fun reAuthenticate(email: String, password: String): Result<Unit>
    suspend fun recoverAccount(email: String, password: String): Result<User>
}
