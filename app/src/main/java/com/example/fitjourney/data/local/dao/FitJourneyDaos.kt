package com.example.fitjourney.data.local.dao

import androidx.room.*
import com.example.fitjourney.data.local.entity.ChatMessageEntity
import com.example.fitjourney.data.local.entity.DietEntity
import com.example.fitjourney.data.local.entity.UserEntity
import com.example.fitjourney.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()

    @Query("SELECT * FROM users WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedUsers(): Flow<List<UserEntity>>
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutEntity)

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAll()

    @Query("SELECT * FROM workout_sessions WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedSessions(): Flow<List<WorkoutEntity>>

    @Query("UPDATE workout_sessions SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface DietDao {
    @Query("SELECT * FROM diet_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DietEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DietEntity)

    @Delete
    suspend fun deleteLog(log: DietEntity)

    @Query("DELETE FROM diet_logs WHERE foodName = :name")
    suspend fun deleteLogsByName(name: String)

    @Query("DELETE FROM diet_logs")
    suspend fun clearAll()

    @Query("SELECT * FROM diet_logs WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedLogs(): Flow<List<DietEntity>>

    @Query("UPDATE diet_logs SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)
}

@Dao
interface StepsDao {
    @Query("SELECT * FROM step_logs ORDER BY date DESC")
    fun getAllSteps(): Flow<List<com.example.fitjourney.data.local.entity.StepsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: com.example.fitjourney.data.local.entity.StepsEntity)

    @Delete
    suspend fun deleteSteps(steps: com.example.fitjourney.data.local.entity.StepsEntity)

    @Query("DELETE FROM step_logs")
    suspend fun clearAll()

    @Query("SELECT * FROM step_logs WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedSteps(): Flow<List<com.example.fitjourney.data.local.entity.StepsEntity>>

    @Query("UPDATE step_logs SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs ORDER BY date DESC")
    fun getAllWater(): Flow<List<com.example.fitjourney.data.local.entity.WaterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: com.example.fitjourney.data.local.entity.WaterEntity)

    @Query("DELETE FROM water_logs")
    suspend fun clearAll()

    @Query("SELECT * FROM water_logs WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedWater(): Flow<List<com.example.fitjourney.data.local.entity.WaterEntity>>

    @Query("UPDATE water_logs SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getAllWeight(): Flow<List<com.example.fitjourney.data.local.entity.WeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: com.example.fitjourney.data.local.entity.WeightEntity)

    @Delete
    suspend fun deleteWeight(weight: com.example.fitjourney.data.local.entity.WeightEntity)

    @Query("DELETE FROM weight_logs")
    suspend fun clearAll()

    @Query("SELECT * FROM weight_logs WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedWeight(): Flow<List<com.example.fitjourney.data.local.entity.WeightEntity>>

    @Query("UPDATE weight_logs SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM progress_photos ORDER BY date DESC")
    fun getAllPhotos(): Flow<List<com.example.fitjourney.data.local.entity.ProgressPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: com.example.fitjourney.data.local.entity.ProgressPhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: com.example.fitjourney.data.local.entity.ProgressPhotoEntity)

    @Query("DELETE FROM progress_photos")
    suspend fun clearAll()

    @Query("SELECT * FROM progress_photos WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedPhotos(): Flow<List<com.example.fitjourney.data.local.entity.ProgressPhotoEntity>>

    @Query("UPDATE progress_photos SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAllMeasurements(): Flow<List<com.example.fitjourney.data.local.entity.BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: com.example.fitjourney.data.local.entity.BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: com.example.fitjourney.data.local.entity.BodyMeasurementEntity)

    @Query("DELETE FROM body_measurements")
    suspend fun clearAll()

    @Query("SELECT * FROM body_measurements WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedMeasurements(): Flow<List<com.example.fitjourney.data.local.entity.BodyMeasurementEntity>>

    @Query("UPDATE body_measurements SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}
@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearHistory(userId: String)

    @Query("SELECT * FROM chat_messages WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedMessages(): Flow<List<ChatMessageEntity>>

    @Query("UPDATE chat_messages SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): kotlinx.coroutines.flow.Flow<List<com.example.fitjourney.data.local.entity.HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: com.example.fitjourney.data.local.entity.HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: com.example.fitjourney.data.local.entity.HabitEntity)

    @Query("SELECT * FROM habits WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedHabits(): Flow<List<com.example.fitjourney.data.local.entity.HabitEntity>>

    @Query("UPDATE habits SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}

@Dao
interface WeeklyReportDao {
    @Query("SELECT * FROM weekly_reports ORDER BY generatedAt DESC")
    fun getAllReports(): kotlinx.coroutines.flow.Flow<List<com.example.fitjourney.data.local.entity.WeeklyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: com.example.fitjourney.data.local.entity.WeeklyReportEntity)

    @Query("SELECT * FROM weekly_reports WHERE isSynced = 0 OR isDeleted = 1")
    fun getUnsyncedReports(): Flow<List<com.example.fitjourney.data.local.entity.WeeklyReportEntity>>

    @Query("UPDATE weekly_reports SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)
}
