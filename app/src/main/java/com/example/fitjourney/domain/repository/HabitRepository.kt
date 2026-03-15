package com.example.fitjourney.domain.repository

import com.example.fitjourney.domain.model.Habit
import kotlinx.coroutines.flow.StateFlow

interface HabitRepository {
    val habits: StateFlow<List<Habit>>
    suspend fun toggleHabit(habitId: String, date: Long = System.currentTimeMillis())
    suspend fun addHabit(name: String, icon: String)
    suspend fun deleteHabit(habitId: String)
}
