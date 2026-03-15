package com.example.fitjourney.ui.client.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.*
import com.example.fitjourney.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class WorkoutTrackingViewModel(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val apiRepository: com.example.fitjourney.domain.repository.ApiRepository
) : ViewModel() {

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()
    
    private val _workoutSession = MutableStateFlow<List<Exercise>>(emptyList())
    val workoutSession: StateFlow<List<Exercise>> = _workoutSession.asStateFlow()
    
    val activePlan: StateFlow<WeeklyWorkoutPlan?> = workoutRepository.activePlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _totalCaloriesBurned = MutableStateFlow(0)
    val totalCaloriesBurned: StateFlow<Int> = _totalCaloriesBurned.asStateFlow()

    private val _totalDurationMinutes = MutableStateFlow(0)
    val totalDurationMinutes: StateFlow<Int> = _totalDurationMinutes.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    // ── Rest Timer Logic ──────────────────────────────────────────
    private val _restTimeRemaining = MutableStateFlow(0) // Seconds
    val restTimeRemaining: StateFlow<Int> = _restTimeRemaining.asStateFlow()

    fun startRestTimer(seconds: Int) {
        _restTimeRemaining.value = seconds
        viewModelScope.launch {
            while (_restTimeRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                _restTimeRemaining.value -= 1
            }
        }
    }

    // ── Exercise & Set Management ───────────────────────────────
    fun addExercise(name: String, sets: List<WorkoutSet>, duration: Int, calories: Int, restTime: Int) {
        val exercise = Exercise(name, sets, duration, calories, restTime)
        _workoutSession.value = _workoutSession.value + exercise
        recalculateTotals()
    }

    fun addSetToExercise(exerciseName: String) {
        _workoutSession.value = _workoutSession.value.map { exercise ->
            if (exercise.name == exerciseName) {
                val lastSet = exercise.sets.lastOrNull() ?: WorkoutSet(10, 0f)
                exercise.copy(sets = exercise.sets + WorkoutSet(lastSet.reps, lastSet.weight))
            } else exercise
        }
        recalculateTotals()
    }

    fun toggleSetCompletion(exerciseName: String, setIndex: Int) {
        _workoutSession.value = _workoutSession.value.map { exercise ->
            if (exercise.name == exerciseName) {
                val newSets = exercise.sets.toMutableList()
                val updatedSet = newSets[setIndex].copy(isCompleted = !newSets[setIndex].isCompleted)
                newSets[setIndex] = updatedSet
                
                // Trigger rest timer if completed
                if (updatedSet.isCompleted) startRestTimer(exercise.restTimeSeconds) 
                
                exercise.copy(sets = newSets)
            } else exercise
        }
        recalculateTotals()
    }

    fun updateSetValues(exerciseName: String, setIndex: Int, reps: Int, weight: Float) {
        _workoutSession.value = _workoutSession.value.map { exercise ->
            if (exercise.name == exerciseName) {
                val newSets = exercise.sets.toMutableList()
                newSets[setIndex] = newSets[setIndex].copy(reps = reps, weight = weight)
                exercise.copy(sets = newSets)
            } else exercise
        }
        recalculateTotals()
    }

    private fun recalculateTotals() {
        _totalCaloriesBurned.value = _workoutSession.value.sumOf { it.caloriesBurned }
        _totalDurationMinutes.value = _workoutSession.value.sumOf { it.durationMinutes }
    }

    fun finishWorkout() {
        if (_workoutSession.value.isEmpty()) return
        
        viewModelScope.launch {
            val session = WorkoutSession(
                totalDurationMinutes = _totalDurationMinutes.value,
                totalCaloriesBurned = _totalCaloriesBurned.value,
                exercises = _workoutSession.value
            )
            workoutRepository.saveWorkout(session)
            _isFinished.value = true
        }
    }

    // ── AI Calorie Suggestion Logic ───────────────────────────────
    fun calculateSessionCaloriesAI() {
        if (_workoutSession.value.isEmpty()) return
        
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiError.value = null
            
            try {
                val sessionDetails = _workoutSession.value.joinToString("\n") { ex ->
                    "${ex.name}: ${ex.sets.size} sets, reps: ${ex.sets.joinToString(",") { it.reps.toString() }}, weight: ${ex.sets.joinToString(",") { it.weight.toString() }}kg"
                }

                val prompt = """
                    Calculate the total estimated calories burned for this entire workout session:
                    $sessionDetails
                    
                    Return ONLY a single integer representing the total calories burned. No text, no units.
                """.trimIndent()

                val resultText = apiRepository.generateContent(prompt)

                val calories = resultText.filter { it.isDigit() }.toIntOrNull() ?: 0
                _totalCaloriesBurned.value = calories
            } catch (e: Exception) {
                _aiError.value = e.message ?: "AI calculation failed"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

}
