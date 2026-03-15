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
    private val scope = CoroutineScope(Dispatchers.IO)

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
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun syncWorkouts(userId: String) {
        val unsynced = db.workoutDao().getUnsyncedSessions().first()
        for (session in unsynced) {
            try {
                if (session.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WORKOUTS).document(session.id).delete().await()
                    // Specific delete should be handled in DAO, for now keeping as is but fixing loop
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WORKOUTS).document(session.id)
                        .set(session.toWorkoutFirestoreMap()).await()
                    db.workoutDao().insertSession(session.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncDiet(userId: String) {
        val unsynced = db.dietDao().getUnsyncedLogs().first()
        for (log in unsynced) {
            try {
                if (log.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.DIET_ENTRIES).document(log.id.toString()).delete().await()
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.DIET_ENTRIES).document(log.id.toString())
                        .set(log.toDietFirestoreMap()).await()
                    db.dietDao().insertLog(log.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncWater(userId: String) {
        val unsynced = db.waterDao().getUnsyncedWater().first()
        for (entry in unsynced) {
            try {
                if (entry.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WATER_ENTRIES).document(entry.id).delete().await()
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WATER_ENTRIES).document(entry.id)
                        .set(entry.toWaterFirestoreMap()).await()
                    db.waterDao().insertWater(entry.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncSteps(userId: String) {
        val unsynced = db.stepDao().getUnsyncedSteps().first()
        for (entry: StepsEntity in unsynced) {
            try {
                if (entry.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.STEPS_ENTRIES).document(entry.id).delete().await()
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.STEPS_ENTRIES).document(entry.id)
                        .set(entry.toStepsFirestoreMap()).await()
                    db.stepDao().insertSteps(entry.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncWeight(userId: String) {
        val unsynced = db.weightDao().getUnsyncedWeight().first()
        for (entry in unsynced) {
            try {
                if (entry.isDeleted) {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WEIGHT_ENTRIES).document(entry.id).delete().await()
                } else {
                    firestore.collection(FirestoreSchema.USERS).document(userId)
                        .collection(FirestoreSchema.WEIGHT_ENTRIES).document(entry.id)
                        .set(entry.toWeightFirestoreMap()).await()
                    db.weightDao().insertWeight(entry.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncHabits(userId: String) {
        val unsynced = db.habitDao().getUnsyncedHabits().first()
        for (habit in unsynced) {
            try {
                firestore.collection(FirestoreSchema.USERS).document(userId)
                    .collection(FirestoreSchema.HABITS).document(habit.id)
                    .set(habit.toHabitFirestoreMap()).await()
                db.habitDao().insertHabit(habit.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncMeasurements(userId: String) {
        val unsynced = db.bodyMeasurementDao().getUnsyncedMeasurements().first()
        for (m in unsynced) {
            try {
                firestore.collection(FirestoreSchema.USERS).document(userId)
                    .collection(FirestoreSchema.BODY_MEASUREMENTS).document(m.id)
                    .set(m.toMeasurementFirestoreMap()).await()
                db.bodyMeasurementDao().insertMeasurement(m.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun syncReports(userId: String) {
        val unsynced = db.weeklyReportDao().getUnsyncedReports().first()
        for (r in unsynced) {
            try {
                firestore.collection(FirestoreSchema.USERS).document(userId)
                    .collection(FirestoreSchema.WEEKLY_REPORTS).document(r.id)
                    .set(r.toReportFirestoreMap()).await()
                db.weeklyReportDao().insertReport(r.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
