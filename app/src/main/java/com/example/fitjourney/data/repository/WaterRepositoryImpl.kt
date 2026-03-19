package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.WaterDao
import com.example.fitjourney.data.local.entity.WaterEntity
import com.example.fitjourney.domain.repository.WaterLogEntry
import com.example.fitjourney.domain.repository.WaterRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.io.Closeable

class WaterRepositoryImpl(
    private val waterDao: WaterDao,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : WaterRepository, Closeable {

    // The scope's lifetime matches the Application lifecycle as this is a singleton in AppContainer.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val waterLogs: Flow<List<WaterLogEntry>> = waterDao.getAllWater()
        .map { entities -> 
            entities.map { it.toDomain() }
        }

    override val totalWaterToday: StateFlow<Int> = waterLogs
        .map { logs ->
            logs.filter { isToday(it.date) }.sumOf { it.ml }
        }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), 0)

    override suspend fun logWater(ml: Int) {
        waterDao.insertWater(WaterEntity(ml = ml, isSynced = false))
        syncManager.startSync()
    }

    override fun close() {
        repositoryScope.cancel()
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
