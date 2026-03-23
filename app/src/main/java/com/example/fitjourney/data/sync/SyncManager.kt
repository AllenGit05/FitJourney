package com.example.fitjourney.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.fitjourney.data.local.FitJourneyDatabase
import com.example.fitjourney.data.local.dao.*
import com.example.fitjourney.data.local.entity.StepsEntity
import com.example.fitjourney.data.remote.*
import com.example.fitjourney.data.remote.FirestoreSchema
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SyncManager(
    private val context: Context,
    private val db: FitJourneyDatabase,
    private val firestore: FirebaseFirestore
) {
    private val scope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    fun startSync() {
        if (!isNetworkAvailable()) return
        val userId = auth.currentUser?.uid ?: return
        
        scope.launch {
            syncWorkouts(userId)
            syncDiet(userId)
            syncWater(userId)
            syncSteps(userId)
            syncWeight(userId)
            syncHabits(userId)
            syncMeasurements(userId)
            syncReports(userId)
            syncPhotos(userId)
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun <T> retry(
        times: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T? {
        var currentDelay = initialDelay
        repeat(times) { iteration ->
            try {
                return block()
            } catch (e: Exception) {
                if (iteration == times - 1) throw e
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        return null
    }

    private suspend fun syncWorkouts(userId: String) {
        val unsynced = db.workoutDao().getUnsyncedSessions().first()
        for (session in unsynced) {
            retry {
                if (session.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WORKOUTS).document(session.id).delete().await()
                    db.workoutDao().deleteSessionById(session.id)
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WORKOUTS).document(session.id)
                        .set(session.toWorkoutFirestoreMap()).await()
                    db.workoutDao().insertSession(session.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    private suspend fun syncDiet(userId: String) {
        val unsynced = db.dietDao().getUnsyncedLogs().first()
        for (log in unsynced) {
            retry {
                if (log.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.DIET).document(log.id.toString()).delete().await()
                    db.dietDao().deleteLogById(log.id)
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.DIET).document(log.id.toString())
                        .set(log.toDietFirestoreMap()).await()
                    db.dietDao().insertLog(log.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }

            }
        }
    }

    private suspend fun syncWater(userId: String) {
        val unsynced = db.waterDao().getUnsyncedWater().first()
        for (entry in unsynced) {
            retry {
                if (entry.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WATER).document(entry.id).delete().await()
                    db.waterDao().deleteWaterById(entry.id)
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WATER).document(entry.id)
                        .set(entry.toWaterFirestoreMap()).await()
                    db.waterDao().insertWater(entry.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }

            }
        }
    }

    private suspend fun syncSteps(userId: String) {
        val unsynced = db.stepDao().getUnsyncedSteps().first()
        for (entry: StepsEntity in unsynced) {
            retry {
                if (entry.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.STEPS).document(entry.id).delete().await()
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.STEPS).document(entry.id)
                        .set(entry.toStepsFirestoreMap()).await()
                    db.stepDao().insertSteps(entry.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }

            }
        }
    }

    private suspend fun syncWeight(userId: String) {
        val unsynced = db.weightDao().getUnsyncedWeight().first()
        for (entry in unsynced) {
            retry {
                if (entry.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WEIGHT).document(entry.id).delete().await()
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WEIGHT).document(entry.id)
                        .set(entry.toWeightFirestoreMap()).await()
                    db.weightDao().insertWeight(entry.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }

            }
        }
    }

    private suspend fun syncHabits(userId: String) {
        val unsynced = db.habitDao().getUnsyncedHabits().first()
        for (habit in unsynced) {
            retry {
                firestore.collection(FirestoreSchema.USERS).document(userId)
                    .collection(FirestoreSchema.HABITS).document(habit.id)
                    .set(habit.toHabitFirestoreMap()).await()
                db.habitDao().insertHabit(habit.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            }
        }
    }

    private suspend fun syncMeasurements(userId: String) {
        val unsynced = db.bodyMeasurementDao().getUnsyncedMeasurements().first()
        for (m in unsynced) {
            retry {
                firestore.collection(FirestoreSchema.USERS).document(userId)
                    .collection(FirestoreSchema.BODY_MEASUREMENTS).document(m.id)
                    .set(m.toMeasurementFirestoreMap()).await()
                db.bodyMeasurementDao().insertMeasurement(m.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            }
        }
    }

    private suspend fun syncReports(userId: String) {
        val unsynced = db.weeklyReportDao().getUnsyncedReports().first()
        for (r in unsynced) {
            retry {
                firestore.collection(FirestoreSchema.USERS).document(userId)
                    .collection(FirestoreSchema.WEEKLY_REPORTS).document(r.id)
                    .set(r.toReportFirestoreMap()).await()
                db.weeklyReportDao().insertReport(r.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            }
        }
    }
    private suspend fun syncPhotos(userId: String) {
        val unsynced = db.photoDao().getUnsyncedPhotos().first()
        for (photo in unsynced) {
            retry {
                if (photo.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.PROGRESS).document(photo.id).delete().await()
                    // Note: We don't delete from Storage here to avoid complexity with StorageRepo dependency in SyncManager, 
                    // or we could add it. For now, Firestore deletion is standard.
                    db.photoDao().deletePhoto(photo)
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.PROGRESS).document(photo.id)
                        .set(photo.toPhotoFirestoreMap()).await()

                    db.photoDao().insertPhoto(photo.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }

            }
        }
    }
}

