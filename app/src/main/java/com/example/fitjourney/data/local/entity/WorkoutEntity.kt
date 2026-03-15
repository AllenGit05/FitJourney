package com.example.fitjourney.data.local.entity

import androidx.room.*
import com.example.fitjourney.domain.model.Exercise

@Entity(tableName = "workout_sessions")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val totalDurationMinutes: Int,
    val totalCaloriesBurned: Int,
    val exercises: List<Exercise>,
    val isSynced: Boolean = false,
    val syncedAt: Long = 0L,
    val firestoreId: String = "",
    val isDeleted: Boolean = false
)
