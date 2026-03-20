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
        const val DEFAULT_ADMIN_EMAIL = "admin@fitjourney.com"
        private val ADMIN_EMAIL_KEY = stringPreferencesKey("admin_email")
    }

    suspend fun getAdminEmail(): String {
        return context.adminConfigDataStore.data
            .map { it[ADMIN_EMAIL_KEY] ?: DEFAULT_ADMIN_EMAIL }
            .first()
    }

    suspend fun saveAdminEmail(newEmail: String) {
        context.adminConfigDataStore.edit { prefs ->
            prefs[ADMIN_EMAIL_KEY] = newEmail.trim().lowercase()
        }
    }

    suspend fun isAdminEmail(email: String): Boolean {
        val adminEmail = getAdminEmail()
        return email.trim().lowercase() == adminEmail.lowercase()
    }
}
