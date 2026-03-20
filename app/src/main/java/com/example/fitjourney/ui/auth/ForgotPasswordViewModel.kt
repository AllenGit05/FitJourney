package com.example.fitjourney.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: com.example.fitjourney.domain.repository.AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _backupPin = MutableStateFlow("")
    val backupPin: StateFlow<String> = _backupPin.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmNewPassword = MutableStateFlow("")
    val confirmNewPassword: StateFlow<String> = _confirmNewPassword.asStateFlow()

    fun setEmail(value: String) { _email.value = value }
    fun setBackupPin(value: String) { _backupPin.value = value }
    fun setNewPassword(value: String) { _newPassword.value = value }
    fun setConfirmNewPassword(value: String) { _confirmNewPassword.value = value }

    fun resetPassword(onSuccess: () -> Unit) {
        if (_email.value.isBlank()) return
        if (_newPassword.value != _confirmNewPassword.value) return 

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.resetPassword(
                    email = _email.value.trim(),
                    backupPin = _backupPin.value,
                    newPassword = _newPassword.value
                )
                _isLoading.value = false
                if (result.isSuccess) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
