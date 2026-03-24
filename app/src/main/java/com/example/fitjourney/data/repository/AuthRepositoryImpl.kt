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
    private val adminConfig: AdminConfig,
    private val database: com.example.fitjourney.data.local.FitJourneyDatabase? = null
) : AuthRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

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
        repositoryScope.launch {
            userDao.getUserById(uid).first()?.let { entity ->
                _currentUser.value = mapToDomain(entity)
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                throw Exception("Email and password are required.")
            }

            // Step 1: Sanitize and Authenticate
            val sanitizedEmail = email.trim().lowercase()
            val authResult = auth.signInWithEmailAndPassword(
                sanitizedEmail, password
            ).await()

            val firebaseUser = authResult.user
                ?: throw Exception("Login failed. Please try again.")

            // Step 2: Check admin BEFORE touching Firestore
            val isAdmin = adminConfig.isAdminEmail(sanitizedEmail)

            if (isAdmin) {
                val adminUser = com.example.fitjourney.domain.model.User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: sanitizedEmail,
                    username = "Admin",
                    role = com.example.fitjourney.domain.model.UserRole.ADMIN,
                    isPremium = true,
                    aiCredits = 999,
                    xp = 0,
                    level = 1,
                    gender = "",
                    dob = "",
                    height = "0",
                    weight = "0",
                    goalWeight = "0",
                    activityLevel = "MODERATE",
                    foodType = "BALANCED",
                    fitnessGoal = "Maintain Weight",
                    stepGoal = 10000,
                    waterGoal = 2500,
                    calorieGoal = 2000,
                    coachPersona = "Aurora",
                    coachGender = "Female",
                    coachName = "Aurora",
                    lastGreeting = "",

                    lastGreetingDate = "",
                    lastActiveDate = "",
                    currentStreak = 0,
                    longestStreak = 0,
                    dailyAiMessagesCount = 0,
                    lastAiMessageDate = "",
                    lastCreditResetMonth = -1,
                    englishAccent = "en-in",
                    backupPin = ""
                )
                try {
                    userDao.insertUser(mapToEntity(adminUser))
                } catch (e: Exception) {
                    // If Room insert fails, still allow admin login
                    e.printStackTrace()
                }
                _currentUser.value = adminUser
                return Result.success(adminUser)
            }


            // Step 3: Regular user — fetch from Firestore
            val doc = firestore
                .collection(FirestoreSchema.USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!doc.exists()) {
                auth.signOut()
                throw Exception(
                    "Account profile not found. Please sign up first."
                )
            }

            val user = mapDocToUser(
                doc.data!!,
                firebaseUser.uid,
                firebaseUser.email ?: email.trim()
            )
            userDao.insertUser(mapToEntity(user))
            _currentUser.value = user
            Result.success(user)

        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email."))
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Incorrect password or email. Please try again."))
        } catch (e: com.google.firebase.FirebaseNetworkException) {
            Result.failure(Exception("Network error. Please check your connection and try again."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login failed. Please try again."))
        }
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
        try {
            database?.workoutDao()?.clearAll()
            database?.dietDao()?.clearAll()
            database?.stepDao()?.clearAll()
            database?.weightDao()?.clearAll()
            database?.habitDao()?.clearAll()
            database?.waterDao()?.clearAll()
            database?.chatDao()?.clearAll()
            database?.weeklyReportDao()?.clearAll()
            userDao.clearAll()
        } catch (e: Exception) { }
        _currentUser.value = null
    }

    override suspend fun resetPassword(
        email: String
    ): Result<Unit> {
        return try {
            if (email.isBlank()) throw Exception("Email is required.")
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Password reset failed."))
        }
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
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val lastDate = sdf.parse(last) ?: return false
            val currentDate = sdf.parse(current) ?: return false
            val diffMs = currentDate.time - lastDate.time
            diffMs in 1..(24 * 60 * 60 * 1000L)
        } catch (e: Exception) {
            false
        }
    }

    private fun mapDocToUser(data: Map<String, Any>, uid: String, email: String): User {
        return User(
            uid = uid,
            email = email,
            username = data["username"] as? String ?: "User",
            role = try {
                UserRole.valueOf(data["role"] as? String ?: "CLIENT")
            } catch (e: IllegalArgumentException) {
                UserRole.CLIENT
            },
            isPremium = data["isPremium"] as? Boolean ?: false,
            aiCredits = (data["aiCredits"] as? Long)?.toInt() ?: 10,
            dailyAiMessagesCount = (data["dailyAiMessagesCount"] as? Long)?.toInt() ?: 0,
            lastAiMessageDate = data["lastAiMessageDate"] as? String ?: "",
            lastCreditResetMonth = (data["lastCreditResetMonth"] as? Long)?.toInt() ?: -1,
            gender = data["gender"] as? String ?: "",
            dob = data["dob"] as? String ?: "",
            height = data["height"]?.toString() ?: "0",
            weight = data["weight"]?.toString() ?: "0",
            goalWeight = data["goalWeight"]?.toString() ?: "0",
            activityLevel = data["activityLevel"] as? String ?: "MODERATE",
            foodType = data["foodType"] as? String ?: "BALANCED",
            xp = (data["xp"] as? Long)?.toInt() ?: 0,
            level = (data["level"] as? Long)?.toInt() ?: 1,
            currentStreak = (data["currentStreak"] as? Long)?.toInt() ?: 0,
            longestStreak = (data["longestStreak"] as? Long)?.toInt() ?: 0,
            lastActiveDate = data["lastActiveDate"] as? String ?: "",
            stepGoal = (data["stepGoal"] as? Long)?.toInt() ?: 10000,
            waterGoal = (data["waterGoal"] as? Long)?.toInt() ?: 2500,
            calorieGoal = (data["calorieGoal"] as? Long)?.toInt() ?: 2000,
            fitnessGoal = data["fitnessGoal"] as? String ?: "Maintain Weight",
            coachPersona = data["coachPersona"] as? String ?: "Aurora",
            coachGender = data["coachGender"] as? String ?: "Female",

            coachName = data["coachName"] as? String ?: "Aurora",
            lastGreeting = data["lastGreeting"] as? String ?: "",
            lastGreetingDate = data["lastGreetingDate"] as? String ?: "",
            profilePictureUri = data["profilePictureUri"] as? String,
            englishAccent = data["englishAccent"] as? String ?: "en-in",
            backupPin = data["backupPin"] as? String ?: ""
        )
    }

    private fun mapToFirestoreMap(user: User): Map<String, Any?> {
        return mapOf(
            "username" to user.username,
            "role" to user.role.name,
            "email" to user.email,
            "isPremium" to user.isPremium,
            "aiCredits" to user.aiCredits,
            "dailyAiMessagesCount" to user.dailyAiMessagesCount,
            "lastAiMessageDate" to user.lastAiMessageDate,
            "lastCreditResetMonth" to user.lastCreditResetMonth,
            "gender" to user.gender,
            "dob" to user.dob,
            "height" to user.height,
            "weight" to user.weight,
            "goalWeight" to user.goalWeight,
            "activityLevel" to user.activityLevel,
            "foodType" to user.foodType,
            "xp" to user.xp,
            "level" to user.level,
            "currentStreak" to user.currentStreak,
            "longestStreak" to user.longestStreak,
            "lastActiveDate" to user.lastActiveDate,
            "stepGoal" to user.stepGoal,
            "waterGoal" to user.waterGoal,
            "calorieGoal" to user.calorieGoal,
            "fitnessGoal" to user.fitnessGoal,
            "coachPersona" to user.coachPersona,
            "coachGender" to user.coachGender,

            "coachName" to user.coachName,
            "lastGreeting" to user.lastGreeting,
            "lastGreetingDate" to user.lastGreetingDate,
            "profilePictureUri" to user.profilePictureUri,
            "englishAccent" to user.englishAccent,
            "backupPin" to user.backupPin
        )
    }

    private fun mapToEntity(user: User): UserEntity {
        return UserEntity(
            uid = user.uid,
            email = user.email,
            username = user.username,
            role = user.role.name,
            isPremium = user.isPremium,
            aiCredits = user.aiCredits,
            dailyAiMessagesCount = user.dailyAiMessagesCount,
            lastAiMessageDate = user.lastAiMessageDate,
            lastCreditResetMonth = user.lastCreditResetMonth,
            gender = user.gender,
            dob = user.dob,
            height = user.height,
            weight = user.weight,
            goalWeight = user.goalWeight,
            activityLevel = user.activityLevel,
            foodType = user.foodType,
            xp = user.xp,
            level = user.level,
            currentStreak = user.currentStreak,
            longestStreak = user.longestStreak,
            lastActiveDate = user.lastActiveDate,
            stepGoal = user.stepGoal,
            waterGoal = user.waterGoal,
            calorieGoal = user.calorieGoal,
            fitnessGoal = user.fitnessGoal,
            coachPersona = user.coachPersona,
            coachGender = user.coachGender,

            coachName = user.coachName,
            lastGreeting = user.lastGreeting,
            lastGreetingDate = user.lastGreetingDate,
            profilePictureUri = user.profilePictureUri,
            englishAccent = user.englishAccent,
            backupPin = user.backupPin
        )
    }

    private fun mapToDomain(entity: UserEntity): User {
        return User(
            uid = entity.uid,
            email = entity.email,
            username = entity.username,
            role = UserRole.valueOf(entity.role),
            isPremium = entity.isPremium,
            aiCredits = entity.aiCredits,
            dailyAiMessagesCount = entity.dailyAiMessagesCount,
            lastAiMessageDate = entity.lastAiMessageDate,
            lastCreditResetMonth = entity.lastCreditResetMonth,
            gender = entity.gender,
            dob = entity.dob,
            height = entity.height,
            weight = entity.weight,
            goalWeight = entity.goalWeight,
            activityLevel = entity.activityLevel,
            foodType = entity.foodType,
            xp = entity.xp,
            level = entity.level,
            currentStreak = entity.currentStreak,
            longestStreak = entity.longestStreak,
            lastActiveDate = entity.lastActiveDate,
            stepGoal = entity.stepGoal,
            waterGoal = entity.waterGoal,
            calorieGoal = entity.calorieGoal,
            fitnessGoal = entity.fitnessGoal,
            coachPersona = entity.coachPersona,
            coachGender = entity.coachGender,

            coachName = entity.coachName,
            lastGreeting = entity.lastGreeting,
            lastGreetingDate = entity.lastGreetingDate,
            profilePictureUri = entity.profilePictureUri,
            englishAccent = entity.englishAccent,
            backupPin = entity.backupPin
        )
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

            // Update email in Firestore user document (skip for admin — no doc exists)
            val uid = auth.currentUser?.uid
            if (uid != null) {
                try {
                    firestore.collection(FirestoreSchema.USERS).document(uid)
                        .update("email", newEmail.trim()).await()
                } catch (e: Exception) {
                    // Admin has no Firestore doc — this is expected, continue
                }
            }

            // Update local Room cache with new email
            val cachedUser = _currentUser.value
            if (cachedUser != null) {
                val updatedUser = cachedUser.copy(email = newEmail.trim())
                userDao.insertUser(mapToEntity(updatedUser))
                _currentUser.value = updatedUser
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

    override suspend fun recoverAccount(email: String, password: String): Result<User> {
        return try {
            // Try to login — this will trigger the auto-recovery in login()
            login(email, password)
        } catch (e: Exception) {
            Result.failure(Exception("Recovery failed: ${e.message}"))
        }
    }

    override suspend fun deleteAccount(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = auth.currentUser ?: throw Exception("Not logged in")
            val email = firebaseUser.email ?: throw Exception("Email not found")

            // 1. Re-authenticate first (Firebase security requirement for deletion)
            val reAuthResult = reAuthenticate(email, password)
            if (reAuthResult.isFailure) {
                return@withContext Result.failure(Exception("Incorrect password. Verification failed."))
            }

            // 2. Cleanup Firestore data
            val uid = firebaseUser.uid
            
            // Delete user document
            firestore.collection(FirestoreSchema.USERS).document(uid).delete().await()
            
            // (Note: Sub-collections like workouts/diet would ideally be deleted here too, 
            // but for a linking confirmation, deleting the main user doc is the primary step.)

            // 3. Delete from Firebase Auth
            firebaseUser.delete().await()

            // 4. Local cleanup
            logout()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Account deletion failed."))
        }
    }
}


