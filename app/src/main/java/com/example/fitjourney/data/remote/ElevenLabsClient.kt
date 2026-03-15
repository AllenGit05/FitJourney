package com.example.fitjourney.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ElevenLabsClient {

    // ── Voice IDs (free ElevenLabs voices) ──────────────
    // These are stable pre-made voices available on all accounts
    object Voices {
        const val AURORA  = "21m00Tcm4TlvDq8ikWAM" // Rachel — warm friendly female
        const val REX     = "VR6AewLTigWG4xSOukaG" // Arnold — deep powerful male
        const val ZEN     = "ErXwobaYiN019PkySvjV" // Antoni — calm smooth male
        const val ARJUN   = "TxGEqnHWrfWFTfGW9XjX" // Josh — use with Indian settings
        const val DEFAULT = "21m00Tcm4TlvDq8ikWAM" // Rachel as fallback
    }

    // ── Voice Settings per Coach ─────────────────────────
    data class VoiceSettings(
        val stability: Double,        // 0.0-1.0 (higher = more consistent)
        val similarityBoost: Double,  // 0.0-1.0 (higher = closer to original voice)
        val style: Double = 0.0,      // 0.0-1.0 (expressiveness)
        val useSpeakerBoost: Boolean = true
    )

    private fun getVoiceSettings(persona: String?): Pair<String, VoiceSettings> {
        return when (persona) {
            "Rex" -> Voices.REX to VoiceSettings(
                stability = 0.35,       // Less stable = more expressive/energetic
                similarityBoost = 0.85,
                style = 0.7             // High style for dramatic effect
            )
            "Zen" -> Voices.ZEN to VoiceSettings(
                stability = 0.90,       // Very stable = consistent calm voice
                similarityBoost = 0.75,
                style = 0.1             // Low style = monotone/meditative
            )
            "Arjun" -> Voices.ARJUN to VoiceSettings(
                stability = 0.55,
                similarityBoost = 0.80,
                style = 0.5             // Medium expressiveness
            )
            else -> Voices.AURORA to VoiceSettings(  // Aurora default
                stability = 0.60,
                similarityBoost = 0.80,
                style = 0.3
            )
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Returns audio as ByteArray (MP3)
    suspend fun synthesize(
        apiKey: String,
        text: String,
        persona: String?,
        speakingLanguage: String = "en"
    ): ByteArray = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) throw Exception("ElevenLabs key not set")

        val cleanText = text
            .replace(Regex("\\*\\*|__|\\*|_"), "")
            .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "")
            .replace(Regex("^[#*>-]\\s*", RegexOption.MULTILINE), "")
            .trim()
            .take(500) // Limit chars per request to save quota

        val (voiceId, settings) = getVoiceSettings(persona)

        val body = JSONObject().apply {
            put("text", cleanText)
            put("model_id", "eleven_multilingual_v2")
            // Pass language code so ElevenLabs pronounces correctly
            if (speakingLanguage != "en") {
                val langCode = when (speakingLanguage) {
                    "hi" -> "hi"   // Hindi
                    "ml" -> "ml"   // Malayalam
                    else -> "en"
                }
                put("language_code", langCode)
            }
            put("voice_settings", JSONObject().apply {
                put("stability", settings.stability)
                put("similarity_boost", settings.similarityBoost)
                put("style", settings.style)
                put("use_speaker_boost", settings.useSpeakerBoost)
            })
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("xi-api-key", apiKey)
            .addHeader("Accept", "audio/mpeg")
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: "Unknown error"
            throw Exception("ElevenLabs error (${response.code}): $errBody")
        }

        response.body?.bytes() ?: throw Exception("Empty audio response")
    }

    suspend fun testConnection(apiKey: String): Pair<Boolean, String> {
        return try {
            synthesize(apiKey, "Hello", "Aurora", "en")
            Pair(true, "Connected ✓")
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection failed")
        }
    }
}
