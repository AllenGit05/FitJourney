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
}
