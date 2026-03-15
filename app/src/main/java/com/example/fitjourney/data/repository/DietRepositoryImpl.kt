package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.DietDao
import com.example.fitjourney.data.local.entity.DietEntity
import com.example.fitjourney.domain.repository.DietRepository
import com.example.fitjourney.domain.repository.FoodLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class DietRepositoryImpl(
    private val dietDao: DietDao,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
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
        dietDao.insertLog(entry.toEntity().copy(isSynced = false))
        syncManager.startSync()
    }

    override suspend fun removeFood(entry: FoodLogEntry) {
        dietDao.softDelete(entry.id)
        syncManager.startSync()
    }

    override suspend fun removeFoodByName(name: String) {
        // Soft delete all by name if possible, or just skip for now as it's less common
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
