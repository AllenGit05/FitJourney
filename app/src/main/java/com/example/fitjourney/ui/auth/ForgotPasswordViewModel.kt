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

    fun setEmail(value: String) { _email.value = value }

    fun resetPassword(onSuccess: () -> Unit) {
        if (_email.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.resetPassword(
                    email = _email.value.trim()
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
