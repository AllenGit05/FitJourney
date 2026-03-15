package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.WeeklyReportDao
import com.example.fitjourney.data.local.entity.WeeklyReportEntity
import com.example.fitjourney.domain.model.WeeklyReport
import com.example.fitjourney.domain.repository.WeeklyReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*

class WeeklyReportRepositoryImpl(
    private val weeklyReportDao: WeeklyReportDao,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : WeeklyReportRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override val reports: StateFlow<List<WeeklyReport>> = weeklyReportDao.getAllReports()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override suspend fun saveReport(report: WeeklyReport) {
        weeklyReportDao.insertReport(report.toEntity().copy(isSynced = false))
        syncManager.startSync()
    }

    override suspend fun insertReport(report: WeeklyReportEntity) {
        weeklyReportDao.insertReport(report.copy(isSynced = false))
        syncManager.startSync()
    }

    private fun WeeklyReportEntity.toDomain(): WeeklyReport = WeeklyReport(
        id = id,
        weekStartDate = weekStartDate,
        weekEndDate = weekEndDate,
        averageSteps = averageSteps,
        totalWorkouts = totalWorkouts,
        averageCalories = averageCalories,
        averageWaterMl = averageWaterMl,
        weightChangeKg = weightChangeKg,
        aiAnalysis = aiAnalysis,
        generatedAt = generatedAt
    )

    private fun WeeklyReport.toEntity(): WeeklyReportEntity = WeeklyReportEntity(
        id = id,
        weekStartDate = weekStartDate,
        weekEndDate = weekEndDate,
        averageSteps = averageSteps,
        totalWorkouts = totalWorkouts,
        averageCalories = averageCalories,
        averageWaterMl = averageWaterMl,
        weightChangeKg = weightChangeKg,
        aiAnalysis = aiAnalysis,
        generatedAt = generatedAt
    )
}
