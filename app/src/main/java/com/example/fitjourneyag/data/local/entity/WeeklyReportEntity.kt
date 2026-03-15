package com.example.fitjourneyag.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reports")
data class WeeklyReportEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val weekStartDate: Long,
    val weekEndDate: Long,
    val averageSteps: Int,
    val totalWorkouts: Int,
    val averageCalories: Float,
    val averageWaterMl: Int,
    val weightChangeKg: Float,
    val aiAnalysis: String,
    val generatedAt: Long = 0L
)
