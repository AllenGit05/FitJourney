package com.example.fitjourney.domain.model

import java.util.*

data class WeeklyReport(
    val id: String,
    val weekStartDate: Long,
    val weekEndDate: Long,
    val averageSteps: Int,
    val totalWorkouts: Int,
    val averageCalories: Float,
    val averageWaterMl: Int,
    val weightChangeKg: Float,
    val aiAnalysis: String,
    val generatedAt: Long
)
