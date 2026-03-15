package com.example.fitjourneyag.ui.client.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourneyag.data.local.entity.WeeklyReportEntity
import com.example.fitjourneyag.domain.repository.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
