package com.example.fitjourney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val isSynced: Boolean = false,
    val syncedAt: Long = 0L,
    val firestoreId: String = "",
    val isDeleted: Boolean = false,
    val currentStreak: Int,
    val bestStreak: Int,
    val isCompletedToday: Boolean,
    val isMastered: Boolean = false,
    val isMilestone: Boolean = false,
    val freezesUsedThisWeek: Int = 0,
    val lastFreezeResetDate: String = "", // format: YYYY-WW

    val logsJson: String
)

