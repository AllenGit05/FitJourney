package com.example.fitjourney.ui.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.data.local.AdminConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUser(val id: String, val email: String, val isMasterAdmin: Boolean)

class AdminManagementViewModel(
    private val firestore: com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _admins = MutableStateFlow<List<AdminUser>>(emptyList())
    val admins: StateFlow<List<AdminUser>> = _admins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _newAdminEmail = MutableStateFlow("")
    val newAdminEmail: StateFlow<String> = _newAdminEmail.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadAdmins()
    }

    fun setNewAdminEmail(email: String) {
        _newAdminEmail.value = email
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    private fun loadAdmins() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("admins").get().await()
                _admins.value = snapshot.documents.map { doc ->
                    AdminUser(
                        id = doc.id,
                        email = doc.getString("email") ?: "",
                        isMasterAdmin = doc.getBoolean("isMasterAdmin") ?: false
                    )
                }
            } catch (e: Exception) {
                _admins.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun addAdmin() {
        if (_newAdminEmail.value.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "email" to _newAdminEmail.value.trim().lowercase(),
                    "isMasterAdmin" to false
                )
                firestore.collection("admins").add(data).await()
                loadAdmins()
                _newAdminEmail.value = ""
            } catch (e: Exception) {
                // handle silently
            }
            _isLoading.value = false
        }
    }

    fun removeAdmin(adminId: String) {
        val admin = _admins.value.find { it.id == adminId }
        if (admin == null || admin.isMasterAdmin) return
        viewModelScope.launch {
            try {
                firestore.collection("admins").document(adminId).delete().await()
                _admins.value = _admins.value.filter { it.id != adminId }
            } catch (e: Exception) {
                // handle silently
            }
        }
    }
}
