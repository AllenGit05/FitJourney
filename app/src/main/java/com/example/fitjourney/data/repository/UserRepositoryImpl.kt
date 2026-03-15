package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.UserDao
import com.example.fitjourney.data.local.entity.UserEntity
import com.example.fitjourney.domain.model.User
import com.example.fitjourney.domain.model.UserRole
import com.example.fitjourney.domain.repository.UserRepository
import com.example.fitjourney.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import java.util.*

class UserRepositoryImpl(
    private val authRepository: AuthRepository,
    private val userDao: UserDao
) : UserRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val userProfile: StateFlow<User?> = authRepository.currentUser
        .onEach { user ->
            if (user != null) {
                userDao.insertUser(user.toEntity(user.uid))
            }
        }.stateIn(repositoryScope, SharingStarted.Eagerly, null)

    init {
        // Initialization logic moved to userProfile observation
    }


    override suspend fun updateCredits(consumedCredits: Int): Boolean {
        val current = userProfile.value ?: return false
        if (current.isPremium) return true
        
        if (current.aiCredits < consumedCredits) return false
        
        val updated = current.copy(aiCredits = current.aiCredits - consumedCredits)
        saveProfile(updated)
        return true
    }

    override suspend fun refreshUser() {
        val current = userProfile.value ?: return
        checkAndResetCredits(current)
    }

    override suspend fun saveProfile(user: User) {
        authRepository.updateUserProfile(user)
        userDao.insertUser(user.toEntity(user.uid))
    }

    override suspend fun updateProfilePicture(uri: String?) {
        val current = userProfile.value ?: return
        val updated = current.copy(profilePictureUri = uri)
        saveProfile(updated)
    }

    private fun checkAndResetCredits(user: User) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) // 0-11
        
        if (user.lastCreditResetMonth != currentMonth) {
            val updatedUser = user.copy(
                aiCredits = if (user.isPremium) user.aiCredits else 20,
                lastCreditResetMonth = currentMonth,
                dailyAiMessagesCount = 0
            )
            CoroutineScope(Dispatchers.IO).launch {
                saveProfile(updatedUser)
            }
        }
    }

    private fun UserEntity.toDomain(): User = User(
        uid = uid,
        email = email,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.CLIENT },
        username = username,
        isPremium = isPremium,
        aiCredits = aiCredits,
        dailyAiMessagesCount = dailyAiMessagesCount,
        lastAiMessageDate = lastAiMessageDate,
        lastCreditResetMonth = lastCreditResetMonth,
        gender = gender,
        dob = dob,
        height = height,
        weight = weight,
        goalWeight = goalWeight,
        activityLevel = activityLevel,
        foodType = foodType,
        xp = xp,
        level = level,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActiveDate = lastActiveDate,
        stepGoal = stepGoal,
        waterGoal = waterGoal,
        calorieGoal = calorieGoal,
        fitnessGoal = fitnessGoal,
        coachPersona = coachPersona,
        customCoachPersona = customCoachPersona,
        coachGender = coachGender,
        coachName = coachName,
        lastGreeting = lastGreeting,
        lastGreetingDate = lastGreetingDate,
        profilePictureUri = profilePictureUri
    )

    private fun User.toEntity(id: String): UserEntity = UserEntity(
        uid = id,
        email = email,
        role = role.name,
        username = username,
        isPremium = isPremium,
        aiCredits = aiCredits,
        dailyAiMessagesCount = dailyAiMessagesCount,
        lastAiMessageDate = lastAiMessageDate,
        lastCreditResetMonth = lastCreditResetMonth,
        gender = gender,
        dob = dob,
        height = height,
        weight = weight,
        goalWeight = goalWeight,
        activityLevel = activityLevel,
        foodType = foodType,
        xp = xp,
        level = level,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActiveDate = lastActiveDate,
        stepGoal = stepGoal,
        waterGoal = waterGoal,
        calorieGoal = calorieGoal,
        fitnessGoal = fitnessGoal,
        coachPersona = coachPersona,
        customCoachPersona = customCoachPersona,
        coachGender = coachGender,
        coachName = coachName,
        lastGreeting = lastGreeting,
        lastGreetingDate = lastGreetingDate,
        profilePictureUri = profilePictureUri
    )
}
