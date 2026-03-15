package com.example.fitjourneyag.ui.client.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourneyag.domain.model.Habit
import com.example.fitjourneyag.domain.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = habitRepository.habits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.toggleHabit(habitId)
        }
    }

    fun addHabit(name: String, icon: String) {
        viewModelScope.launch {
            habitRepository.addHabit(name, icon)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
        }
    }
}
