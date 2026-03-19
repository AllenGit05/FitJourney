package com.example.fitjourney.domain.model

import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String, // String representation of an icon name
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val freezesUsedThisWeek: Int = 0,
    val logs: List<HabitLog> = emptyList()
)

data class HabitLog(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)
