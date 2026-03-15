package com.example.fitjourney.domain.repository

import com.example.fitjourney.domain.model.WorkoutSession
import com.example.fitjourney.domain.model.WeeklyWorkoutPlan
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    val workoutHistory: Flow<List<WorkoutSession>>
    val activePlan: Flow<WeeklyWorkoutPlan?>
    suspend fun saveWorkout(session: WorkoutSession): Result<Unit>
    suspend fun saveWorkoutPlan(plan: WeeklyWorkoutPlan): Result<Unit>
}
