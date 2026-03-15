package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.local.dao.UserDao
import com.example.fitjourneyag.data.local.entity.UserEntity
import com.example.fitjourneyag.domain.model.User
import com.example.fitjourneyag.domain.model.UserRole
import com.example.fitjourneyag.domain.repository.UserRepository
import com.example.fitjourneyag.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import java.util.*

class UserRepositoryImpl(
    private val authRepository: AuthRepository,
    private val userDao: UserDao
) : UserRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val userProfile: StateFlow<User?> = userDao.getUserById("current_user")
        .map { entity -> entity?.toDomain() }
        .stateIn(repositoryScope, SharingStarted.Eagerly, null)

    init {
        // Observe auth state to sync user profile and save to Room
        // Use a background scope specifically for DB sync
        CoroutineScope(Dispatchers.IO).launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    // Check local DB first via DAO instead of StateFlow value to be sure
                    userDao.insertUser(user.toEntity("current_user"))
                    checkAndResetCredits(user)
                } else {
                    // Seed a prototype user if none exists for instant use
                    seedUser()
                }
            }
        }
    }

    private suspend fun seedUser() {
        val current = userDao.getUserById("current_user").first()
        if (current == null) {
            val defaultUser = User(
                uid = "current_user",
                email = "allen@fitjourney.com",
                role = UserRole.CLIENT,
                username = "Allen",
                gender = "Male",
                dob = "01/01/1995",
                height = "180",
                weight = "75",
                goalWeight = "72",
                activityLevel = "Moderate",
                fitnessGoal = "Maintain Weight",
                calorieGoal = 2500,
                stepGoal = 10000,
                waterGoal = 2500,
                coachPersona = "Aurora"
            )
            val entity = defaultUser.toEntity("current_user")
            userDao.insertUser(entity)
            checkAndResetCredits(defaultUser)
        }
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
        userDao.insertUser(user.toEntity("current_user"))
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
