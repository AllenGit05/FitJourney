package com.example.fitjourney.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WaterRepository {
    val waterLogs: Flow<List<WaterLogEntry>>
    val totalWaterToday: StateFlow<Int>
    suspend fun logWater(ml: Int)
}

data class WaterLogEntry(
    val id: String,
    val date: Long,
    val ml: Int
)
