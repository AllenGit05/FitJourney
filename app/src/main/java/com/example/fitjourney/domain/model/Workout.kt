package com.example.fitjourney.domain.model

import java.util.*

data class WorkoutSet(
    val reps: Int,
    val weight: Float,
    val isCompleted: Boolean = false
)

data class Exercise(
    val name: String,
    val sets: List<WorkoutSet>,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val restTimeSeconds: Int = 60
)

data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val totalDurationMinutes: Int,
    val totalCaloriesBurned: Int,
    val exercises: List<Exercise>
)
