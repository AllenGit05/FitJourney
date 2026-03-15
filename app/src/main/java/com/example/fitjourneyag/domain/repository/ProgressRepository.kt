package com.example.fitjourneyag.domain.repository

import com.example.fitjourneyag.domain.model.ProgressPhoto
import com.example.fitjourneyag.domain.model.WeightEntry
import kotlinx.coroutines.flow.StateFlow

interface ProgressRepository {
    val weightHistory: StateFlow<List<WeightEntry>>
    val progressPhotos: StateFlow<List<ProgressPhoto>>
    val stepsHistory: StateFlow<List<com.example.fitjourneyag.domain.model.StepsEntry>>
    val bodyMeasurements: StateFlow<List<com.example.fitjourneyag.domain.model.BodyMeasurement>>
    
    suspend fun logWeight(weight: Float)
    suspend fun logSteps(count: Int)
    suspend fun addProgressPhoto(imageUrl: String, weight: Float, note: String)
    suspend fun logMeasurements(waist: Float, chest: Float, arms: Float, hips: Float, legs: Float)
    
    suspend fun deleteWeight(entry: WeightEntry)
    suspend fun deleteSteps(entry: com.example.fitjourneyag.domain.model.StepsEntry)
    suspend fun deleteProgressPhoto(photo: ProgressPhoto)
    suspend fun deleteMeasurement(measurement: com.example.fitjourneyag.domain.model.BodyMeasurement)
}
