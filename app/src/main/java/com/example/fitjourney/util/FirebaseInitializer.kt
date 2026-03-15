package com.example.fitjourney.util

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseInitializer {

    private val COLLECTIONS = listOf(
        "users", "workouts", "diet_entries", "water_entries",
        "steps_entries", "weight_entries", "chat_messages",
        "habits", "progress_photo_metadata", "weekly_reports", "sync_metadata"
    )

    /**
     * Call this from Admin screen → "Reset Firebase" button
     * Wipes top-level data. Note: Better to handle sub-collections recursively in production.
     */
    suspend fun wipeAndReinitialize(db: FirebaseFirestore) {
        COLLECTIONS.forEach { collectionName ->
            try {
                val snapshot = db.collection(collectionName).get().await()
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                if (snapshot.documents.isNotEmpty()) {
                    batch.commit().await()
                }
            } catch (e: Exception) {
                // Collection doesn't exist yet or permission error
            }
        }
    }
}
