package com.example.fitjourneyag.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel : ViewModel() {
    
    // In a real app these would be fetched from Firestore aggregations
    private val _totalClients = MutableStateFlow(0)
    val totalClients: StateFlow<Int> = _totalClients.asStateFlow()

    private val _totalAdmins = MutableStateFlow(0)
    val totalAdmins: StateFlow<Int> = _totalAdmins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
}
