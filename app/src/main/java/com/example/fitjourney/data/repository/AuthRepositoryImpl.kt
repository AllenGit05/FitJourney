package com.example.fitjourney.data.repository

import android.content.Context
import com.example.fitjourney.data.local.AdminConfig
import com.example.fitjourney.data.local.dao.UserDao
import com.example.fitjourney.data.local.entity.UserEntity
import com.example.fitjourney.data.remote.FirestoreSchema
import com.example.fitjourney.domain.model.User
import com.example.fitjourney.domain.model.UserRole
import com.example.fitjourney.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val adminConfig: AdminConfig
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                _currentUser.value = null
            } else {
                // We'll update the flow when profile is fetched or from local cache
                loadUserFromCache(firebaseUser.uid)
            }
        }
    }

    private fun loadUserFromCache(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.getUserById(uid).first()?.let { entity ->
                _currentUser.value = mapToDomain(entity)
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User not found after login")
            
            val doc = firestore.collection(FirestoreSchema.USERS).document(firebaseUser.uid).get().await()
            if (!doc.exists()) throw Exception("Profile not found in Firestore")
            
            val user = mapDocToUser(doc.data!!, firebaseUser.uid, firebaseUser.email!!)
            userDao.insertUser(mapToEntity(user))
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun signUpClient(user: User, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Signup failed")
            val newUser = user.copy(uid = firebaseUser.uid, role = UserRole.CLIENT)
            
            firestore.collection(FirestoreSchema.USERS).document(firebaseUser.uid)
                .set(mapToFirestoreMap(newUser)).await()
            
            userDao.insertUser(mapToEntity(newUser))
            _currentUser.value = newUser
            Result.success(newUser)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun logout() {
        auth.signOut()
        userDao.clearAll()
        _currentUser.value = null
    }

    override suspend fun resetPassword(email: String, backupPin: String, newPassword: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection(FirestoreSchema.USERS).document(user.uid)
                .set(mapToFirestoreMap(user)).await()
            userDao.insertUser(mapToEntity(user))
            _currentUser.value = user
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun recordDailyActivity() {
        _currentUser.value?.let { user ->
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            if (user.lastActiveDate != today) {
                val updatedUser = user.copy(
                    lastActiveDate = today,
                    currentStreak = if (isConsecutive(user.lastActiveDate, today)) user.currentStreak + 1 else 1,
                    xp = user.xp + 10
                )
                updateUserProfile(updatedUser)
            }
        }
    }

    override suspend fun grantXp(amount: Int) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(xp = user.xp + amount)
            updateUserProfile(updatedUser)
        }
    }

    private fun isConsecutive(last: String, current: String): Boolean {
        // Simplified Logic
        return true 
    }

    private fun mapDocToUser(data: Map<String, Any>, uid: String, email: String): User {
        return User(
            uid = uid,
            email = email,
            username = data["username"] as? String ?: "User",
            role = UserRole.valueOf(data["role"] as? String ?: "CLIENT"),
            isPremium = data["isPremium"] as? Boolean ?: false,
            gender = data["gender"] as? String ?: "",
            dob = data["dob"] as? String ?: "",
            height = data["height"]?.toString() ?: "0",
            weight = data["weight"]?.toString() ?: "0",
            goalWeight = data["goalWeight"]?.toString() ?: "0",
            fitnessGoal = data["fitnessGoal"] as? String ?: ""
        )
    }

    private fun mapToFirestoreMap(user: User): Map<String, Any> {
        return mapOf(
            "username" to user.username,
            "role" to user.role.name,
            "email" to user.email,
            "isPremium" to user.isPremium,
            "gender" to user.gender,
            "dob" to user.dob,
            "height" to user.height,
            "weight" to user.weight,
            "goalWeight" to user.goalWeight,
            "fitnessGoal" to user.fitnessGoal,
            "xp" to user.xp,
            "level" to user.level
        )
    }

    private fun mapToEntity(user: User): UserEntity {
        return UserEntity(
            uid = user.uid, 
            email = user.email, 
            username = user.username, 
            role = user.role.name,
            isPremium = user.isPremium, 
            gender = user.gender, 
            dob = user.dob,
            height = user.height, 
            weight = user.weight, 
            goalWeight = user.goalWeight,
            fitnessGoal = user.fitnessGoal, 
            xp = user.xp, 
            level = user.level,
            currentStreak = user.currentStreak, 
            lastActiveDate = user.lastActiveDate,
            aiCredits = 10,
            dailyAiMessagesCount = 0,
            lastAiMessageDate = "",
            lastCreditResetMonth = 0,
            activityLevel = "MODERATE",
            foodType = "BALANCED",
            longestStreak = user.currentStreak,
            stepGoal = 10000,
            waterGoal = 2500,
            calorieGoal = 2000,
            coachPersona = "AURORA",
            customCoachPersona = "",
            coachGender = "FEMALE",
            coachName = "Aurora",
            lastGreeting = "",
            lastGreetingDate = ""
        )
    }

    private fun mapToDomain(entity: UserEntity): User {
        return User(
            uid = entity.uid, 
            email = entity.email, 
            username = entity.username,
            role = UserRole.valueOf(entity.role), 
            isPremium = entity.isPremium,
            gender = entity.gender, 
            dob = entity.dob, 
            height = entity.height,
            weight = entity.weight, 
            goalWeight = entity.goalWeight,
            fitnessGoal = entity.fitnessGoal, 
            xp = entity.xp, 
            level = entity.level,
            currentStreak = entity.currentStreak, 
            lastActiveDate = entity.lastActiveDate
        )
    }

    override suspend fun isAdminEmail(email: String): Boolean {
        return adminConfig.isAdminEmail(email)
    }

    override fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    // Re-authenticate before sensitive operations (required by Firebase)
    override suspend fun reAuthenticate(
        email: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.reauthenticate(credential)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Change email — requires re-auth first
    override suspend fun changeEmail(
        newEmail: String,
        currentPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentEmail = auth.currentUser?.email
                ?: return@withContext Result.failure(Exception("Not logged in"))

            // Re-authenticate first (Firebase security requirement)
            val reAuthResult = reAuthenticate(currentEmail, currentPassword)
            if (reAuthResult.isFailure) {
                return@withContext Result.failure(
                    Exception("Current password is incorrect")
                )
            }

            // Update email in Firebase Auth
            auth.currentUser?.updateEmail(newEmail.trim())?.await()

            // Save new admin email to AdminConfig DataStore
            adminConfig.saveAdminEmail(newEmail.trim())

            // Update email in Firestore user document
            val uid = auth.currentUser?.uid
            if (uid != null) {
                firestore.collection("users").document(uid)
                    .update("email", newEmail.trim()).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to change email: ${e.message}"))
        }
    }

    // Change password — requires re-auth first
    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentEmail = auth.currentUser?.email
                ?: return@withContext Result.failure(Exception("Not logged in"))

            // Re-authenticate first
            val reAuthResult = reAuthenticate(currentEmail, currentPassword)
            if (reAuthResult.isFailure) {
                return@withContext Result.failure(
                    Exception("Current password is incorrect")
                )
            }

            // Update password in Firebase Auth
            auth.currentUser?.updatePassword(newPassword)?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to change password: ${e.message}"))
        }
    }
}

