package com.example.fitjourney.data.repository

import com.example.fitjourney.domain.model.ProgressPhoto
import com.example.fitjourney.domain.model.WeightEntry
import com.example.fitjourney.domain.repository.ProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class ProgressRepositoryImpl(
    private val stepsDao: com.example.fitjourney.data.local.dao.StepsDao,
    private val weightDao: com.example.fitjourney.data.local.dao.WeightDao,
    private val photoDao: com.example.fitjourney.data.local.dao.PhotoDao,
    private val bodyMeasurementDao: com.example.fitjourney.data.local.dao.BodyMeasurementDao,
    private val storageRepo: com.example.fitjourney.data.remote.FirebaseStorageRepository,
    private val auth: com.google.firebase.auth.FirebaseAuth,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : ProgressRepository {

    override val weightHistory: StateFlow<List<WeightEntry>> = weightDao.getAllWeight()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override val progressPhotos: StateFlow<List<ProgressPhoto>> = photoDao.getAllPhotos()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override val stepsHistory: StateFlow<List<com.example.fitjourney.domain.model.StepsEntry>> = stepsDao.getAllSteps()
        .map { entities -> 
            entities.map { com.example.fitjourney.domain.model.StepsEntry(id = it.id, date = it.date, count = it.count) }
        }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override val bodyMeasurements: StateFlow<List<com.example.fitjourney.domain.model.BodyMeasurement>> = bodyMeasurementDao.getAllMeasurements()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())

    override suspend fun logWeight(weight: Float) {
        weightDao.insertWeight(com.example.fitjourney.data.local.entity.WeightEntity(weight = weight, isSynced = false))
        syncManager.startSync()
    }

    override suspend fun logSteps(count: Int) {
        val entity = com.example.fitjourney.data.local.entity.StepsEntity(count = count, isSynced = false)
        stepsDao.insertSteps(entity)
        syncManager.startSync()
    }

    override suspend fun addProgressPhoto(imageUrl: String, weight: Float, note: String) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val photoId = java.util.UUID.randomUUID().toString()
        
        // Upload to Cloud (imageUrl here is expected to be a local URI from camera)
        val cloudUrl = try {
            storageRepo.uploadProgressPhoto(userId, photoId, java.io.File(imageUrl))
        } catch (e: Exception) {
            imageUrl // Fallback to local
        }

        photoDao.insertPhoto(com.example.fitjourney.data.local.entity.ProgressPhotoEntity(
            id = photoId, 
            imageUrl = cloudUrl, 
            weight = weight, 
            note = note,
            isSynced = cloudUrl != imageUrl
        ))
    }

    override suspend fun logMeasurements(waist: Float, chest: Float, arms: Float, hips: Float, legs: Float) {
        bodyMeasurementDao.insertMeasurement(
            com.example.fitjourney.data.local.entity.BodyMeasurementEntity(
                waist = waist, chest = chest, arms = arms, hips = hips, legs = legs, isSynced = false
            )
        )
        syncManager.startSync()
    }

    override suspend fun deleteWeight(entry: WeightEntry) {
        weightDao.softDelete(entry.id)
        syncManager.startSync()
    }

    override suspend fun deleteSteps(entry: com.example.fitjourney.domain.model.StepsEntry) {
        stepsDao.softDelete(entry.id)
        syncManager.startSync()
    }

    override suspend fun deleteProgressPhoto(photo: ProgressPhoto) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        
        // Delete from Cloud
        storageRepo.deleteProgressPhoto(userId, photo.id)
        
        // Delete from Local (or soft delete)
        photoDao.deletePhoto(com.example.fitjourney.data.local.entity.ProgressPhotoEntity(id = photo.id, date = photo.date, imageUrl = photo.imageUrl, weight = photo.weight, note = photo.note))
    }

    override suspend fun deleteMeasurement(measurement: com.example.fitjourney.domain.model.BodyMeasurement) {
        bodyMeasurementDao.softDelete(measurement.id)
        syncManager.startSync()
    }

    private fun com.example.fitjourney.data.local.entity.WeightEntity.toDomain() = WeightEntry(id = id, date = date, weight = weight)
    private fun com.example.fitjourney.data.local.entity.ProgressPhotoEntity.toDomain() = ProgressPhoto(id = id, date = date, imageUrl = imageUrl, weight = weight, note = note)
    private fun com.example.fitjourney.data.local.entity.BodyMeasurementEntity.toDomain() = com.example.fitjourney.domain.model.BodyMeasurement(
        id = id, date = date, waist = waist, chest = chest, arms = arms, hips = hips, legs = legs
    )
}
