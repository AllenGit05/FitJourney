package com.example.fitjourneyag.ui.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUser(val id: String, val email: String, val isMasterAdmin: Boolean)

class AdminManagementViewModel : ViewModel() {
    
    private val _admins = MutableStateFlow<List<AdminUser>>(emptyList())
    val admins: StateFlow<List<AdminUser>> = _admins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _newAdminEmail = MutableStateFlow("")
    val newAdminEmail: StateFlow<String> = _newAdminEmail.asStateFlow()

    init {
        loadAdmins()
    }

    private fun loadAdmins() {
        viewModelScope.launch {
            _isLoading.value = true
            // Mock fetching data
            kotlinx.coroutines.delay(1000)
            _admins.value = listOf(
                AdminUser("adm1", "master@fitjourney.com", true),
                AdminUser("adm2", "secondary@fitjourney.com", false)
            )
            _isLoading.value = false
        }
    }

    fun setNewAdminEmail(email: String) {
        _newAdminEmail.value = email
    }

    fun addAdmin() {
        if (_newAdminEmail.value.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                // Mock adding admin logic
                kotlinx.coroutines.delay(1000)
                val newAdmin = AdminUser("adm${System.currentTimeMillis()}", _newAdminEmail.value, false)
                _admins.value = _admins.value + newAdmin
                _newAdminEmail.value = ""
                _isLoading.value = false
            }
        }
    }

    fun removeAdmin(adminId: String) {
        // Find if they are master admin, master admin cannot be removed
        val admin = _admins.value.find { it.id == adminId }
        if (admin != null && !admin.isMasterAdmin) {
            _admins.value = _admins.value.filter { it.id != adminId }
            // Perform actual backend deletion
        }
    }
}
