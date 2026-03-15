package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.local.dao.WaterDao
import com.example.fitjourneyag.data.local.entity.WaterEntity
import com.example.fitjourneyag.domain.repository.WaterLogEntry
import com.example.fitjourneyag.domain.repository.WaterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class WaterRepositoryImpl(private val waterDao: WaterDao) : WaterRepository {

    override val waterLogs: Flow<List<WaterLogEntry>> = waterDao.getAllWater()
        .map { entities -> 
            entities.map { it.toDomain() }
        }

    override val totalWaterToday: StateFlow<Int> = waterLogs
        .map { logs ->
            logs.filter { isToday(it.date) }.sumOf { it.ml }
        }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), 0)

    override suspend fun logWater(ml: Int) {
        waterDao.insertWater(WaterEntity(ml = ml))
    }

    private fun WaterEntity.toDomain() = WaterLogEntry(id = id, date = date, ml = ml)

    private fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        cal.timeInMillis = timestamp
        return cal.get(Calendar.DAY_OF_YEAR) == todayDay && cal.get(Calendar.YEAR) == todayYear
    }
}
