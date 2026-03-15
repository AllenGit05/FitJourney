package com.example.fitjourneyag.domain.model

import java.util.*

data class PlanExercise(
    val name: String,
    val sets: Int,
    val reps: String, // e.g., "10-12" or "8"
    val restTimeSeconds: Int
)

data class WorkoutDay(
    val dayName: String, // e.g., "Day 1: Upper Body"
    val exercises: List<PlanExercise>
)

data class WeeklyWorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val goal: String,
    val level: String,
    val location: String,
    val daysPerWeek: Int,
    val durationMinutes: Int,
    val weeklySchedule: List<WorkoutDay>,
    val createdAt: Long = System.currentTimeMillis()
)
