package com.example.fitjourney.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.data.local.AdminConfig
import com.example.fitjourney.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminProfileUiState(
    // Change Email
    val currentEmailDisplay: String = "",
    val newEmail: String = "",
    val emailCurrentPassword: String = "",
    val isChangingEmail: Boolean = false,
    val emailSuccess: String? = null,
    val emailError: String? = null,

    // Change Password
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isChangingPassword: Boolean = false,
    val passwordSuccess: String? = null,
    val passwordError: String? = null,

    // Visibility toggles
    val showEmailCurrentPassword: Boolean = false,
    val showCurrentPassword: Boolean = false,
    val showNewPassword: Boolean = false,
    val showConfirmPassword: Boolean = false
)

class AdminProfileViewModel(
    private val authRepository: AuthRepository,
    private val adminConfig: AdminConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentEmail()
    }

    private fun loadCurrentEmail() {
        viewModelScope.launch {
            val email = authRepository.getCurrentUserEmail() ?: ""
            _uiState.update { it.copy(currentEmailDisplay = email) }
        }
    }

    // ── Change Email ─────────────────────────────────────────────

    fun onNewEmailChanged(value: String) {
        _uiState.update { it.copy(newEmail = value, emailError = null, emailSuccess = null) }
    }

    fun onEmailCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(emailCurrentPassword = value, emailError = null) }
    }

    fun toggleEmailPasswordVisibility() {
        _uiState.update { it.copy(showEmailCurrentPassword = !it.showEmailCurrentPassword) }
    }

    fun changeEmail() {
        val state = _uiState.value

        // Validation
        if (state.newEmail.isBlank()) {
            _uiState.update { it.copy(emailError = "Please enter a new email") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.newEmail).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            return
        }
        if (state.emailCurrentPassword.isBlank()) {
            _uiState.update { it.copy(emailError = "Please enter your current password") }
            return
        }
        if (state.newEmail.trim().lowercase() == state.currentEmailDisplay.lowercase()) {
            _uiState.update { it.copy(emailError = "New email is same as current email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingEmail = true, emailError = null) }

            val result = authRepository.changeEmail(
                newEmail = state.newEmail.trim(),
                currentPassword = state.emailCurrentPassword
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isChangingEmail = false,
                            currentEmailDisplay = state.newEmail.trim(),
                            newEmail = "",
                            emailCurrentPassword = "",
                            emailSuccess = "Email updated successfully! Use your new email to log in next time.",
                            emailError = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isChangingEmail = false,
                            emailError = e.message ?: "Failed to update email"
                        )
                    }
                }
            )
        }
    }

    // ── Change Password ──────────────────────────────────────────

    fun onCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(currentPassword = value, passwordError = null, passwordSuccess = null) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null) }
    }

    fun onConfirmNewPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmNewPassword = value, passwordError = null) }
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.update { it.copy(showCurrentPassword = !it.showCurrentPassword) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(showNewPassword = !it.showNewPassword) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(showConfirmPassword = !it.showConfirmPassword) }
    }

    fun changePassword() {
        val state = _uiState.value

        // Validation
        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(passwordError = "Please enter your current password") }
            return
        }
        if (state.newPassword.isBlank()) {
            _uiState.update { it.copy(passwordError = "Please enter a new password") }
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            return
        }
        if (state.newPassword != state.confirmNewPassword) {
            _uiState.update { it.copy(passwordError = "Passwords do not match") }
            return
        }
        if (state.currentPassword == state.newPassword) {
            _uiState.update { it.copy(passwordError = "New password must be different from current") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, passwordError = null) }

            val result = authRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            currentPassword = "",
                            newPassword = "",
                            confirmNewPassword = "",
                            passwordSuccess = "Password changed successfully!",
                            passwordError = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            passwordError = e.message ?: "Failed to change password"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                emailSuccess = null,
                emailError = null,
                passwordSuccess = null,
                passwordError = null
            )
        }
    }
}
