package com.example.fitjourneyag.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.apiKeyDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "api_keys")

class ApiKeyStore(private val context: Context) {

    companion object {
        private val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        private val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
    }

    // ── Gemini ──────────────────────────────────────────────
    val geminiApiKey: Flow<String> = context.apiKeyDataStore.data
        .map { it[GEMINI_API_KEY] ?: "" }

    suspend fun getGeminiApiKey(): String =
        context.apiKeyDataStore.data.first()[GEMINI_API_KEY] ?: ""

    suspend fun saveGeminiApiKey(key: String) {
        context.apiKeyDataStore.edit { it[GEMINI_API_KEY] = key.trim() }
    }

    suspend fun clearGeminiApiKey() {
        context.apiKeyDataStore.edit { it.remove(GEMINI_API_KEY) }
    }

    fun isGeminiKeySet(): Flow<Boolean> = geminiApiKey.map { it.isNotBlank() }

    // ── Groq ────────────────────────────────────────────────
    val groqApiKey: Flow<String> = context.apiKeyDataStore.data
        .map { it[GROQ_API_KEY] ?: "" }

    suspend fun getGroqApiKey(): String =
        context.apiKeyDataStore.data.first()[GROQ_API_KEY] ?: ""

    suspend fun saveGroqApiKey(key: String) {
        context.apiKeyDataStore.edit { it[GROQ_API_KEY] = key.trim() }
    }

    suspend fun clearGroqApiKey() {
        context.apiKeyDataStore.edit { it.remove(GROQ_API_KEY) }
    }

    fun isGroqKeySet(): Flow<Boolean> = groqApiKey.map { it.isNotBlank() }

    // ── Helper: check if both keys are configured ────────────
    fun areBothKeysSet(): Flow<Boolean> = context.apiKeyDataStore.data
        .map { prefs ->
            val gemini = prefs[GEMINI_API_KEY] ?: ""
            val groq = prefs[GROQ_API_KEY] ?: ""
            gemini.isNotBlank() && groq.isNotBlank()
        }
}
