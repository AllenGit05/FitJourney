package com.example.fitjourneyag.data.local.entity

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
    val timestamp: Long
)
