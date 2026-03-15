package com.example.fitjourney.ui.client.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.*
import com.example.fitjourney.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProgressViewModel(
    private val workoutRepository: WorkoutRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    val photos: StateFlow<List<ProgressPhoto>> = progressRepository.progressPhotos
    val weightHistory: StateFlow<List<WeightEntry>> = progressRepository.weightHistory
    val bodyMeasurements: StateFlow<List<com.example.fitjourney.domain.model.BodyMeasurement>> = progressRepository.bodyMeasurements

    val workoutHistory: StateFlow<List<WorkoutSession>> = workoutRepository.workoutHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise.asStateFlow()

    val availableExercises: StateFlow<List<String>> = workoutHistory.map { history ->
        history.flatMap { it.exercises }.map { it.name }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Strength Progress: Max weight per exercise over time, filtered if selectedExercise is set
    val strengthProgress: StateFlow<List<StrengthPoint>> = combine(workoutHistory, _selectedExercise) { history, selected ->
        history.flatMap { session ->
            session.exercises.filter { selected == null || it.name == selected }.map { exercise ->
                StrengthPoint(
                    exerciseName = exercise.name,
                    date = session.date,
                    maxWeight = exercise.sets.maxOfOrNull { it.weight } ?: 0f
                )
            }
        }.sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workout Frequency: sessions per last 7 days
    val workoutFrequency: StateFlow<Int> = workoutHistory.map { history ->
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        history.count { it.date >= weekAgo }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logWeight(weight: Float) {
        viewModelScope.launch {
            progressRepository.logWeight(weight)
        }
    }

    fun uploadPhoto(imageUrl: String, weight: Float, note: String) {
        viewModelScope.launch {
            progressRepository.addProgressPhoto(imageUrl, weight, note)
        }
    }

    fun logMeasurements(waist: Float, chest: Float, arms: Float, hips: Float, legs: Float) {
        viewModelScope.launch {
            progressRepository.logMeasurements(waist, chest, arms, hips, legs)
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            progressRepository.deleteWeight(entry)
        }
    }

    fun deletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            progressRepository.deleteProgressPhoto(photo)
        }
    }

    fun deleteMeasurement(measurement: com.example.fitjourney.domain.model.BodyMeasurement) {
        viewModelScope.launch {
            progressRepository.deleteMeasurement(measurement)
        }
    }

    fun selectExercise(exerciseName: String?) {
        _selectedExercise.value = exerciseName
    }
}
