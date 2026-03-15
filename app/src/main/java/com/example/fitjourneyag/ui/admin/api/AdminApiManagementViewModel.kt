package com.example.fitjourneyag.ui.admin.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourneyag.data.local.ApiKeyStore
import com.example.fitjourneyag.data.remote.GroqApiClient
import com.example.fitjourneyag.util.RateLimitHandler
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminApiUiState(
    // Gemini
    val geminiKeyInput: String = "",
    val geminiKeyVisible: Boolean = false,
    val isGeminiKeySaved: Boolean = false,
    val geminiStatus: String = "UNKNOWN", // UNKNOWN, TESTING, ONLINE, ERROR, RATE_LIMITED
    val geminiLatencyMs: Long = 0L,
    val geminiStatusMessage: String = "",
    val isTestingGemini: Boolean = false,

    // Groq
    val groqKeyInput: String = "",
    val groqKeyVisible: Boolean = false,
    val isGroqKeySaved: Boolean = false,
    val groqStatus: String = "UNKNOWN",
    val groqLatencyMs: Long = 0L,
    val groqStatusMessage: String = "",
    val isTestingGroq: Boolean = false,

    // General
    val successMessage: String? = null,
    val testResponse: String? = null,
    val isLiveTesting: Boolean = false
)

class AdminApiManagementViewModel(
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    private val groqClient = GroqApiClient()

    private val _uiState = MutableStateFlow(AdminApiUiState())
    val uiState: StateFlow<AdminApiUiState> = _uiState.asStateFlow()

    init {
        // Load saved keys on launch
        viewModelScope.launch {
            val savedGemini = apiKeyStore.getGeminiApiKey()
            val savedGroq = apiKeyStore.getGroqApiKey()
            _uiState.update {
                it.copy(
                    geminiKeyInput = savedGemini,
                    isGeminiKeySaved = savedGemini.isNotBlank(),
                    groqKeyInput = savedGroq,
                    isGroqKeySaved = savedGroq.isNotBlank()
                )
            }
        }
    }

    // ── Gemini ────────────────────────────────────────────
    fun onGeminiKeyChanged(key: String) {
        _uiState.update { it.copy(geminiKeyInput = key, isGeminiKeySaved = false) }
    }

    fun toggleGeminiKeyVisibility() {
        _uiState.update { it.copy(geminiKeyVisible = !it.geminiKeyVisible) }
    }

    fun saveGeminiKey() {
        viewModelScope.launch {
            val key = _uiState.value.geminiKeyInput.trim()
            if (key.isBlank()) return@launch
            apiKeyStore.saveGeminiApiKey(key)
            _uiState.update {
                it.copy(
                    isGeminiKeySaved = true,
                    geminiStatus = "UNKNOWN",
                    successMessage = "Gemini key saved successfully"
                )
            }
        }
    }

    fun deleteGeminiKey() {
        viewModelScope.launch {
            apiKeyStore.clearGeminiApiKey()
            _uiState.update {
                it.copy(
                    geminiKeyInput = "",
                    isGeminiKeySaved = false,
                    geminiStatus = "UNKNOWN",
                    geminiStatusMessage = ""
                )
            }
        }
    }

    fun testGeminiConnection() {
        viewModelScope.launch {
            val key = _uiState.value.geminiKeyInput.trim()
            if (key.isBlank()) {
                _uiState.update {
                    it.copy(
                        geminiStatus = "ERROR",
                        geminiStatusMessage = "Enter a key first."
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isTestingGemini = true, geminiStatusMessage = "") }
            val start = System.currentTimeMillis()
            try {
                val model = GenerativeModel(
                    modelName = "gemini-2.0-flash-lite",
                    apiKey = key
                )
                withContext(Dispatchers.IO) { model.generateContent("Say OK") }
                val latency = System.currentTimeMillis() - start
                _uiState.update {
                    it.copy(
                        isTestingGemini = false,
                        geminiStatus = "ONLINE",
                        geminiLatencyMs = latency,
                        geminiStatusMessage = "Connected (${latency}ms)"
                    )
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - start
                val msg = e.message ?: "Unknown error"
                val isRateLimit = RateLimitHandler.isRateLimitError(msg)
                _uiState.update {
                    it.copy(
                        isTestingGemini = false,
                        geminiStatus = if (isRateLimit) "RATE_LIMITED" else "ERROR",
                        geminiLatencyMs = latency,
                        geminiStatusMessage = if (isRateLimit)
                            "Rate limited — quota exceeded."
                        else "Error: $msg"
                    )
                }
            }
        }
    }

    // ── Groq ─────────────────────────────────────────────
    fun onGroqKeyChanged(key: String) {
        _uiState.update { it.copy(groqKeyInput = key, isGroqKeySaved = false) }
    }

    fun toggleGroqKeyVisibility() {
        _uiState.update { it.copy(groqKeyVisible = !it.groqKeyVisible) }
    }

    fun saveGroqKey() {
        viewModelScope.launch {
            val key = _uiState.value.groqKeyInput.trim()
            if (key.isBlank()) return@launch
            apiKeyStore.saveGroqApiKey(key)
            _uiState.update {
                it.copy(
                    isGroqKeySaved = true,
                    groqStatus = "UNKNOWN",
                    successMessage = "Groq key saved successfully"
                )
            }
        }
    }

    fun deleteGroqKey() {
        viewModelScope.launch {
            apiKeyStore.clearGroqApiKey()
            _uiState.update {
                it.copy(
                    groqKeyInput = "",
                    isGroqKeySaved = false,
                    groqStatus = "UNKNOWN",
                    groqStatusMessage = ""
                )
            }
        }
    }

    fun testGroqConnection() {
        viewModelScope.launch {
            val key = _uiState.value.groqKeyInput.trim()
            if (key.isBlank()) {
                _uiState.update {
                    it.copy(
                        groqStatus = "ERROR",
                        groqStatusMessage = "Enter a key first."
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isTestingGroq = true, groqStatusMessage = "") }
            val start = System.currentTimeMillis()
            try {
                withContext(Dispatchers.IO) {
                    groqClient.generateContent(key, "Hello", "Reply with just: OK")
                }
                val latency = System.currentTimeMillis() - start
                _uiState.update {
                    it.copy(
                        isTestingGroq = false,
                        groqStatus = "ONLINE",
                        groqLatencyMs = latency,
                        groqStatusMessage = "Connected (${latency}ms)"
                    )
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - start
                _uiState.update {
                    it.copy(
                        isTestingGroq = false,
                        groqStatus = "ERROR",
                        groqLatencyMs = latency,
                        groqStatusMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun testAiPrompt(provider: String, prompt: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLiveTesting = true, testResponse = "Thinking...") }
            try {
                val key = if (provider == "Gemini") _uiState.value.geminiKeyInput else _uiState.value.groqKeyInput
                val response = when (provider) {
                    "Gemini" -> {
                        val model = GenerativeModel(modelName = "gemini-2.0-flash-lite", apiKey = key.trim())
                        withContext(Dispatchers.IO) { model.generateContent(prompt).text ?: "Empty response" }
                    }
                    "Groq" -> {
                        withContext(Dispatchers.IO) { groqClient.generateContent(key.trim(), prompt) }
                    }
                    else -> "Invalid provider"
                }
                _uiState.update { it.copy(testResponse = response) }
            } catch (e: Exception) {
                _uiState.update { it.copy(testResponse = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLiveTesting = false) }
            }
        }
    }

    fun clearTest() {
        _uiState.update { it.copy(testResponse = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
