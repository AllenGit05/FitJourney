package com.example.fitjourneyag.domain.model

import java.util.*

data class WeightEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val weight: Float
)

data class ProgressPhoto(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val imageUrl: String, // Path to local storage or URI
    val weight: Float,
    val note: String = ""
)

data class StrengthPoint(
    val exerciseName: String,
    val date: Long,
    val maxWeight: Float
)

data class BodyMeasurement(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val waist: Float = 0f,
    val chest: Float = 0f,
    val arms: Float = 0f,
    val hips: Float = 0f,
    val legs: Float = 0f
)

data class StepsEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val count: Int
)
