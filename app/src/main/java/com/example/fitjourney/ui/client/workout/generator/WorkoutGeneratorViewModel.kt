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
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

            if (!user.isPremium && user.aiCredits < 3) {
                _uiState.value = GeneratorUiState.Error(
                    "Insufficient credits. 3 credits required."
                )
                return@launch
            }

            _uiState.value = GeneratorUiState.Loading

            try {
                val prompt = """
                    Generate a $daysPerWeek-day workout plan for someone with these details:
                    Goal: $goal, Level: $level, Location: $location, Equipment: $equipment,
                    Session duration: $durationMinutes minutes.
                    Return ONLY a JSON object, no markdown, no explanation. Format:
                    {"weeklySchedule":[{"dayName":"string","exercises":[{"name":"string","sets":0,"reps":"string","restTimeSeconds":0}]}]}
                """.trimIndent()

                val resultJson = apiRepository.generateContent(prompt)
                
                val jsonStr = resultJson.substringAfter("{").substringBeforeLast("}")
                val json = org.json.JSONObject("{$jsonStr}")
                val scheduleArray = json.getJSONArray("weeklySchedule")
                val days = (0 until scheduleArray.length()).map { i ->
                    val dayObj = scheduleArray.getJSONObject(i)
                    val exArray = dayObj.getJSONArray("exercises")
                    val exercises = (0 until exArray.length()).map { j ->
                        val ex = exArray.getJSONObject(j)
                        PlanExercise(
                            name = ex.getString("name"),
                            sets = ex.getInt("sets"),
                            reps = ex.getString("reps"),
                            restTimeSeconds = ex.getInt("restTimeSeconds")
                        )
                    }
                    WorkoutDay(
                        dayName = dayObj.getString("dayName"),
                        exercises = exercises
                    )
                }

                // Only deduct credits AFTER successful parse
                if (!user.isPremium) {
                    userRepository.updateCredits(3)
                }

                _uiState.value = GeneratorUiState.Success(
                    WeeklyWorkoutPlan(
                        goal = goal,
                        level = level,
                        location = location,
                        daysPerWeek = daysPerWeek,
                        durationMinutes = durationMinutes,
                        weeklySchedule = days
                    )
                )
            } catch (e: org.json.JSONException) {
                _uiState.value = GeneratorUiState.Error(
                    "Could not parse the AI response. Please try again."
                )
            } catch (e: Exception) {
                _uiState.value = GeneratorUiState.Error(
                    e.message ?: "AI request failed. Please try again."
                )
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
