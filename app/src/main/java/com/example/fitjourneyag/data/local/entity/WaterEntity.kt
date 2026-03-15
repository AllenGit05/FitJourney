package com.example.fitjourneyag.data.local.entity

import androidx.room.*
import java.util.UUID

@Entity(tableName = "water_logs")
data class WaterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val ml: Int
)
