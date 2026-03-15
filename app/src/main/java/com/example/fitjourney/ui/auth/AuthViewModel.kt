package com.example.fitjourney.ui.auth

import androidx.lifecycle.ViewModel
import com.example.fitjourney.domain.repository.AuthRepository
import com.example.fitjourney.domain.model.UserRole
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigateToAdmin = MutableStateFlow(false)
    val navigateToAdmin: StateFlow<Boolean> = _navigateToAdmin.asStateFlow()

    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard: StateFlow<Boolean> = _navigateToDashboard.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue
        _errorMessage.value = null
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun onNavigationHandled() {
        _navigateToAdmin.value = false
        _navigateToDashboard.value = false
    }

    fun login(onSuccess: (UserRole) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.login(_email.value, _password.value)
            _isLoading.value = false

            result.onSuccess { user ->
                val isAdmin = authRepository.isAdminEmail(_email.value)
                _isAdmin.value = isAdmin
                if (isAdmin) {
                    _navigateToAdmin.value = true
                } else {
                    _navigateToDashboard.value = true
                }
                onSuccess(user.role)
            }.onFailure { exception ->
                val message = exception.message ?: "Login failed. Please try again."
                _errorMessage.value = message
                onError(message)
            }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun isAdminUser(): Boolean {
        // Return current admin status or check by role
        return _isAdmin.value || (authRepository.getCurrentUserEmail() != null && _navigateToAdmin.value)
    }
}
