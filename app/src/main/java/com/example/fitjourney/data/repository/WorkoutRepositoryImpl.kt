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
    private val syncManager: com.example.fitjourney.data.sync.SyncManager,
    private val context: android.content.Context
) : WorkoutRepository, Closeable {

    private val ACTIVE_PLAN_KEY = "active_workout_plan_json"

    // The scope's lifetime matches the Application lifecycle as this is a singleton in AppContainer.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val workoutHistory: Flow<List<WorkoutSession>> = workoutDao.getAllSessions()
        .map { it.map { entity -> entity.toDomain() } }

    private val _activePlan = MutableStateFlow<WeeklyWorkoutPlan?>(null)
    override val activePlan: Flow<WeeklyWorkoutPlan?> = _activePlan.asStateFlow()

    init {
        val json = context.getSharedPreferences(
            "workout_prefs", android.content.Context.MODE_PRIVATE
        ).getString(ACTIVE_PLAN_KEY, null)
        if (json != null) {
            try {
                _activePlan.value = com.google.gson.Gson()
                    .fromJson(json, WeeklyWorkoutPlan::class.java)
            } catch (e: Exception) { /* ignore corrupt data */ }
        }
    }

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
        return try {
            val json = com.google.gson.Gson().toJson(plan)
            context.getSharedPreferences("workout_prefs", android.content.Context.MODE_PRIVATE)
                .edit().putString(ACTIVE_PLAN_KEY, json).apply()
            _activePlan.value = plan
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
