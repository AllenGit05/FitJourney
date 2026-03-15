package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.domain.model.ProgressPhoto
import com.example.fitjourneyag.domain.model.WeightEntry
import com.example.fitjourneyag.domain.repository.ProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class ProgressRepositoryImpl(
    private val stepsDao: com.example.fitjourneyag.data.local.dao.StepsDao,
    private val weightDao: com.example.fitjourneyag.data.local.dao.WeightDao,
    private val photoDao: com.example.fitjourneyag.data.local.dao.PhotoDao,
    private val bodyMeasurementDao: com.example.fitjourneyag.data.local.dao.BodyMeasurementDao
) : ProgressRepository {

    override val weightHistory: StateFlow<List<WeightEntry>> = weightDao.getAllWeight()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override val progressPhotos: StateFlow<List<ProgressPhoto>> = photoDao.getAllPhotos()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override val stepsHistory: StateFlow<List<com.example.fitjourneyag.domain.model.StepsEntry>> = stepsDao.getAllSteps()
        .map { entities -> 
            entities.map { com.example.fitjourneyag.domain.model.StepsEntry(id = it.id, date = it.date, count = it.count) }
        }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override val bodyMeasurements: StateFlow<List<com.example.fitjourneyag.domain.model.BodyMeasurement>> = bodyMeasurementDao.getAllMeasurements()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override suspend fun logWeight(weight: Float) {
        weightDao.insertWeight(com.example.fitjourneyag.data.local.entity.WeightEntity(weight = weight))
    }

    override suspend fun logSteps(count: Int) {
        val entity = com.example.fitjourneyag.data.local.entity.StepsEntity(count = count)
        stepsDao.insertSteps(entity)
    }

    override suspend fun addProgressPhoto(imageUrl: String, weight: Float, note: String) {
        photoDao.insertPhoto(com.example.fitjourneyag.data.local.entity.ProgressPhotoEntity(imageUrl = imageUrl, weight = weight, note = note))
    }

    override suspend fun logMeasurements(waist: Float, chest: Float, arms: Float, hips: Float, legs: Float) {
        bodyMeasurementDao.insertMeasurement(
            com.example.fitjourneyag.data.local.entity.BodyMeasurementEntity(
                waist = waist, chest = chest, arms = arms, hips = hips, legs = legs
            )
        )
    }

    override suspend fun deleteWeight(entry: WeightEntry) {
        weightDao.deleteWeight(com.example.fitjourneyag.data.local.entity.WeightEntity(id = entry.id, date = entry.date, weight = entry.weight))
    }

    override suspend fun deleteSteps(entry: com.example.fitjourneyag.domain.model.StepsEntry) {
        stepsDao.deleteSteps(com.example.fitjourneyag.data.local.entity.StepsEntity(id = entry.id, date = entry.date, count = entry.count))
    }

    override suspend fun deleteProgressPhoto(photo: ProgressPhoto) {
        photoDao.deletePhoto(com.example.fitjourneyag.data.local.entity.ProgressPhotoEntity(id = photo.id, date = photo.date, imageUrl = photo.imageUrl, weight = photo.weight, note = photo.note))
    }

    override suspend fun deleteMeasurement(measurement: com.example.fitjourneyag.domain.model.BodyMeasurement) {
        bodyMeasurementDao.deleteMeasurement(
            com.example.fitjourneyag.data.local.entity.BodyMeasurementEntity(
                id = measurement.id, date = measurement.date, waist = measurement.waist, chest = measurement.chest, arms = measurement.arms, hips = measurement.hips, legs = measurement.legs
            )
        )
    }

    private fun com.example.fitjourneyag.data.local.entity.WeightEntity.toDomain() = WeightEntry(id = id, date = date, weight = weight)
    private fun com.example.fitjourneyag.data.local.entity.ProgressPhotoEntity.toDomain() = ProgressPhoto(id = id, date = date, imageUrl = imageUrl, weight = weight, note = note)
    private fun com.example.fitjourneyag.data.local.entity.BodyMeasurementEntity.toDomain() = com.example.fitjourneyag.domain.model.BodyMeasurement(
        id = id, date = date, waist = waist, chest = chest, arms = arms, hips = hips, legs = legs
    )
}
