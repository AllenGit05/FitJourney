package com.example.fitjourneyag.ui.client.workout.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourneyag.domain.model.*
import com.example.fitjourneyag.domain.repository.WorkoutRepository
import com.example.fitjourneyag.domain.repository.UserRepository
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
    private val userRepository: UserRepository
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
            
            // Deduct 3 credits
            if (!user.isPremium && user.aiCredits < 3) {
                _uiState.value = GeneratorUiState.Error("Insufficient credits. 3 credits required for plan generation.")
                return@launch
            }

            _uiState.value = GeneratorUiState.Loading
            
            val success = userRepository.updateCredits(3)
            if (!success && !user.isPremium) {
                _uiState.value = GeneratorUiState.Error("Credit deduction failed. Please check your balance.")
                return@launch
            }

            // Simulate AI API Call
            delay(2000)
            
            val generatedPlan = simulateAiResponse()
            _uiState.value = GeneratorUiState.Success(generatedPlan)
        }
    }

    fun savePlan(plan: WeeklyWorkoutPlan) {
        viewModelScope.launch {
            workoutRepository.saveWorkoutPlan(plan)
            _uiState.value = GeneratorUiState.Saved
        }
    }

    private fun simulateAiResponse(): WeeklyWorkoutPlan {
        val days = (1..daysPerWeek).map { i ->
            WorkoutDay(
                dayName = "Day $i: ${if (i % 2 == 0) "Lower Body" else "Upper Body"}",
                exercises = listOf(
                    PlanExercise("Push Ups", 3, "12-15", 60),
                    PlanExercise("Squats", 3, "10-12", 90),
                    PlanExercise("Plank", 3, "45s", 45)
                )
            )
        }
        
        return WeeklyWorkoutPlan(
            goal = goal,
            level = level,
            location = location,
            daysPerWeek = daysPerWeek,
            durationMinutes = durationMinutes,
            weeklySchedule = days
        )
    }

    fun reset() {
        _uiState.value = GeneratorUiState.Input
    }
}
