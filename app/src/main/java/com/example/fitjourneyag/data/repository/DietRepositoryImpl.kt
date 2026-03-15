package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.local.dao.DietDao
import com.example.fitjourneyag.data.local.entity.DietEntity
import com.example.fitjourneyag.domain.repository.DietRepository
import com.example.fitjourneyag.domain.repository.FoodLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class DietRepositoryImpl(
    private val dietDao: DietDao
) : DietRepository {

    override val foodLogs: StateFlow<List<FoodLogEntry>> = dietDao.getAllLogs()
        .map { it.map { entity -> entity.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, emptyList())

    override val totalCaloriesToday: StateFlow<Int> = foodLogs.map { logs ->
        logs.filter { isToday(it.date) }.sumOf { it.calories }
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, 0)

    override val totalProteinToday: StateFlow<Int> = foodLogs.map { logs ->
        logs.filter { isToday(it.date) }.sumOf { it.protein }
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, 0)

    override suspend fun addFood(entry: FoodLogEntry) {
        dietDao.insertLog(entry.toEntity())
    }

    override suspend fun removeFood(entry: FoodLogEntry) {
        dietDao.deleteLog(entry.toEntity())
    }

    override suspend fun removeFoodByName(name: String) {
        dietDao.deleteLogsByName(name)
    }

    private fun isToday(timestamp: Long): Boolean {
        val cal = java.util.Calendar.getInstance()
        val today = cal.get(java.util.Calendar.DAY_OF_YEAR)
        val year = cal.get(java.util.Calendar.YEAR)
        cal.timeInMillis = timestamp
        return cal.get(java.util.Calendar.DAY_OF_YEAR) == today && cal.get(java.util.Calendar.YEAR) == year
    }

    private fun DietEntity.toDomain(): FoodLogEntry = FoodLogEntry(
        id = id,
        name = foodName,
        calories = calories,
        protein = proteinGrams.toInt(),
        carbs = carbsGrams.toInt(),
        fats = fatsGrams.toInt(),
        date = timestamp,
        mealType = mealType
    )

    private fun FoodLogEntry.toEntity(): DietEntity = DietEntity(
        id = id,
        mealType = mealType,
        foodName = name,
        calories = calories,
        proteinGrams = protein.toFloat(),
        carbsGrams = carbs.toFloat(),
        fatsGrams = fats.toFloat(),
        timestamp = date
    )
}
