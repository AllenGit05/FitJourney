package com.example.fitjourney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_logs")
data class DietEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String, // Breakfast, Lunch, etc.
    val foodName: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatsGrams: Float,
    val timestamp: Long,
    val isSynced: Boolean = false,
    val syncedAt: Long = 0L,
    val firestoreId: String = "",
    val isDeleted: Boolean = false
)
