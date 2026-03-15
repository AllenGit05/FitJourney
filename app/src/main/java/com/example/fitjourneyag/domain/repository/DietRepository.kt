package com.example.fitjourneyag.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class FoodLogEntry(
    val id: Long = 0,
    val name: String, 
    val calories: Int, 
    val protein: Int, 
    val carbs: Int, 
    val fats: Int,
    val date: Long = System.currentTimeMillis(),
    val mealType: String = "Snack" // Breakfast, Lunch, Dinner, Snack
)

interface DietRepository {
    val totalCaloriesToday: StateFlow<Int>
    val totalProteinToday: StateFlow<Int>
    val foodLogs: StateFlow<List<FoodLogEntry>>
    
    suspend fun addFood(entry: FoodLogEntry)
    suspend fun removeFood(entry: FoodLogEntry)
    suspend fun removeFoodByName(name: String)
}
