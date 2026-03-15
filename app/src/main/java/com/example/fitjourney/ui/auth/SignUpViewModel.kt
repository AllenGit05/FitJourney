package com.example.fitjourney.ui.auth

import androidx.lifecycle.ViewModel
import com.example.fitjourney.domain.repository.AuthRepository
import com.example.fitjourney.domain.model.User
import com.example.fitjourney.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val adminConfig: com.example.fitjourney.data.local.AdminConfig
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _dob = MutableStateFlow("")
    val dob: StateFlow<String> = _dob.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _height = MutableStateFlow("")
    val height: StateFlow<String> = _height.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _goalWeight = MutableStateFlow("")
    val goalWeight: StateFlow<String> = _goalWeight.asStateFlow()

    private val _activityLevel = MutableStateFlow("")
    val activityLevel: StateFlow<String> = _activityLevel.asStateFlow()

    private val _foodType = MutableStateFlow("")
    val foodType: StateFlow<String> = _foodType.asStateFlow()

    private val _fitnessGoal = MutableStateFlow("")
    val fitnessGoal: StateFlow<String> = _fitnessGoal.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _backupPin = MutableStateFlow("")
    val backupPin: StateFlow<String> = _backupPin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateField(field: (String) -> Unit, value: String) {
        field(value)
    }

    // Setters
    fun setUsername(v: String) { _username.value = v; _errorMessage.value = null }
    fun setEmail(v: String) { _email.value = v; _errorMessage.value = null }
    fun setDob(v: String) { _dob.value = v }
    fun setGender(v: String) { _gender.value = v }
    fun setHeight(v: String) { _height.value = v }
    fun setWeight(v: String) { _weight.value = v }
    fun setGoalWeight(v: String) { _goalWeight.value = v }
    fun setActivityLevel(v: String) { _activityLevel.value = v }
    fun setFoodType(v: String) { _foodType.value = v }
    fun setFitnessGoal(v: String) { _fitnessGoal.value = v }
    fun setPassword(v: String) { _password.value = v; _errorMessage.value = null }
    fun setConfirmPassword(v: String) { _confirmPassword.value = v; _errorMessage.value = null }
    fun setBackupPin(v: String) { _backupPin.value = v }

    fun signUp(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_password.value != _confirmPassword.value) {
            _errorMessage.value = "Passwords do not match."
            onError("Passwords do not match.")
            return
        }
        if (_password.value.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            onError("Password must be at least 6 characters.")
            return
        }
        if (_email.value.isBlank() || _username.value.isBlank()) {
            _errorMessage.value = "Email and Username are required."
            onError("Email and Username are required.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Block registration with admin email
            val isAdmin = adminConfig.isAdminEmail(_email.value)
            if (isAdmin) {
                val msg = "This email is reserved for administration. Please use a different email."
                _errorMessage.value = msg
                onError(msg)
                _isLoading.value = false
                return@launch
            }

            val calculatedCalories = calculateCalorieGoal()

            val user = User(
                uid = "mock-uid",
                email = _email.value,
                username = _username.value,
                role = UserRole.CLIENT,
                gender = _gender.value,
                dob = _dob.value,
                height = _height.value,
                weight = _weight.value,
                goalWeight = _goalWeight.value,
                activityLevel = _activityLevel.value,
                foodType = _foodType.value,
                fitnessGoal = _fitnessGoal.value,
                calorieGoal = calculatedCalories,
                aiCredits = 20
            )

            val result = authRepository.signUpClient(user, _password.value)
            _isLoading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                val message = exception.message ?: "Signup failed. Please try again."
                _errorMessage.value = message
                onError(message)
            }
        }
    }

    fun previewCalorieGoal(): Int = calculateCalorieGoal()

    private fun calculateCalorieGoal(): Int {
        return try {
            val weightKg = _weight.value.toDouble()
            val heightCm = _height.value.toDouble()

            // Calculate age from dob (format: dd/MM/yyyy)
            val parts = _dob.value.split("/")
            val birthYear = parts[2].toInt()
            val birthMonth = parts[1].toInt()
            val birthDay = parts[0].toInt()
            val today = java.util.Calendar.getInstance()
            var age = today.get(java.util.Calendar.YEAR) - birthYear
            if (today.get(java.util.Calendar.MONTH) + 1 < birthMonth ||
                (today.get(java.util.Calendar.MONTH) + 1 == birthMonth &&
                 today.get(java.util.Calendar.DAY_OF_MONTH) < birthDay)) {
                age--
            }

            // Mifflin-St Jeor BMR
            val bmr = if (_gender.value == "Male") {
                (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
            } else {
                (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
            }

            // Activity multiplier
            val tdee = bmr * when (_activityLevel.value) {
                "Sedentary"  -> 1.2
                "Low"        -> 1.375
                "Moderate"   -> 1.55
                "High"       -> 1.725
                "Very High"  -> 1.9
                else         -> 1.55
            }

            // Adjust based on fitness goal
            val calorieGoal = when (_fitnessGoal.value) {
                "Fat Loss"     -> tdee - 500   // 500 kcal deficit
                "Muscle Gain"  -> tdee + 300   // 300 kcal surplus
                "Recomp"       -> tdee         // maintenance
                "Maintain"     -> tdee         // maintenance
                else           -> tdee
            }

            calorieGoal.toInt().coerceIn(1200, 4000)
        } catch (e: Exception) {
            2000 // safe fallback
        }
    }
}
