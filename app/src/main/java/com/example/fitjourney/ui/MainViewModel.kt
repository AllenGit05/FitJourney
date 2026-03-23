package com.example.fitjourney.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.UserRole
import com.example.fitjourney.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AppStartDestination {
    object Loading : AppStartDestination()
    object Login : AppStartDestination()
    object ClientDashboard : AppStartDestination()
    object AdminDashboard : AppStartDestination()
}

class MainViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _startDestination =
        MutableStateFlow<AppStartDestination>(AppStartDestination.Loading)
    val startDestination: StateFlow<AppStartDestination> =
        _startDestination.asStateFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            try {
                if (!authRepository.isLoggedIn()) {
                    _startDestination.value = AppStartDestination.Login
                    return@launch
                }
                // Wait up to 3 seconds for user to load from cache
                val user = kotlinx.coroutines.withTimeoutOrNull(3000L) {
                    authRepository.currentUser.first { it != null }
                }
                _startDestination.value = when (user?.role) {
                    UserRole.ADMIN -> AppStartDestination.AdminDashboard
                    UserRole.CLIENT -> AppStartDestination.ClientDashboard
                    else -> AppStartDestination.Login
                }
            } catch (e: Exception) {
                _startDestination.value = AppStartDestination.Login
            }
        }
    }

    fun refresh() {
        _startDestination.value = AppStartDestination.Loading
        resolveStartDestination()
    }
}
