package com.example.fitjourney.data.repository

import com.example.fitjourney.domain.model.ProgressPhoto
import com.example.fitjourney.domain.model.WeightEntry
import com.example.fitjourney.domain.repository.ProgressRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable

class ProgressRepositoryImpl(
    private val context: android.content.Context,
    private val stepsDao: com.example.fitjourney.data.local.dao.StepsDao,
    private val weightDao: com.example.fitjourney.data.local.dao.WeightDao,
    private val photoDao: com.example.fitjourney.data.local.dao.PhotoDao,
    private val bodyMeasurementDao: com.example.fitjourney.data.local.dao.BodyMeasurementDao,
    private val storageRepo: com.example.fitjourney.data.remote.FirebaseStorageRepository,
    private val fireauth: com.google.firebase.auth.FirebaseAuth,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : ProgressRepository, Closeable {

    // The scope's lifetime matches the Application lifecycle as this is a singleton in AppContainer.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val weightHistory: StateFlow<List<WeightEntry>> = weightDao.getAllWeight()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override val progressPhotos: StateFlow<List<ProgressPhoto>> = photoDao.getAllPhotos()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override val stepsHistory: StateFlow<List<com.example.fitjourney.domain.model.StepsEntry>> = stepsDao.getAllSteps()
        .map { entities -> 
            entities.map { com.example.fitjourney.domain.model.StepsEntry(id = it.id, date = it.date, count = it.count) }
        }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override val bodyMeasurements: StateFlow<List<com.example.fitjourney.domain.model.BodyMeasurement>> = bodyMeasurementDao.getAllMeasurements()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override suspend fun logWeight(weight: Float) {
        weightDao.insertWeight(com.example.fitjourney.data.local.entity.WeightEntity(weight = weight, isSynced = false))
        syncManager.startSync()
    }

    override suspend fun logSteps(count: Int) {
        val entity = com.example.fitjourney.data.local.entity.StepsEntity(count = count, isSynced = false)
        stepsDao.insertSteps(entity)
        syncManager.startSync()
    }

    override suspend fun addProgressPhoto(imageUri: android.net.Uri, weight: Float, note: String) {
        val userId = fireauth.currentUser?.uid ?: "anonymous"
        val photoId = java.util.UUID.randomUUID().toString()

        val imageUrl = imageUri.toString()
        val tempFile = java.io.File(context.cacheDir, "photo_upload_$photoId.jpg")
        val uploadedUrl = try {
            if (imageUrl.startsWith("content://")) {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                storageRepo.uploadProgressPhoto(userId, photoId, android.net.Uri.fromFile(tempFile))
            } else {
                val file = java.io.File(imageUrl)
                if (file.exists()) {
                    storageRepo.uploadProgressPhoto(userId, photoId, android.net.Uri.fromFile(file))
                } else {
                    imageUrl
                }
            }
        } catch (e: Exception) {
            imageUrl
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }

        photoDao.insertPhoto(
            com.example.fitjourney.data.local.entity.ProgressPhotoEntity(
                id = photoId,
                imageUrl = uploadedUrl,
                weight = weight,
                note = note,
                isSynced = uploadedUrl != imageUrl
            )
        )
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
        val userId = fireauth.currentUser?.uid ?: return
        photoDao.softDelete(photo.id)
        storageRepo.deleteProgressPhoto(userId, photo.id)
        syncManager.startSync()
    }




    override suspend fun deleteMeasurement(measurement: com.example.fitjourney.domain.model.BodyMeasurement) {
        bodyMeasurementDao.softDelete(measurement.id)
        syncManager.startSync()
    }

    override fun close() {
        repositoryScope.cancel()
    }

    private fun com.example.fitjourney.data.local.entity.WeightEntity.toDomain() = WeightEntry(id = id, date = date, weight = weight)
    private fun com.example.fitjourney.data.local.entity.ProgressPhotoEntity.toDomain() = ProgressPhoto(id = id, date = date, imageUrl = imageUrl, weight = weight, note = note)
    private fun com.example.fitjourney.data.local.entity.BodyMeasurementEntity.toDomain() = com.example.fitjourney.domain.model.BodyMeasurement(
        id = id, date = date, waist = waist, chest = chest, arms = arms, hips = hips, legs = legs
    )
}
