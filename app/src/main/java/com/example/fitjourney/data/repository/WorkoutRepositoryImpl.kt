package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.WorkoutDao
import com.example.fitjourney.data.local.entity.WorkoutEntity
import com.example.fitjourney.domain.model.*
import com.example.fitjourney.domain.repository.WorkoutRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : WorkoutRepository, Closeable {

    // The scope's lifetime matches the Application lifecycle as this is a singleton in AppContainer.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val workoutHistory: Flow<List<WorkoutSession>> = workoutDao.getAllSessions()
        .map { it.map { entity -> entity.toDomain() } }

    private val _activePlan = MutableStateFlow<WeeklyWorkoutPlan?>(null)
    override val activePlan: Flow<WeeklyWorkoutPlan?> = _activePlan.asStateFlow()

    override suspend fun saveWorkout(session: WorkoutSession): Result<Unit> {
        return try {
            workoutDao.insertSession(session.toEntity().copy(isSynced = false))
            syncManager.startSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveWorkoutPlan(plan: WeeklyWorkoutPlan): Result<Unit> {
        _activePlan.value = plan
        return Result.success(Unit)
    }

    override fun close() {
        repositoryScope.cancel()
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
