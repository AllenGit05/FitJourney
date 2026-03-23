package com.example.fitjourney.ui.client.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.speech.tts.UtteranceProgressListener
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioDeviceInfo
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoiceManager(
    private val context: Context
) : RecognitionListener {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var isSpeakerphoneRequested = false
    private var currentPersona: String? = null
    private val voiceScope = CoroutineScope(Dispatchers.IO)

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript = _transcript.asStateFlow()

    private var onRecognitionFinished: ((String) -> Unit)? = null

    init {
        speechRecognizer.setRecognitionListener(this)
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                
                // Configure TTS to use voice call stream for proper routing (Earpiece vs Speaker)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
                
                isTtsReady = true
            }
        }
        // Set initial mode for voice communication
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    fun startListening(onResult: (String) -> Unit) {
        stopSpeaking() // Ensure AI stops when user starts talking
        onRecognitionFinished = onResult
        _transcript.value = "Listening..."
        val recognitionLocale = Locale.getDefault().toLanguageTag()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, recognitionLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, recognitionLocale)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
        _isListening.value = true
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        _isListening.value = false
    }

    fun stopSpeaking() {
        if (isTtsReady) tts?.stop()
    }

    fun speak(text: String, pitch: Float = 1.0f, rate: Float = 1.0f, onFinished: () -> Unit = {}) {
        if (isTtsReady) {
            // Re-apply routing right before speaking to guard against system resets
            applyAudioRouting(isSpeakerphoneRequested)
            
            val cleanedText = cleanTextForSpeech(text)
            tts?.setPitch(pitch)
            tts?.setSpeechRate(rate)
            tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "coach_speech")
            onFinished() 
        }
    }

    private fun cleanTextForSpeech(text: String): String {
        return text
            // Remove Markdown bold/italic
            .replace(Regex("\\*\\*|__|\\*|_"), "")
            // Remove Emojis (Common range)
            .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "")
            // Remove Bullet points/Hashes
            .replace(Regex("^[#*>-]\\s*", RegexOption.MULTILINE), "")
            // Remove links
            .replace(Regex("\\[.*?\\]\\(.*?\\)"), "")
            .trim()
    }

    fun speakWithPersona(
        text: String,
        persona: String?,
        gender: String?,
        englishAccent: String = "en-in",
        onFinished: () -> Unit = {}
    ) {
        if (!isTtsReady) return

        val locale = when (englishAccent) {
            "en-in" -> java.util.Locale("en", "IN")
            "en-gb" -> java.util.Locale("en", "GB")
            "en-au" -> java.util.Locale("en", "AU")
            else    -> java.util.Locale("en", "US")
        }
        val localeResult = tts?.isLanguageAvailable(locale)
        if (localeResult == android.speech.tts.TextToSpeech.LANG_AVAILABLE ||
            localeResult == android.speech.tts.TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            localeResult == android.speech.tts.TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            tts?.language = locale
        } else {
            tts?.language = java.util.Locale.ENGLISH
        }

        // ── Voice Profile per Coach ──────────────────────────
        // pitch: < 1.0 = deeper, > 1.0 = higher
        // rate:  < 1.0 = slower, > 1.0 = faster
        data class VoiceProfile(
            val pitch: Float,
            val rate: Float,
            val preferredGender: String // "male" or "female"
        )

        val profile = when (persona) {
            "Rex" -> VoiceProfile(
                pitch = 0.75f,     // Deep, commanding voice
                rate  = 1.2f,      // Fast, energetic
                preferredGender = "male"
            )
            "Zen" -> VoiceProfile(
                pitch = 0.95f,     // Calm, neutral
                rate  = 0.75f,     // Slow, meditative
                preferredGender = "male"
            )
            else -> VoiceProfile(   // Aurora — default
                pitch = 1.2f,      // Bright, friendly female
                rate  = 1.0f,      // Normal pace
                preferredGender = "female"
            )
        }

        // ── Find the best matching installed voice ───────────
        val allVoices = tts?.voices?.filter { 
            !it.isNetworkConnectionRequired 
        } ?: emptyList()

        // First try exact locale match with gender
        var targetVoice = allVoices.firstOrNull { voice ->
            voice.locale.language == locale.language &&
            voice.locale.country == locale.country &&
            voice.name.lowercase().contains(profile.preferredGender)
        }

        // Fallback 1: Exact locale, any gender
        if (targetVoice == null) {
            targetVoice = allVoices.firstOrNull { voice ->
                voice.locale.language == locale.language &&
                voice.locale.country == locale.country
            }
        }

        // Fallback 2: Same language, any country, preferred gender
        if (targetVoice == null) {
            targetVoice = allVoices.firstOrNull { voice ->
                voice.locale.language == locale.language &&
                voice.name.lowercase().contains(profile.preferredGender)
            }
        }

        // Fallback 3: Any English voice with preferred gender
        if (targetVoice == null) {
            targetVoice = allVoices.firstOrNull { voice ->
                voice.locale.language == "en" &&
                voice.name.lowercase().contains(profile.preferredGender)
            }
        }

        // Fallback 4: Any available voice
        if (targetVoice == null) {
            targetVoice = allVoices.firstOrNull()
        }

        // Apply locale and voice
        targetVoice?.let { tts?.voice = it }

        speak(text, profile.pitch, profile.rate, onFinished)
    }

    fun setRecognitionLocale(persona: String?) {
        currentPersona = persona
    }

    fun setSpeakerphone(isOn: Boolean) {
        isSpeakerphoneRequested = isOn
        applyAudioRouting(isOn)
    }

    private fun applyAudioRouting(isOn: Boolean) {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isOn) {
                    val speakerDevice = audioManager.availableCommunicationDevices
                        .find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                    speakerDevice?.let { audioManager.setCommunicationDevice(it) }
                } else {
                    audioManager.clearCommunicationDevice()
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = isOn
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        speechRecognizer.destroy()
        tts?.shutdown()
        // Reset to normal mode and clear routing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.clearCommunicationDevice()
        } else {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = false
        }
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    // RecognitionListener overrides
    override fun onReadyForSpeech(params: Bundle?) { _transcript.value = "Go ahead, I'm listening..." }
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { _isListening.value = false }
    override fun onError(error: Int) { 
        _isListening.value = false 
        _transcript.value = "Didn't catch that. Try again?"
    }
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        _transcript.value = text
        onRecognitionFinished?.invoke(text)
    }
    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        _transcript.value = matches?.firstOrNull() ?: ""
    }
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
