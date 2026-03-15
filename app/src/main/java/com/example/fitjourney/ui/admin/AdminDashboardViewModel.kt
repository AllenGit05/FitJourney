package com.example.fitjourney.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val syncManager: com.example.fitjourney.data.sync.SyncManager? = null,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore? = null
) : ViewModel() {
    
    // In a real app these would be fetched from Firestore aggregations
    private val _totalClients = MutableStateFlow(0)
    val totalClients: StateFlow<Int> = _totalClients.asStateFlow()

    private val _totalAdmins = MutableStateFlow(0)
    val totalAdmins: StateFlow<Int> = _totalAdmins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            // Mock data loading
            kotlinx.coroutines.delay(1000)
            _totalClients.value = 154
            _totalAdmins.value = 2
            _isLoading.value = false
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Starting manual sync..."
            try {
                syncManager?.startSync()
                _lastSyncTime.value = System.currentTimeMillis()
                _syncMessage.value = "Sync completed successfully"
            } catch (e: Exception) {
                _syncMessage.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun resetFirebase() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore?.let { 
                    com.example.fitjourney.util.FirebaseInitializer.wipeAndReinitialize(it)
                    _syncMessage.value = "Firebase project wiped and re-initialized"
                }
            } catch (e: Exception) {
                _syncMessage.value = "Reset failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
