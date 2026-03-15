package com.example.fitjourneyag.data.local.entity

import androidx.room.*
import java.util.UUID

@Entity(tableName = "step_logs")
data class StepsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val count: Int
)
