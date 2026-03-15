package com.example.fitjourneyag.ui.client.ai

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

class VoiceManager(private val context: Context) : RecognitionListener {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var isSpeakerphoneRequested = false

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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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
        if (isTtsReady) {
            tts?.stop()
        }
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

    fun speakWithPersona(text: String, persona: String?, gender: String?, onFinished: () -> Unit = {}) {
        val isMale = gender == "Male"
        
        val (pitch, rate) = when (persona) {
            "Rex" -> (if (isMale) 0.85f else 0.9f) to 1.15f   // Energetic, punchy
            "Zen" -> (if (isMale) 0.95f else 1.0f) to 0.80f   // Calm, slow
            "Custom" -> (if (isMale) 0.90f else 1.1f) to 1.00f // Neutral
            else -> 1.15f to 1.05f                            // Aurora: Friendly
        }

        // Attempt to find a gender-matching voice with diversity
        if (isTtsReady) {
            val voices = tts?.voices?.filter { 
                it.locale.language == Locale.getDefault().language && !it.isNetworkConnectionRequired
            }?.sortedBy { it.name } ?: emptyList()

            val targetVoice = when (persona) {
                "Rex" -> voices.filter { it.name.lowercase().contains("male") }.getOrNull(1) // Try second male
                    ?: voices.find { it.name.lowercase().contains("male") }
                "Zen" -> voices.filter { it.name.lowercase().contains("male") }.getOrNull(0) // Try first male
                "Aurora" -> voices.filter { it.name.lowercase().contains("female") }.getOrNull(0) // Try first female
                else -> {
                    if (isMale) voices.find { it.name.lowercase().contains("male") }
                    else voices.find { it.name.lowercase().contains("female") }
                }
            } ?: voices.firstOrNull()
            
            targetVoice?.let { tts?.voice = it }
        }

        speak(text, pitch, rate, onFinished)
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
