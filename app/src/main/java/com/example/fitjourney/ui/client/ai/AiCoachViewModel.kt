package com.example.fitjourney.ui.client.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.User
import com.example.fitjourney.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val workoutRepository: com.example.fitjourney.domain.repository.WorkoutRepository,
    private val dietRepository: com.example.fitjourney.domain.repository.DietRepository,
    private val apiRepository: com.example.fitjourney.domain.repository.ApiRepository,
    private val chatRepository: com.example.fitjourney.domain.repository.ChatRepository,
    private val progressRepository: com.example.fitjourney.domain.repository.ProgressRepository,
    private val waterRepository: com.example.fitjourney.domain.repository.WaterRepository,
    private val habitRepository: com.example.fitjourney.domain.repository.HabitRepository
) : ViewModel() {

    // Persistent chat history
    val messages: StateFlow<List<ChatMessage>> = userRepository.userProfile
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else chatRepository.getMessages(user.uid).map { entities ->
                if (entities.isEmpty()) {
                    val welcomeMsg = when(user?.coachPersona) {
                    "Rex"   -> "Listen up! I'm Sergeant Rex. No excuses, just results. What are we working on today?"
                    "Zen"   -> "Welcome. I'm Zen Master. Take a deep breath... and let's begin your journey."
                    else    -> "Hello! I'm Aurora, your AI fitness coach. How can I help you today?"
                }
                listOf(ChatMessage(text = welcomeMsg, isFromUser = false))
                } else {
                    entities.map { ChatMessage(it.id.toString(), it.text, it.isFromUser, it.timestamp, it.repliedToId?.toString(), it.repliedToText) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


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
    
    val waterDrank = waterRepository.totalWaterToday
    val stepsToday = progressRepository.stepsHistory.map { history ->
        history.filter { isToday(it.date) }.sumOf { it.count }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Historical context for AI
    private val weeklyWorkouts = workoutRepository.workoutHistory.map { history ->
        history.filter { it.date > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L }
    }
    
    private val weightTrend = progressRepository.weightHistory.map { history ->
        history.takeLast(7)
    }

    private val currentHabits: StateFlow<List<com.example.fitjourney.domain.model.Habit>> =
        habitRepository.habits
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

        val user = userRepository.userProfile.value ?: return
        if (!user.isPremium && user.aiCredits <= 0) {
            _showCreditStore.value = true
            return
        }


        viewModelScope.launch {
            val intent = detectAndExecuteIntent(text)

            val intentContext = when {
                intent?.startsWith("LOGGED_WATER:") == true -> {
                    val ml = intent.removePrefix("LOGGED_WATER:")
                        .toIntOrNull() ?: 0
                    "\n[SYSTEM NOTE — do not mention this note: " +
                    "${ml}ml of water has been logged for the user. " +
                    "Acknowledge it warmly in one sentence then continue.]"
                }
                intent?.startsWith("LOGGED_STEPS:") == true -> {
                    val s = intent.removePrefix("LOGGED_STEPS:")
                    "\n[SYSTEM NOTE — do not mention this note: " +
                    "${s} steps have been logged. Acknowledge briefly.]"
                }
                intent?.startsWith("LOGGED_WEIGHT:") == true -> {
                    val kg = intent.removePrefix("LOGGED_WEIGHT:")
                    "\n[SYSTEM NOTE — do not mention this note: " +
                    "Weight of ${kg}kg has been logged. Acknowledge briefly.]"
                }
                else -> ""
            }

            val enrichedMessage = text + intentContext

            // Save user message to DB
            val userMsg = com.example.fitjourney.data.local.entity.ChatMessageEntity(
                text = text,
                isFromUser = true,
                userId = user.uid,
                repliedToId = _replyTo.value?.id?.toLongOrNull(),
                repliedToText = _replyTo.value?.text
            )
            chatRepository.saveMessage(userMsg)
            _replyTo.value = null // Cancel reply

            _isTyping.value = true

            try {
                val sysPrompt = buildSystemPrompt(user.isPremium)
                val response = apiRepository.generateContentWithSystem(enrichedMessage, sysPrompt)
                
                // ONLY deduct credits after success
                if (!user.isPremium) {
                    userRepository.updateCredits(1)
                }

                _isTyping.value = false
                val cleanResponse = processAiResponse(response)


                // Save AI response to DB
                val aiMsg = com.example.fitjourney.data.local.entity.ChatMessageEntity(
                    text = cleanResponse,
                    isFromUser = false,
                    userId = user.uid
                )
                chatRepository.saveMessage(aiMsg)

                // Prune history
                val count = chatRepository.getMessageCount(user.uid)
                if (count > 50) {
                    chatRepository.pruneHistory(user.uid, count - 50)
                }
            } catch (e: Exception) {
                _isTyping.value = false
                val errorMsg = com.example.fitjourney.data.local.entity.ChatMessageEntity(
                    text = "Sorry, I ran into an issue. Please try again.",
                    isFromUser = false,
                    userId = user.uid
                )
                chatRepository.saveMessage(errorMsg)
            }

        }
    }

    // New: Handle voice-specific result processing
    fun onVoiceResult(text: String, onAiResponse: (String) -> Unit) {
        if (text.isBlank() || text == "Listening...") return
        
        val user = currentUser.value ?: return
        
        // Initial credit check before starting
        if (!user.isPremium && user.aiCredits <= 0) {
            onAiResponse("You've run out of credits. Please upgrade to continue our conversation!")
            _showCreditStore.value = true
            return
        }

        viewModelScope.launch {
            _isTyping.value = true
            try {
                // Save user message
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                    text = text, isFromUser = true, userId = user.uid
                ))
                pruneHistoryIfNeeded(user.uid)

                val response = apiRepository.generateContentWithSystem(text, buildSystemPrompt(true))
                
                // Process tool calls and clean the response
                val cleanResponse = processAiResponse(response)
                
                // Only deduct credit AFTER a successful API response and processing
                if (!user.isPremium) {
                    userRepository.updateCredits(1)
                }

                // Save AI response
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                    text = cleanResponse, isFromUser = false, userId = user.uid
                ))
                pruneHistoryIfNeeded(user.uid)
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


        val basePersona = when(user.coachPersona) {
            "Rex"    -> personaStrict
            "Zen"    -> personaZen
            else     -> personaSupportive
        }


        val accentInstruction = when (user.englishAccent) {
            "en-in" -> """

                COMMUNICATION STYLE:
                Write in warm Indian English. This user is from India.
                Use Indian expressions naturally — words like "yaar",
                "achha", "bilkul", "bas" when they feel natural and warm.
                Reference Indian foods for nutrition examples: dal, roti,
                sabzi, rice, idli, dosa, paneer. Use metric units (kg, km).
                Keep tone like a trusted older sibling or friend.
                Celebrate effort with phrases like "bahut achha kiya!"
            """.trimIndent()
            "en-gb" -> """

                COMMUNICATION STYLE:
                Write in British English. Use British spelling and 
                expressions. Say "brilliant" not "awesome", "spot on"
                not "exactly right", "well done" frequently.
                Use "whilst" not "while". Use metric units.
                Keep tone professional, warm and encouraging.
            """.trimIndent()
            "en-au" -> """

                COMMUNICATION STYLE:
                Write in Australian English. Use Aussie expressions
                naturally — "arvo", "reckon", "heaps", "no worries",
                "ripper". Keep tone very relaxed and upbeat.
                Celebrate wins loudly. Use metric units.
            """.trimIndent()
            else -> """

                COMMUNICATION STYLE:
                Write in standard American English. Energetic,
                motivating, direct. Use metric units unless user
                profile specifies otherwise.
            """.trimIndent()
        }

        val systemPrompt = """
            $basePersona
            $accentInstruction
            
            USER PROFILE:
            - Name: ${user.username}
            - Current: ${latestWeight}kg, Height: ${user.height}cm
            - Goal: ${user.goalWeight}kg
            - Today: $workouts workouts, $calories calories.
            
            INSTRUCTIONS:
            - Talk like a HUMAN. Use contractions and casual transitions.
            - DO NOT act like a robot. Never say "As an AI model".
            - Keep it conversational.
            
            TOOL ACCESS: You can perform ANY action in the app for the user.
            To use a tool output a JSON block starting with 'CODE_ACTION:' on a new line.
            ALWAYS output CODE_ACTION AT THE BOTTOM of your response.
            ONLY use a tool when the user clearly asks you to do something.
            NEVER use multiple tools in one response unless explicitly asked.

            Available tools:

            // ── NUTRITION ──
            - {"tool": "add_food", "name": String, "calories": Int, "protein": Int, "carbs": Int, "fats": Int, "mealType": String}
              mealType options: Breakfast, Lunch, Dinner, Snack
            - {"tool": "delete_food", "name": String}
              Use when user says remove, delete or undo a food log
            - {"tool": "add_water", "ml": Int}
              Convert cups/glasses to ml (1 glass = 250ml, 1 cup = 240ml)
            - {"tool": "set_calorie_goal", "calories": Int}
              Use when user wants to change their daily calorie target

            // ── FITNESS ──
            - {"tool": "add_workout", "duration": Int, "calories": Int, "exercises": [{"name": String, "sets": [{"reps": Int, "weight": Double}]}]}
            - {"tool": "add_steps", "steps": Int}
            - {"tool": "set_step_goal", "steps": Int}
            - {"tool": "set_water_goal", "ml": Int}

            // ── PROGRESS ──
            - {"tool": "add_weight", "weight": Double}
            - {"tool": "add_measurements", "waist": Double, "chest": Double, "arms": Double, "hips": Double, "legs": Double}
              Only include fields the user mentioned, use 0.0 for unmentioned fields

            // ── HABITS ──
            - {"tool": "add_habit", "name": String, "icon": String}
              Pick a relevant emoji for icon based on the habit name
            - {"tool": "toggle_habit", "name": String}
              Use when user says they completed a habit or want to check it off
            - {"tool": "delete_habit", "name": String}

            // ── PROFILE ──
            - {"tool": "update_profile", "field": String, "value": String}
              field options: weight, height, goalWeight, activityLevel, fitnessGoal, 
              foodType, gender
              fitnessGoal options: Fat Loss, Recomp, Muscle Gain, Maintain
              activityLevel options: Sedentary, Low, Moderate, High, Very High

            // ── COACH SETTINGS ──  
            - {"tool": "change_persona", "persona": String}
              persona options: Aurora, Rex, Zen

            - {"tool": "change_coach_name", "name": String, "gender": String}

            // ── REPORTS ──
            - {"tool": "generate_weekly_report"}
              Use when user asks for weekly summary or analysis

            Examples of natural language → tool mapping:
            "Log 3 glasses of water" → add_water ml=750
            "I just did 20 pushups and 15 squats" → add_workout with those exercises
            "Remove the pizza I logged" → delete_food name="pizza"
            "I completed my meditation habit" → toggle_habit name="meditation"  
            "Change my goal to muscle gain" → update_profile field="fitnessGoal" value="Muscle Gain"
            "I weigh 74.5kg today" → add_weight weight=74.5
            "My chest is 95cm and waist is 80cm" → add_measurements
            "Switch to Coach Rex" → change_persona persona="Rex"
            "Create a morning run habit" → add_habit name="Morning Run" icon="🏃"
            "Set my step goal to 12000" → set_step_goal steps=12000
            "Give me a weekly summary" → generate_weekly_report

            Never omit the 'CODE_ACTION:' prefix if taking an action.
            
            IMPORTANT BEHAVIOUR:
            When the user mentions drinking water, their steps, or their
            weight the app automatically logs it. Just acknowledge it in
            one warm sentence then continue naturally.
            Never tell the user to go to another screen to log something.
            You are their one stop — everything happens through you.
            Keep all responses to 3-5 sentences unless detail is asked for.
            
            $voiceInstructions
        """.trimIndent()
        
        return systemPrompt
        
        return "$systemPrompt$accentInstruction"
    }

    private suspend fun detectAndExecuteIntent(
        message: String
    ): String? {
        val lower = message.lowercase().trim()

        // Water logging
        val waterMl = when {
            Regex("""(\d+)\s*ml""").containsMatchIn(lower) ->
                Regex("""(\d+)\s*ml""").find(lower)
                    ?.groupValues?.get(1)?.toIntOrNull()
            Regex("""(\d+(?:\.\d+)?)\s*(?:l|liter|litre)\b""")
                .containsMatchIn(lower) ->
                Regex("""(\d+(?:\.\d+)?)\s*(?:l|liter|litre)\b""")
                    .find(lower)?.groupValues?.get(1)
                    ?.toFloatOrNull()?.times(1000)?.toInt()
            Regex("""(\d+)\s*glass""").containsMatchIn(lower) ->
                Regex("""(\d+)\s*glass""").find(lower)
                    ?.groupValues?.get(1)?.toIntOrNull()?.times(250)
            Regex("""(\d+)\s*cup""").containsMatchIn(lower) ->
                Regex("""(\d+)\s*cup""").find(lower)
                    ?.groupValues?.get(1)?.toIntOrNull()?.times(240)
            lower.contains("water") && lower.length < 80 -> 250
            lower.contains("drank") && lower.length < 80 -> 250
            else -> null
        }
        if (waterMl != null && waterMl > 0) {
            waterRepository.logWater(waterMl)
            return "LOGGED_WATER:$waterMl"
        }

        // Steps logging
        val steps = when {
            Regex("""(\d[\d,]*)\s*steps?""").containsMatchIn(lower) ->
                Regex("""(\d[\d,]*)\s*steps?""").find(lower)
                    ?.groupValues?.get(1)?.replace(",", "")
                    ?.toIntOrNull()
            Regex("""(\d+(?:\.\d+)?)\s*km\b""").containsMatchIn(lower)
                && lower.contains("walk") ->
                Regex("""(\d+(?:\.\d+)?)\s*km\b""").find(lower)
                    ?.groupValues?.get(1)?.toFloatOrNull()
                    ?.times(1312)?.toInt()
            else -> null
        }
        if (steps != null && steps > 0) {
            progressRepository.logSteps(steps)
            return "LOGGED_STEPS:$steps"
        }

        // Weight logging
        val weightKg = Regex(
            """(?:weigh|weight|i(?:'m| am)\s+)?(\d+(?:\.\d+)?)\s*kg"""
        ).find(lower)?.groupValues?.get(1)?.toFloatOrNull()
        if (weightKg != null && weightKg in 20f..300f) {
            progressRepository.logWeight(weightKg)
            return "LOGGED_WEIGHT:$weightKg"
        }

        return null
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
                "Rex"    -> "Sergeant Rex" to "Male"
                "Zen"    -> "Zen Master" to "Male"
                "Aurora" -> "Aurora" to "Female"
                else     -> user.coachName to user.coachGender
            }

            val updatedUser = user.copy(
                coachPersona = persona,
                coachName = defaultName,
                coachGender = defaultGender
            )

            userRepository.saveProfile(updatedUser)
            
            // Optionally clear history or send a new intro message
            chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                text = "SYSTEM: Coach personality updated to $persona ($defaultName).",
                isFromUser = false,
                userId = user.uid
            ))
            pruneHistoryIfNeeded(user.uid)
        }
    }

    fun setEnglishAccent(accent: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(englishAccent = accent)
            userRepository.saveProfile(updatedUser)
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
                    dietRepository.addFood(
                        com.example.fitjourney.domain.repository.FoodLogEntry(
                            name = name,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fats = fats,
                            mealType = mealType
                        )
                    )
                }
                "delete_food" -> {
                    val name = json.getString("name")
                    val logs = dietRepository.foodLogs.value
                    val entry = logs.firstOrNull {
                        it.name.contains(name, ignoreCase = true)
                    }
                    if (entry != null) dietRepository.removeFood(entry)
                }
                "add_weight" -> {
                    val weight = json.getDouble("weight").toFloat()
                    progressRepository.logWeight(weight)
                }
                "add_steps" -> {
                    val steps = json.getInt("steps")
                    progressRepository.logSteps(steps)
                }
                "set_step_goal" -> {
                    val steps = json.getInt("steps")
                    val user = currentUser.value ?: return
                    userRepository.saveProfile(user.copy(stepGoal = steps))
                }
                "set_water_goal" -> {
                    val ml = json.getInt("ml")
                    val user = currentUser.value ?: return
                    userRepository.saveProfile(user.copy(waterGoal = ml))
                }
                "set_calorie_goal" -> {
                    val calories = json.getInt("calories")
                    val user = currentUser.value ?: return
                    userRepository.saveProfile(user.copy(calorieGoal = calories))
                }
                "add_measurements" -> {
                    val waist = json.optDouble("waist", 0.0).toFloat()
                    val chest = json.optDouble("chest", 0.0).toFloat()
                    val arms  = json.optDouble("arms",  0.0).toFloat()
                    val hips  = json.optDouble("hips",  0.0).toFloat()
                    val legs  = json.optDouble("legs",  0.0).toFloat()
                    progressRepository.logMeasurements(waist, chest, arms, hips, legs)
                }
                "add_habit" -> {
                    val habitName = json.getString("name")
                    val icon = json.optString("icon", "⭐")
                    habitRepository.addHabit(habitName, icon)
                }
                "toggle_habit" -> {
                    val habitName = json.getString("name")
                    val habits = currentHabits.value
                    val habit = habits.firstOrNull {
                        it.name.contains(habitName, ignoreCase = true)
                    }
                    if (habit != null) habitRepository.toggleHabit(habit.id)
                }
                "delete_habit" -> {
                    val habitName = json.getString("name")
                    val habits = currentHabits.value
                    val habit = habits.firstOrNull {
                        it.name.contains(habitName, ignoreCase = true)
                    }
                    if (habit != null) habitRepository.deleteHabit(habit.id)
                }
                "update_profile" -> {
                    val field = json.getString("field")
                    val value = json.getString("value")
                    val user = currentUser.value ?: return
                    val updatedUser = when (field) {
                        "weight"        -> user.copy(weight = value)
                        "height"        -> user.copy(height = value)
                        "goalWeight"    -> user.copy(goalWeight = value)
                        "activityLevel" -> user.copy(activityLevel = value)
                        "fitnessGoal"   -> user.copy(fitnessGoal = value)
                        "foodType"      -> user.copy(foodType = value)
                        "gender"        -> user.copy(gender = value)
                        else            -> user
                    }
                    userRepository.saveProfile(updatedUser)
                }
                "change_persona" -> {
                    val persona = json.getString("persona")
                    updatePersona(persona)
                }
                "change_coach_name" -> {
                    val name = json.getString("name")
                    val gender = json.optString("gender", "Female")
                    updateCoachIdentity(name, gender)
                }
                "add_workout" -> {
                    val duration = json.getInt("duration")
                    val calories = json.getInt("calories")
                    val exercisesJson = json.getJSONArray("exercises")
                    val exercises = mutableListOf<com.example.fitjourney.domain.model.Exercise>()
                    for (i in 0 until exercisesJson.length()) {
                        val exObj = exercisesJson.getJSONObject(i)
                        val setsJson = exObj.getJSONArray("sets")
                        val sets = mutableListOf<com.example.fitjourney.domain.model.WorkoutSet>()
                        for (j in 0 until setsJson.length()) {
                            val setObj = setsJson.getJSONObject(j)
                            sets.add(
                                com.example.fitjourney.domain.model.WorkoutSet(
                                    reps = setObj.getInt("reps"),
                                    weight = setObj.getDouble("weight").toFloat(),
                                    isCompleted = true
                                )
                            )
                        }
                        exercises.add(
                            com.example.fitjourney.domain.model.Exercise(
                                name = exObj.getString("name"),
                                sets = sets,
                                durationMinutes = exObj.optInt("durationMinutes", 0),
                                caloriesBurned = exObj.optInt("caloriesBurned", 0)
                            )
                        )
                    }
                    workoutRepository.saveWorkout(
                        com.example.fitjourney.domain.model.WorkoutSession(
                            totalDurationMinutes = duration,
                            totalCaloriesBurned = calories,
                            exercises = exercises
                        )
                    )
                }
                "generate_weekly_report" -> {
                    // Trigger a summary message from current data
                    val user = currentUser.value ?: return
                    val workouts = weeklyWorkouts.first()
                    val weights = weightTrend.first()
                    val calories = caloriesEaten.value ?: 0
                    val water = waterRepository.totalWaterToday.value
                    
                    val summaryPrompt = """
                        Generate a brief weekly fitness summary for ${user.username}.
                        Data this week:
                        - Workouts completed: ${workouts.size}
                        - Current calories today: $calories / ${user.calorieGoal}
                        - Water today: ${water}ml / ${user.waterGoal}ml
                        - Weight trend: ${weights.map { it.weight }.joinToString(" → ")}
                        
                        Give a 3-4 sentence motivating summary with key insights.
                        Keep it personal and encouraging.
                    """.trimIndent()
                    
                    val summary = apiRepository.generateContent(summaryPrompt)
                    chatRepository.saveMessage(
                        com.example.fitjourney.data.local.entity.ChatMessageEntity(
                            text = summary,
                            isFromUser = false,
                            userId = user.uid
                        )
                    )
                    pruneHistoryIfNeeded(user.uid)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private suspend fun pruneHistoryIfNeeded(userId: String) {
        try {
            val count = chatRepository.getMessageCount(userId)
            if (count > 50) {
                chatRepository.pruneHistory(userId, count - 50)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
