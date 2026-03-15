package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.local.dao.WorkoutDao
import com.example.fitjourneyag.data.local.entity.WorkoutEntity
import com.example.fitjourneyag.domain.model.*
import com.example.fitjourneyag.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.*

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override val workoutHistory: Flow<List<WorkoutSession>> = workoutDao.getAllSessions()
        .map { it.map { entity -> entity.toDomain() } }

    private val _activePlan = MutableStateFlow<WeeklyWorkoutPlan?>(null)
    override val activePlan: Flow<WeeklyWorkoutPlan?> = _activePlan.asStateFlow()

    override suspend fun saveWorkout(session: WorkoutSession): Result<Unit> {
        return try {
            workoutDao.insertSession(session.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveWorkoutPlan(plan: WeeklyWorkoutPlan): Result<Unit> {
        _activePlan.value = plan
        return Result.success(Unit)
    }

    private fun WorkoutEntity.toDomain(): WorkoutSession = WorkoutSession(
        id,
        date,
        totalDurationMinutes,
        totalCaloriesBurned,
        exercises
    )

    private fun WorkoutSession.toEntity(): WorkoutEntity = WorkoutEntity(
        id,
        date,
        totalDurationMinutes,
        totalCaloriesBurned,
        exercises
    )
}
