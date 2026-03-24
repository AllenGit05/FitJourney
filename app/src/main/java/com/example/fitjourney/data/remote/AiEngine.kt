package com.example.fitjourney.data.remote

import com.example.fitjourney.data.local.ApiKeyStore
import com.example.fitjourney.util.RateLimitHandler
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.InvalidStateException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiResult(
    val content: String,
    val usedFallback: Boolean = false,
    val error: String? = null
)

class AiEngine(
    private val apiKeyStore: ApiKeyStore,
    private val groqApiClient: GroqApiClient
) {

    suspend fun generate(
        prompt: String,
        systemInstruction: String = "You are a helpful AI fitness coach."
    ): AiResult = withContext(Dispatchers.IO) {

        // Fetch keys fresh every call — no caching
        val geminiKey = apiKeyStore.getGeminiApiKey()
        val groqKey = apiKeyStore.getGroqApiKey()

        // STEP 1: Try Gemini if key is available
        if (geminiKey.isNotBlank()) {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = geminiKey
                )
                val response = model.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return@withContext AiResult(
                        content = text,
                        usedFallback = false
                    )
                }
            } catch (geminiEx: Exception) {
                val errorMsg = geminiEx.message ?: ""
                val isFallbackWorthy = RateLimitHandler.shouldFallbackToGroq(errorMsg) ||
                                      geminiEx is ServerException ||
                                      geminiEx is InvalidStateException

                if (!isFallbackWorthy) {
                    return@withContext AiResult(content = "", error = errorMsg)
                }

                if (groqKey.isBlank()) {
                    return@withContext AiResult(
                        content = "",
                        error = "Gemini rate limited and no Groq fallback key is configured. Please add a Groq key in Admin → API Management."
                    )
                }
                // Otherwise fall through to Groq fallback below
            }

        }

        // STEP 2: Try Groq (either Gemini failed/rate-limited OR Gemini key not set)
        if (groqKey.isNotBlank()) {
            return@withContext try {
                val response = groqApiClient.generateContent(
                    apiKey = groqKey,
                    prompt = prompt,
                    systemInstruction = systemInstruction
                )
                AiResult(content = response, usedFallback = true)
            } catch (groqEx: Exception) {
                AiResult(content = "", error = "Both APIs failed. Groq: ${groqEx.message}")
            }
        }

        // STEP 3: Both keys missing
        return@withContext AiResult(
            content = "",
            error = "No API keys configured. Please add a Gemini or Groq key in Admin → API Management."
        )
    }

    suspend fun generateOrThrow(
        prompt: String,
        systemInstruction: String = "You are a helpful AI fitness coach."
    ): String {
        val result = generate(prompt, systemInstruction)
        if (result.error != null) throw Exception(result.error)
        return result.content
    }
}
