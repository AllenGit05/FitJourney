package com.example.fitjourney

import android.app.Application
import com.example.fitjourney.di.AppContainer
import com.example.fitjourney.di.DefaultAppContainer

class FitJourneyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        try {
            container = DefaultAppContainer(this)
            // Fix 12: Trigger immediate sync on launch
            container.syncManager.startSync()
        } catch (e: Exception) {

            e.printStackTrace()
            // Create a minimal container so the app does not crash
            container = DefaultAppContainer(this)
        }
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Workout Reminders"
            val descriptionText = "Reminders to log your daily workouts"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel("workout_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

// Final sync for Admin Routing and Profile Management
