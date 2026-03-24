package com.example.fitjourney.ui.client.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.data.local.entity.WeeklyReportEntity
import com.example.fitjourney.domain.model.WeeklyReport
import com.example.fitjourney.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeeklyInsightsUiState(
    val isGenerating: Boolean = false,
    val error: String? = null,
    val currentReport: WeeklyReportEntity? = null
)

class WeeklyInsightsViewModel(
    private val dietRepository: DietRepository,
    private val workoutRepository: WorkoutRepository,
    private val progressRepository: ProgressRepository,
    private val weeklyReportRepository: WeeklyReportRepository,
    private val userRepository: UserRepository,
    private val apiRepository: ApiRepository,
    private val waterRepository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyInsightsUiState())
    val uiState: StateFlow<WeeklyInsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            weeklyReportRepository.reports.first().maxByOrNull { it.generatedAt }?.let { latest ->
                val entity = WeeklyReportEntity(
                    id = latest.id,
                    weekStartDate = latest.weekStartDate,
                    weekEndDate = latest.weekEndDate,
                    averageSteps = latest.averageSteps,
                    totalWorkouts = latest.totalWorkouts,
                    averageCalories = latest.averageCalories,
                    averageWaterMl = latest.averageWaterMl,
                    weightChangeKg = latest.weightChangeKg,
                    aiAnalysis = latest.aiAnalysis,
                    generatedAt = latest.generatedAt
                )
                _uiState.update { it.copy(currentReport = entity) }
            }
        }
    }

    fun saveReport(context: android.content.Context, report: WeeklyReportEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = "FitJourney_Weekly_Report_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.txt"
                val content = buildString {
                    appendLine("FitJourney Weekly Insights Report")
                    appendLine("Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(report.generatedAt))}")
                    appendLine("Period: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(report.weekStartDate))} - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(report.weekEndDate))}")
                    appendLine("---")
                    appendLine("Workouts: ${report.totalWorkouts}")
                    appendLine("Avg Steps: ${report.averageSteps}")
                    appendLine("Avg Calories: ${report.averageCalories.toInt()} kcal")
                    appendLine("Avg Water: ${report.averageWaterMl} ml")
                    appendLine("Weight Change: ${report.weightChangeKg} kg")
                    appendLine("---")
                    appendLine(report.aiAnalysis)
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                
                uri?.let {
                    resolver.openOutputStream(it)?.use { stream ->
                        stream.write(content.toByteArray())
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save report: ${e.message}") }
            }
        }
    }


    fun generateWeeklyReport() {
        viewModelScope.launch {
            try {
                // Check if user has enough credits
                val user = userRepository.userProfile.first()
                if (user == null || (!user.isPremium && user.aiCredits < 3)) {
                    _uiState.update { it.copy(error = "Insufficient credits. You need 3 credits.") }
                    return@launch
                }

                _uiState.update { it.copy(isGenerating = true, error = null) }

                // Collect last 7 days of data
                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

                val workouts = withContext(Dispatchers.IO) {
                    workoutRepository.workoutHistory.first().filter { it.date >= sevenDaysAgo }
                }
                val dietEntries = withContext(Dispatchers.IO) {
                    dietRepository.foodLogs.value.filter { it.date >= sevenDaysAgo }
                }
                val stepEntries = withContext(Dispatchers.IO) {
                    progressRepository.stepsHistory.value.filter { it.date >= sevenDaysAgo }
                }
                val weightEntries = withContext(Dispatchers.IO) {
                    progressRepository.weightHistory.value.filter { it.date >= sevenDaysAgo }
                }
                val waterEntries = withContext(Dispatchers.IO) {
                    waterRepository.waterLogs.first().filter { it.date >= sevenDaysAgo }
                }

                // Compute averages
                val avgSteps = if (stepEntries.isEmpty()) 0
                               else stepEntries.sumOf { it.count } / stepEntries.size
                val avgCalories = if (dietEntries.isEmpty()) 0f
                                  else dietEntries.sumOf { it.calories.toDouble() }.toFloat() / 7f
                val avgWater = if (waterEntries.isEmpty()) 0
                               else waterEntries.sumOf { it.ml } / waterEntries.size
                val startWeight = weightEntries.minByOrNull { it.date }?.weight ?: 0f
                val endWeight = weightEntries.maxByOrNull { it.date }?.weight ?: 0f
                val weightChange = endWeight - startWeight

                // Build the AI prompt
                val prompt = """
                    You are ${user.coachName.ifBlank { "Coach" }}, an expert AI fitness analyst.
                    Analyze this week's performance data for ${user.username.ifBlank { "the user" }}.
                    Their goal is: ${user.fitnessGoal}.

                    WEEKLY DATA SUMMARY:
                    - Workouts completed: ${workouts.size} sessions
                    - Workout sessions: ${workouts.joinToString(", ") { "${it.exercises.size} exercises (${it.totalCaloriesBurned} kcal)" }}
                    - Average daily steps: $avgSteps steps
                    - Average daily calories consumed: ${avgCalories.toInt()} kcal
                    - Average daily water intake: $avgWater ml
                    - Weight at start of week: ${startWeight}kg
                    - Weight at end of week: ${endWeight}kg
                    - Total weight change: ${if (weightChange >= 0) "+" else ""}${String.format("%.1f", weightChange)}kg

                    Please provide a structured weekly analysis with EXACTLY these sections:
                    1. 🏆 WINS THIS WEEK
                    Write 2-3 specific positive achievements based on the data above.

                    2. ⚠️ AREAS TO IMPROVE
                    Write 2-3 specific, actionable improvements with concrete targets.

                    3. 📊 NUTRITION ANALYSIS
                    Assess their calorie and macro balance relative to their goal.

                    4. 🎯 NEXT WEEK'S FOCUS
                    Give 3 specific, measurable targets for next week.

                    5. 💬 COACH'S MESSAGE
                    Write a personal motivational paragraph in your coaching style.

                    Be specific, reference the actual numbers provided, keep response under 450 words.
                """.trimIndent()

                val aiAnalysis = apiRepository.generateContent(prompt)

                // Check and deduct credits NOW that the API call was successful
                if (!user.isPremium) {
                    val canAfford = userRepository.updateCredits(3) // update credits subtracts value
                    if (!canAfford) {
                        _uiState.update { it.copy(error = "Failed to deduct credits.", isGenerating = false) }
                        return@launch
                    }
                }

                // Build and save the report
                val report = WeeklyReportEntity(
                    weekStartDate = sevenDaysAgo,
                    weekEndDate = now,
                    averageSteps = avgSteps,
                    totalWorkouts = workouts.size,
                    averageCalories = avgCalories,
                    averageWaterMl = avgWater,
                    weightChangeKg = weightChange,
                    aiAnalysis = aiAnalysis,
                    generatedAt = now
                )
                withContext(Dispatchers.IO) {
                    weeklyReportRepository.insertReport(report)
                }

                // Update UI state
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        currentReport = report,
                        error = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = "Failed to generate report: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
