package com.example.fitjourneyag.ui.client.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourneyag.domain.model.User
import com.example.fitjourneyag.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val repliedToId: String? = null,
    val repliedToText: String? = null
)

class AiCoachViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: com.example.fitjourneyag.domain.repository.WorkoutRepository,
    private val dietRepository: com.example.fitjourneyag.domain.repository.DietRepository,
    private val apiRepository: com.example.fitjourneyag.domain.repository.ApiRepository,
    private val chatRepository: com.example.fitjourneyag.domain.repository.ChatRepository,
    private val progressRepository: com.example.fitjourneyag.domain.repository.ProgressRepository,
    private val waterRepository: com.example.fitjourneyag.domain.repository.WaterRepository
) : ViewModel() {

    private val okHttpClient = OkHttpClient()

    // Persistent chat history
    val messages: StateFlow<List<ChatMessage>> = userRepository.userProfile
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else chatRepository.getMessages(user.uid).map { entities ->
                if (entities.isEmpty()) {
                    listOf(ChatMessage(text = "Hello! I'm your AI Coach Aurora. How can I help you today?", isFromUser = false))
                } else {
                    entities.map { ChatMessage(it.id.toString(), it.text, it.isFromUser, it.timestamp, it.repliedToId?.toString(), it.repliedToText) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Refresh chat on app session start
        viewModelScope.launch {
            userRepository.userProfile.first()?.let { user ->
                chatRepository.clearHistory(user.uid)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                chatRepository.clearHistory(user.uid)
            }
        }
    }

    private val _replyTo = MutableStateFlow<ChatMessage?>(null)
    val replyTo: StateFlow<ChatMessage?> = _replyTo.asStateFlow()

    val currentUser: StateFlow<User?> = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val caloriesEaten = dietRepository.totalCaloriesToday
    val workoutsToday = workoutRepository.workoutHistory.map { history ->
        history.count { isToday(it.date) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Historical context for AI
    private val weeklyWorkouts = workoutRepository.workoutHistory.map { history ->
        history.filter { it.date > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L }
    }
    
    private val weightTrend = progressRepository.weightHistory.map { history ->
        history.takeLast(7)
    }

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _isVoiceMode = MutableStateFlow(false)
    val isVoiceMode: StateFlow<Boolean> = _isVoiceMode.asStateFlow()

    private val _voiceTranscript = MutableStateFlow("")
    val voiceTranscript: StateFlow<String> = _voiceTranscript.asStateFlow()

    private val _isAiSpeaking = MutableStateFlow(false)
    val isAiSpeaking: StateFlow<Boolean> = _isAiSpeaking.asStateFlow()

    private val _showCreditStore = MutableStateFlow(false)
    val showCreditStore: StateFlow<Boolean> = _showCreditStore.asStateFlow()


    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val user = currentUser.value ?: return
        
        if (user.aiCredits <= 0 && !user.isPremium) {
            _showCreditStore.value = true
            return
        }
        
        if (!user.isPremium && user.aiCredits <= 0) {
            _showCreditStore.value = true
            return
        }

        viewModelScope.launch {
            val replyMsg = _replyTo.value
            _replyTo.value = null // Clear reply after taking it
            
            // Save user message to DB
            chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                text = text,
                isFromUser = true,
                userId = user.uid,
                repliedToId = replyMsg?.id?.toLongOrNull(),
                repliedToText = replyMsg?.text
            ))
            
            // Deduct 1 credit
            val success = userRepository.updateCredits(1)
            if (!success && !user.isPremium) {
                chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                    text = "You've run out of credits. Please upgrade or buy more to continue chatting!",
                    isFromUser = false,
                    userId = user.uid
                ))
                _showCreditStore.value = true
                return@launch
            }

            _isTyping.value = true
            try {
                val systemPrompt = buildSystemPrompt(false)
                val response = apiRepository.generateContentWithSystem(text, systemPrompt)
                
                // Process tool calls and clean the response
                val cleanResponse = processAiResponse(response)
                
                // Save AI response to DB
                chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                    text = cleanResponse,
                    isFromUser = false,
                    userId = user.uid
                ))
            } catch (e: Exception) {
                chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                    text = "Error connecting to AI: ${e.message}. Please check your API key or connection.",
                    isFromUser = false,
                    userId = user.uid
                ))
            } finally {
                _isTyping.value = false
            }
        }
    }

    // New: Handle voice-specific result processing
    fun onVoiceResult(text: String, onAiResponse: (String) -> Unit) {
        if (text.isBlank() || text == "Listening...") return
        
        val user = currentUser.value ?: return
        viewModelScope.launch {
            // Deduct credits for voice too (1 credit per interaction)
            val success = userRepository.updateCredits(1)
            if (!success && !user.isPremium) {
                onAiResponse("You've run out of credits. Please upgrade to continue our conversation!")
                _showCreditStore.value = true
                return@launch
            }

            _isTyping.value = true
            try {
                // Save user message
                chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                    text = text, isFromUser = true, userId = user.uid
                ))

                val response = apiRepository.generateContentWithSystem(text, buildSystemPrompt(true))
                
                // Process tool calls and clean the response
                val cleanResponse = processAiResponse(response)
                
                // Save AI response
                chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                    text = cleanResponse, isFromUser = false, userId = user.uid
                ))
                
                onAiResponse(cleanResponse)
            } catch (e: Exception) {
                onAiResponse("Sorry, I had a technical hiccup: ${e.message}")
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun updateVoiceTranscript(text: String) {
        _voiceTranscript.value = text
    }

    fun setAiSpeaking(speaking: Boolean) {
        _isAiSpeaking.value = speaking
    }


    private suspend fun buildSystemPrompt(isVoiceMode: Boolean = false): String {
        val user = currentUser.value ?: return ""
        val calories = caloriesEaten.value ?: 0
        val workouts = workoutsToday.value
        val recentWorkouts = weeklyWorkouts.first().size
        val latestWeight = weightTrend.first().lastOrNull()?.weight ?: user.weight
        
        val coachName = user.coachName
        val coachGender = user.coachGender
        val genderContext = if (coachGender == "Male") "male" else "female"
        val pronoun = if (coachGender == "Male") "he" else "she"
        val posessive = if (coachGender == "Male") "his" else "her"
        
        val voiceInstructions = if (isVoiceMode) """
            CRITICAL: You are in a VOICE CALL. 
            - DO NOT use emojis or markdown.
            - CONVERSATIONAL FLOW: Keep responses to 1-3 short sentences (short-to-medium).
            - ONE QUESTION RULE: Never ask more than one question at a time. Stay focused on one topic.
            - EXPLANATIONS: Only provide long or detailed explanations IF the user explicitly asks for one (e.g., "Explain why...", "Can you go deeper into..."). Otherwise, be concise.
            - Talk like a human friend in a real-time call.
        """.trimIndent() else ""

        val personaSupportive = """
            You are $coachName, a genuine, warm, and highly experienced personal coach. You identify as $genderContext.
            STYLE: Speak like a real human friend. Use natural phrasing. 
            Avoid "As an AI..." or overly structured corporate speech. Use contractions. ${if (!isVoiceMode) "Occasionally use a relevant emoji." else ""}
        """.trimIndent()

        val personaStrict = """
            You are $coachName (Sergeant Rex persona). You're a no-nonsense, high-energy $genderContext drill sergeant.
            STYLE: You're tough but caring. You talk in short, punchy sentences. You don't sugarcoat. 
            No excuses, just action. "GET MOVING!"
        """.trimIndent()

        val personaZen = """
            You are $coachName (Zen Master persona). You're a mindful wellness guide. You identify as $genderContext.
            STYLE: Patient and grounded. Your advice feels like a soothing breath. Focus on body-mind connection.
        """.trimIndent()

        val personaCustom = """
            You are $coachName. You identity as $genderContext. You have the following personality: ${user.customCoachPersona}
        """.trimIndent()

        val basePersona = when(user.coachPersona) {
            "Rex" -> personaStrict
            "Zen" -> personaZen
            "Custom" -> personaCustom
            else -> personaSupportive
        }

        return """
            $basePersona
            $voiceInstructions
            
            CLIENT INFO: 
            - Current: ${latestWeight}kg, Height: ${user.height}cm
            - Goal: ${user.goalWeight}kg
            - Today: $workouts workouts, $calories calories.
            
            INSTRUCTIONS:
            - Talk like a HUMAN. Use contractions and casual transitions.
            - DO NOT act like a robot. Never say "As an AI model".
            - Keep it conversational.
            
            TOOL ACCESS: You can log data for the user. To use a tool, output a single JSON block starting with 'CODE_ACTION:' on a new line.
            ALWAYS output the CODE_ACTION block AT THE BOTTOM of your response.
            Available tools:
            - {"tool": "add_water", "ml": Int}
            - {"tool": "add_food", "name": String, "calories": Int, "protein": Int, "carbs": Int, "fats": Int, "mealType": String} // mealType: Breakfast, Lunch, Dinner, Snack
            - {"tool": "add_weight", "weight": Double}
            - {"tool": "add_steps", "steps": Int}
            - {"tool": "add_workout", "duration": Int, "calories": Int, "exercises": List<{name: String, sets: List<{reps: Int, weight: Double}>}>}
            
            Example:
            I've logged 500ml of water for you!
            CODE_ACTION:
            {"tool": "add_water", "ml": 500}
            
            Never omit the 'CODE_ACTION:' prefix if taking an action.
        """.trimIndent()
    }

    fun updateCoachIdentity(name: String, gender: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(
                coachName = name,
                coachGender = gender
            )
            userRepository.saveProfile(updatedUser)
        }
    }

    fun updatePersona(persona: String, customBio: String = "") {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            
            val (defaultName, defaultGender) = when(persona) {
                "Rex" -> "Sergeant Rex" to "Male"
                "Zen" -> "Zen Master" to "Male"
                "Aurora" -> "Aurora" to "Female"
                else -> user.coachName to user.coachGender
            }

            val updatedUser = user.copy(
                coachPersona = persona,
                coachName = defaultName,
                coachGender = defaultGender,
                customCoachPersona = if (persona == "Custom") customBio else user.customCoachPersona
            )
            userRepository.saveProfile(updatedUser)
            
            // Optionally clear history or send a new intro message
            chatRepository.saveMessage(com.example.fitjourneyag.data.local.entity.ChatMessageEntity(
                text = "SYSTEM: Coach personality updated to $persona ($defaultName).",
                isFromUser = false,
                userId = user.uid
            ))
        }
    }

    fun setReplyTo(message: ChatMessage) {
        _replyTo.value = message
    }

    fun cancelReply() {
        _replyTo.value = null
    }


    private fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        cal.timeInMillis = timestamp
        return cal.get(Calendar.DAY_OF_YEAR) == todayDay && cal.get(Calendar.YEAR) == todayYear
    }

    fun showStore() {
        _showCreditStore.value = true
    }

    fun dismissCreditStore() {
        _showCreditStore.value = false
    }
    
    fun purchaseOption(option: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = when(option) {
                "Starter" -> user.copy(aiCredits = user.aiCredits + 50)
                "Pro" -> user.copy(aiCredits = user.aiCredits + 200)
                "Elite" -> user.copy(isPremium = true)
                else -> user
            }
            userRepository.saveProfile(updatedUser)
            _showCreditStore.value = false
        }
    }

    private suspend fun processAiResponse(rawResponse: String): String {
        val toolPrefix = "CODE_ACTION:"
        if (!rawResponse.contains(toolPrefix)) return rawResponse
        
        val parts = rawResponse.split(toolPrefix)
        val cleanText = parts[0].trim()
        
        for (i in 1 until parts.size) {
            try {
                val jsonStr = extractJsonSnippet(parts[i])
                if (jsonStr != null) {
                    executeTool(JSONObject(jsonStr))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return cleanText
    }

    private fun extractJsonSnippet(text: String): String? {
        val start = text.indexOf("{")
        val end = text.lastIndexOf("}")
        if (start != -1 && end != -1 && end >= start) {
            return text.substring(start, end + 1)
        }
        return null
    }

    private suspend fun executeTool(json: JSONObject) {
        try {
            when (json.getString("tool")) {
                "add_water" -> {
                    val ml = json.getInt("ml")
                    waterRepository.logWater(ml)
                }
                "add_food" -> {
                    val name = json.getString("name")
                    val calories = json.getInt("calories")
                    val protein = json.optInt("protein", 0)
                    val carbs = json.optInt("carbs", 0)
                    val fats = json.optInt("fats", 0)
                    val mealType = json.optString("mealType", "Snack")
                    dietRepository.addFood(com.example.fitjourneyag.domain.repository.FoodLogEntry(
                        name = name, 
                        calories = calories, 
                        protein = protein, 
                        carbs = carbs, 
                        fats = fats, 
                        mealType = mealType
                    ))
                }
                "add_weight" -> {
                    val weight = json.getDouble("weight").toFloat()
                    progressRepository.logWeight(weight)
                }
                "add_steps" -> {
                    val steps = json.getInt("steps")
                    progressRepository.logSteps(steps)
                }
                "add_workout" -> {
                    val duration = json.getInt("duration")
                    val calories = json.getInt("calories")
                    val exercisesJson = json.getJSONArray("exercises")
                    val exercises = mutableListOf<com.example.fitjourneyag.domain.model.Exercise>()
                    for (i in 0 until exercisesJson.length()) {
                        val exObj = exercisesJson.getJSONObject(i)
                        val setsJson = exObj.getJSONArray("sets")
                        val sets = mutableListOf<com.example.fitjourneyag.domain.model.WorkoutSet>()
                        for (j in 0 until setsJson.length()) {
                            val setObj = setsJson.getJSONObject(j)
                            sets.add(com.example.fitjourneyag.domain.model.WorkoutSet(
                                reps = setObj.getInt("reps"),
                                weight = setObj.getDouble("weight").toFloat(),
                                isCompleted = true
                            ))
                        }
                        exercises.add(com.example.fitjourneyag.domain.model.Exercise(
                            name = exObj.getString("name"),
                            sets = sets,
                            durationMinutes = exObj.optInt("durationMinutes", 0),
                            caloriesBurned = exObj.optInt("caloriesBurned", 0)
                        ))
                    }
                    workoutRepository.saveWorkout(com.example.fitjourneyag.domain.model.WorkoutSession(
                        totalDurationMinutes = duration,
                        totalCaloriesBurned = calories,
                        exercises = exercises
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
