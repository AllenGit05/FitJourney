package com.example.fitjourney.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseStorageRepository(private val storage: FirebaseStorage) {

    suspend fun uploadProgressPhoto(userId: String, photoId: String, uri: Uri): String {
        val ref = storage.reference.child("users/$userId/progress_photos/$photoId.jpg")
        val uploadTask = ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteProgressPhoto(userId: String, photoId: String) {
        val ref = storage.reference.child("users/$userId/progress_photos/$photoId.jpg")
        try {
            ref.delete().await()
        } catch (e: Exception) {
            // Might not exist in storage
        }
    }
}
