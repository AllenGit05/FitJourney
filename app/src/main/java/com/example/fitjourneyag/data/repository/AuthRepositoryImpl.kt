package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.domain.model.User
import com.example.fitjourneyag.domain.model.UserRole
import com.example.fitjourneyag.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Mock implementation of AuthRepository — no Firebase, no network.
 * Replace this with the real Firebase implementation when the backend is ready.
 */
class AuthRepositoryImpl(context: android.content.Context) : AuthRepository {
    private val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser

    init {
        // Restore session on init
        val savedEmail = prefs.getString("user_email", null)
        val savedRole = prefs.getString("user_role", null)
        if (savedEmail != null && savedRole != null) {
            val role = try { UserRole.valueOf(savedRole) } catch (e: Exception) { UserRole.CLIENT }
            _currentUser.value = User(
                uid = if (role == UserRole.ADMIN) "admin-uid-001" else "current_user",
                email = savedEmail,
                role = role,
                username = if (role == UserRole.ADMIN) "Admin" else "Allen"
            )
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        // Mock: specific credentials for allen and admin
        val role = when {
            email == "admin" && password == "admin" -> UserRole.ADMIN
            email == "allen" && password == "abcd" -> UserRole.CLIENT
            else -> return Result.failure(Exception("Invalid prototype credentials. Use allen/abcd or admin/admin."))
        }

        val mockUser = User(
            uid = if (role == UserRole.ADMIN) "admin-uid-001" else "current_user",
            email = email,
            role = role,
            username = if (role == UserRole.ADMIN) "Admin" else "Allen"
        )
        
        // Persist session
        prefs.edit()
            .putString("user_email", email)
            .putString("user_role", role.name)
            .apply()

        _currentUser.value = mockUser
        return Result.success(mockUser)
    }

    override suspend fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    override suspend fun signUpClient(user: User, password: String): Result<User> {
        // Mock: signup always succeeds
        val newUser = user.copy(uid = "mock-uid-${System.currentTimeMillis()}", role = UserRole.CLIENT)
        
        // Optionally auto-login on signup
        prefs.edit()
            .putString("user_email", user.email)
            .putString("user_role", UserRole.CLIENT.name)
            .apply()
        _currentUser.value = newUser
        
        return Result.success(newUser)
    }
    
    // ... rest of the implementation ...
    override suspend fun resetPassword(email: String, backupPin: String, newPassword: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        _currentUser.value = user
        return Result.success(Unit)
    }

    override suspend fun recordDailyActivity() {
        val user = _currentUser.value ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        
        if (user.lastActiveDate == todayStr) return
        
        var newStreak = user.currentStreak
        if (user.lastActiveDate.isNotEmpty()) {
            try {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(cal.time)
                
                if (user.lastActiveDate == yesterdayStr) {
                    newStreak += 1
                } else {
                    newStreak = 1
                }
            } catch (e: Exception) {
                newStreak = 1
            }
        } else {
            newStreak = 1
        }
        
        val longest = maxOf(user.longestStreak, newStreak)
        
        _currentUser.value = user.copy(
            currentStreak = newStreak,
            longestStreak = longest,
            lastActiveDate = todayStr
        )
    }

    override suspend fun grantXp(amount: Int) {
        val user = _currentUser.value ?: return
        val newXp = user.xp + amount
        var calculatedLevel = 1
        var xpRequired = 100
        var remainingXp = newXp
        
        // Simple level scaling: each level takes 100 * level XP
        while (remainingXp >= xpRequired) {
            remainingXp -= xpRequired
            calculatedLevel++
            xpRequired = calculatedLevel * 100
        }
        
        _currentUser.value = user.copy(
            xp = newXp,
            level = calculatedLevel
        )
    }
}
