package com.example.fitjourney.ui.client.workout.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.*
import com.example.fitjourney.domain.repository.WorkoutRepository
import com.example.fitjourney.domain.repository.UserRepository
import com.example.fitjourney.domain.repository.ApiRepository
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class GeneratorUiState {
    object Input : GeneratorUiState()
    object Loading : GeneratorUiState()
    data class Success(val plan: WeeklyWorkoutPlan) : GeneratorUiState()
    object Saved : GeneratorUiState()
    data class Error(val message: String) : GeneratorUiState()
}

class WorkoutGeneratorViewModel(
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val apiRepository: ApiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GeneratorUiState>(GeneratorUiState.Input)
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Form State
    var goal = ""
    var level = ""
    var location = ""
    var equipment = ""
    var daysPerWeek = 3
    var durationMinutes = 45

    fun generatePlan() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            
            // Initial check for credits
            if (!user.isPremium && user.aiCredits < 3) {
                _uiState.value = GeneratorUiState.Error("Insufficient credits. 3 credits required for plan generation.")
                return@launch
            }

            _uiState.value = GeneratorUiState.Loading
            
            val prompt = """
                Generate a professional weekly workout plan based on these preferences:
                - Goal: $goal
                - Experience Level: $level
                - Workout Location: $location
                - Available Equipment: $equipment
                - Frequency: $daysPerWeek days per week
                - Session Duration: $durationMinutes minutes
                
                IMPORTANT: Return ONLY a raw JSON object with no markdown formatting, no code blocks, and no extra text.
                The JSON must strictly follow this structure:
                {
                  "weeklySchedule": [
                    {
                      "dayName": "Day 1: [Focus Area]",
                      "exercises": [
                        {
                          "name": "Exercise Name",
                          "sets": 3,
                          "reps": "10-12",
                          "restTimeSeconds": 60
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            try {
                val response = apiRepository.generateContent(prompt)
                val cleanResponse = response.trim()
                    .removePrefix("```json")
                    .removeSuffix("```")
                    .trim()
                
                val workoutDays = try {
                    val jsonObject = JSONObject(cleanResponse)
                    val scheduleArray = jsonObject.getJSONArray("weeklySchedule")
                    val days = mutableListOf<WorkoutDay>()
                    
                    for (i in 0 until scheduleArray.length()) {
                        val dayObj = scheduleArray.getJSONObject(i)
                        val dayName = dayObj.getString("dayName")
                        val exercisesArray = dayObj.getJSONArray("exercises")
                        val exercises = mutableListOf<PlanExercise>()
                        
                        for (j in 0 until exercisesArray.length()) {
                            val exObj = exercisesArray.getJSONObject(j)
                            exercises.add(PlanExercise(
                                name = exObj.getString("name"),
                                sets = exObj.getInt("sets"),
                                reps = exObj.getString("reps"),
                                restTimeSeconds = exObj.getInt("restTimeSeconds")
                            ))
                        }
                        days.add(WorkoutDay(dayName, exercises))
                    }
                    days
                } catch (e: Exception) {
                    _uiState.value = GeneratorUiState.Error("Could not parse workout plan. Please try again.")
                    return@launch
                }
                
                val generatedPlan = WeeklyWorkoutPlan(
                    goal = goal,
                    level = level,
                    location = location,
                    daysPerWeek = daysPerWeek,
                    durationMinutes = durationMinutes,
                    weeklySchedule = workoutDays
                )
                
                // Deduct 3 credits ONLY after successful parse
                val deductionSuccess = userRepository.updateCredits(3)
                if (!deductionSuccess && !user.isPremium) {
                    _uiState.value = GeneratorUiState.Error("Plan generated but credit deduction failed. Please check your balance.")
                    return@launch
                }

                _uiState.value = GeneratorUiState.Success(generatedPlan)
            } catch (e: Exception) {
                _uiState.value = GeneratorUiState.Error("Generation failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun savePlan(plan: WeeklyWorkoutPlan) {
        viewModelScope.launch {
            workoutRepository.saveWorkoutPlan(plan)
            _uiState.value = GeneratorUiState.Saved
        }
    }


    fun reset() {
        _uiState.value = GeneratorUiState.Input
    }
}
