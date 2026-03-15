package com.example.fitjourneyag.domain.repository

import com.example.fitjourneyag.domain.model.WeeklyReport
import kotlinx.coroutines.flow.StateFlow

interface WeeklyReportRepository {
    val reports: StateFlow<List<WeeklyReport>>
    suspend fun saveReport(report: WeeklyReport)
    suspend fun insertReport(report: com.example.fitjourneyag.data.local.entity.WeeklyReportEntity)
}
