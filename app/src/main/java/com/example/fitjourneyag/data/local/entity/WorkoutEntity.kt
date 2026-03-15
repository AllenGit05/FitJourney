package com.example.fitjourneyag.data.local.entity

import androidx.room.*
import com.example.fitjourneyag.domain.model.Exercise

@Entity(tableName = "workout_sessions")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val totalDurationMinutes: Int,
    val totalCaloriesBurned: Int,
    val exercises: List<Exercise>
)
