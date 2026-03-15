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
    private val adminConfig: AdminConfig? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    private fun loadAdmins() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get real master admin email from AdminConfig DataStore
                val masterEmail = adminConfig?.getAdminEmail() ?: ""

                // Build admin list starting with just the master admin
                val adminList = mutableListOf<AdminUser>()
                
                if (masterEmail.isNotBlank()) {
                    adminList.add(AdminUser("master", masterEmail, isMasterAdmin = true))
                }

                // Fetch any additional admins saved in Firestore "admins" collection
                try {
                    val snapshot = firestore.collection("admins").get().await()
                    snapshot.documents.forEach { doc ->
                        val email = doc.getString("email") ?: return@forEach
                        // Don't duplicate master admin
                        if (email != masterEmail) {
                            adminList.add(AdminUser(doc.id, email, isMasterAdmin = false))
                        }
                    }
                } catch (e: Exception) {
                    // Admins collection may not exist yet — that's fine
                }

                _admins.value = adminList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load admins: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setNewAdminEmail(email: String) {
        _newAdminEmail.value = email
        _errorMessage.value = null
    }

    fun addAdmin() {
        val email = _newAdminEmail.value.trim()
        if (email.isBlank()) return
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        // Don't add duplicate
        if (_admins.value.any { it.email == email }) {
            _errorMessage.value = "This email is already an admin"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Save to Firestore "admins" collection
                val docRef = firestore.collection("admins").document()
                docRef.set(mapOf(
                    "email" to email,
                    "addedAt" to System.currentTimeMillis()
                )).await()

                val newAdmin = AdminUser(docRef.id, email, isMasterAdmin = false)
                _admins.value = _admins.value + newAdmin
                _newAdminEmail.value = ""
                _successMessage.value = "Admin added successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add admin: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeAdmin(adminId: String) {
        val admin = _admins.value.find { it.id == adminId }
        if (admin == null || admin.isMasterAdmin) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Delete from Firestore
                firestore.collection("admins").document(adminId).delete().await()
                _admins.value = _admins.value.filter { it.id != adminId }
                _successMessage.value = "Admin removed"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove admin: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
