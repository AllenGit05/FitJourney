package com.example.fitjourneyag.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GroqApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val mediaType = "application/json".toMediaType()

    data class GroqMessage(
        val role: String,
        val content: String
    )

    data class GroqRequest(
        val model: String,
        val messages: List<GroqMessage>,
        @SerializedName("max_tokens") val maxTokens: Int = 1024,
        val temperature: Double = 0.8
    )

    data class GroqResponse(
        val choices: List<Choice>?
    ) {
        data class Choice(val message: GroqMessage?)
    }

    // apiKey is passed in at call time — fetched fresh from ApiKeyStore each call
    suspend fun generateContent(
        apiKey: String,
        prompt: String,
        systemInstruction: String = "You are a helpful AI fitness coach."
    ): String = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {
            throw Exception("Groq API key not configured. Please add it in Admin → API Management.")
        }

        val messages = listOf(
            GroqMessage(role = "system", content = systemInstruction),
            GroqMessage(role = "user", content = prompt)
        )

        val requestBody = gson.toJson(
            GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = messages
            )
        ).toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
            ?: throw Exception("Empty response from Groq")

        if (!response.isSuccessful) {
            throw Exception("Groq API error (${response.code}): $body")
        }

        val parsed = gson.fromJson(body, GroqResponse::class.java)
        parsed.choices?.firstOrNull()?.message?.content
            ?: throw Exception("No content in Groq response")
    }

    // Test using a provided key (from Admin screen)
    suspend fun testConnection(apiKey: String): Pair<Boolean, String> {
        return try {
            val result = generateContent(apiKey, "Hello", "Reply with just: OK")
            Pair(true, "Connected ✓")
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection failed")
        }
    }
}
