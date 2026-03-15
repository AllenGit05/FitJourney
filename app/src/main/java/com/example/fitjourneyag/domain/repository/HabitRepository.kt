package com.example.fitjourneyag.domain.repository

import com.example.fitjourneyag.domain.model.Habit
import kotlinx.coroutines.flow.StateFlow

interface HabitRepository {
    val habits: StateFlow<List<Habit>>
    suspend fun toggleHabit(habitId: String, date: Long = System.currentTimeMillis())
    suspend fun addHabit(name: String, icon: String)
    suspend fun deleteHabit(habitId: String)
}
