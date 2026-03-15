package com.example.fitjourney.domain.repository

import com.example.fitjourney.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val userProfile: StateFlow<User?>
    suspend fun updateCredits(consumedCredits: Int): Boolean
    suspend fun refreshUser()
    suspend fun saveProfile(user: User)
    suspend fun updateProfilePicture(uri: String?)
}
