package com.example.fitjourney.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.data.sync.SyncManager
import com.example.fitjourney.data.local.AdminConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardViewModel(
    private val syncManager: SyncManager? = null
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _totalClients = MutableStateFlow(0)
    val totalClients: StateFlow<Int> = _totalClients.asStateFlow()

    private val _totalAdmins = MutableStateFlow(1) // Always at least 1 (master admin)
    val totalAdmins: StateFlow<Int> = _totalAdmins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Query Firestore users collection and count by role
                val snapshot = firestore.collection("users").get().await()
                val clientCount = snapshot.documents.count { doc ->
                    val role = doc.getString("role") ?: "CLIENT"
                    role == "CLIENT"
                }
                _totalClients.value = clientCount
                // Admin count: just 1 (master admin) unless more are added via AdminManagement
                // We don't count from Firestore since admins bypass Firestore entirely
                _totalAdmins.value = 1
            } catch (e: Exception) {
                // Network error — show 0
                _totalClients.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshStats() {
        loadStats()
    }
}

