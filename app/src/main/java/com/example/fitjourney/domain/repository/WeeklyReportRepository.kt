package com.example.fitjourney.domain.repository

import com.example.fitjourney.domain.model.WeeklyReport
import kotlinx.coroutines.flow.StateFlow

interface WeeklyReportRepository {
    val reports: StateFlow<List<WeeklyReport>>
    suspend fun saveReport(report: WeeklyReport)
    suspend fun insertReport(report: com.example.fitjourney.data.local.entity.WeeklyReportEntity)
}
