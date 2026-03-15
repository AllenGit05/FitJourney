package com.example.fitjourneyag.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val waist: Float = 0f,
    val chest: Float = 0f,
    val arms: Float = 0f,
    val hips: Float = 0f,
    val legs: Float = 0f
)
