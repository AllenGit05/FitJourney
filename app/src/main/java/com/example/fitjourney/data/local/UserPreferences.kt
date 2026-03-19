package com.example.fitjourney.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val WORKOUT_REMINDER_ENABLED = booleanPreferencesKey("workout_reminder_enabled")
        private val WORKOUT_REMINDER_TIME = stringPreferencesKey("workout_reminder_time")
        private val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }

    val biometricLockEnabled: Flow<Boolean> = context.userPrefsDataStore.data
        .map { it[BIOMETRIC_LOCK_ENABLED] ?: false }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[BIOMETRIC_LOCK_ENABLED] = enabled }
    }

    val workoutReminderEnabled: Flow<Boolean> = context.userPrefsDataStore.data
        .map { it[WORKOUT_REMINDER_ENABLED] ?: true }

    val workoutReminderTime: Flow<String> = context.userPrefsDataStore.data
        .map { it[WORKOUT_REMINDER_TIME] ?: "08:00" }

    suspend fun setWorkoutReminderEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[WORKOUT_REMINDER_ENABLED] = enabled }
    }

    suspend fun setWorkoutReminderTime(time: String) {
        context.userPrefsDataStore.edit { it[WORKOUT_REMINDER_TIME] = time }
    }
}
