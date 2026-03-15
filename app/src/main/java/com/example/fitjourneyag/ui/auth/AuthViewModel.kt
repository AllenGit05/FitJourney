package com.example.fitjourneyag.ui.auth

import androidx.lifecycle.ViewModel
import com.example.fitjourneyag.domain.repository.AuthRepository
import com.example.fitjourneyag.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Bug Fix #1 & #4: Expose error state so the UI can display it
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue
        _errorMessage.value = null // clear error when user starts typing
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
        _errorMessage.value = null // clear error when user starts typing
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(onSuccess: (UserRole) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.login(_email.value, _password.value)
            _isLoading.value = false

            result.onSuccess { user ->
                onSuccess(user.role)
            }.onFailure { exception ->
                // Bug Fix #1: Surface the real error message instead of swallowing it
                val message = exception.message ?: "Login failed. Please try again."
                _errorMessage.value = message
                onError(message)
            }
        }
    }
}
