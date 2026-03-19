package com.example.fitjourney.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.adminConfigDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "admin_config")

class AdminConfig(private val context: Context) {

    companion object {
        // Default admin email — stored in DataStore so admin can change it
        const val DEFAULT_ADMIN_EMAIL = "admin@fitjourney.com"
        private val ADMIN_EMAIL_KEY = stringPreferencesKey("admin_email")
    }

    // Get current admin email (from DataStore, fallback to default)
    suspend fun getAdminEmail(): String {
        return context.adminConfigDataStore.data
            .map { it[ADMIN_EMAIL_KEY] ?: DEFAULT_ADMIN_EMAIL }
            .first()
    }

    // Save new admin email to DataStore
    suspend fun saveAdminEmail(newEmail: String) {
        context.adminConfigDataStore.edit { prefs ->
            prefs[ADMIN_EMAIL_KEY] = newEmail.trim().lowercase()
        }
    }

    }
}
