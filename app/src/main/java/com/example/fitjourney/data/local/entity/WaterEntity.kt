package com.example.fitjourney.data.local.entity

import androidx.room.*
import java.util.UUID

@Entity(tableName = "water_logs")
data class WaterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val ml: Int,
    val isSynced: Boolean = false,
    val syncedAt: Long = 0L,
    val firestoreId: String = "",
    val isDeleted: Boolean = false
)
