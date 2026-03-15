package com.example.fitjourneyag.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _backupPin = MutableStateFlow("")
    val backupPin: StateFlow<String> = _backupPin.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmNewPassword = MutableStateFlow("")
    val confirmNewPassword: StateFlow<String> = _confirmNewPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setEmail(v: String) { _email.value = v }
    fun setBackupPin(v: String) { _backupPin.value = v }
    fun setNewPassword(v: String) { _newPassword.value = v }
    fun setConfirmNewPassword(v: String) { _confirmNewPassword.value = v }

    fun resetPassword(onSuccess: () -> Unit) {
        _isLoading.value = true
        // TODO: Implement actual firebase reset password
        _isLoading.value = false
        onSuccess()
    }
}
