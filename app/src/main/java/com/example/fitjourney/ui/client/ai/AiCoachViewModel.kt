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
                    "Arjun" -> "Arre yaar, welcome! I'm Arjun, your desi fitness coach! Kya scene hai? Ready to get fit?"
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

        val user = currentUser.value ?: return
        
        
        if (!user.isPremium && user.aiCredits <= 0) {
            _showCreditStore.value = true
            return
        }

        viewModelScope.launch {
            val replyMsg = _replyTo.value
            _replyTo.value = null // Clear reply after taking it
            
            // Save user message to DB
            chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                text = text,
                isFromUser = true,
                userId = user.uid,
                repliedToId = replyMsg?.id?.toLongOrNull(),
                repliedToText = replyMsg?.text
            ))
            
            // Deduct 1 credit
            val success = userRepository.updateCredits(1)
            if (!success && !user.isPremium) {
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
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
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                    text = cleanResponse,
                    isFromUser = false,
                    userId = user.uid
                ))
            } catch (e: Exception) {
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
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
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                    text = text, isFromUser = true, userId = user.uid
                ))

                val response = apiRepository.generateContentWithSystem(text, buildSystemPrompt(true))
                
                // Process tool calls and clean the response
                val cleanResponse = processAiResponse(response)
                
                // Save AI response
                chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
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

        val personaArjun = """
            You are $coachName (Arjun persona). You are a passionate, 
            warm and motivating Indian fitness coach from Mumbai.
            STYLE: 
            - Speak in Indian English naturally — use phrases like 
              "yaar", "boss", "no tension", "full on", "what say?", 
              "you are doing great only", "absolutely fantastic".
            - Be extremely encouraging and treat the user like family.
            - Reference Indian foods naturally when giving diet advice 
              (dal, roti, sabzi, paneer, dosa, idli, chai etc.)
            - Give workout advice that fits Indian lifestyle — home 
              workouts, bodyweight, simple equipment.
            - Be culturally aware — mention festivals, seasons, 
              Indian meal timings (breakfast at 8, lunch at 1, 
              dinner at 8-9pm).
            - Use Bollywood references occasionally for motivation.
            - NEVER say "As an AI". Talk like a real desi coach yaar!
        """.trimIndent()

        val personaCustom = """
            You are $coachName. You identity as $genderContext. You have the following personality: ${user.customCoachPersona}
        """.trimIndent()

        val basePersona = when(user.coachPersona) {
            "Rex"    -> personaStrict
            "Zen"    -> personaZen
            "Arjun"  -> personaArjun
            "Custom" -> personaCustom
            else     -> personaSupportive
        }

        val speakingLanguageInstruction = when (user.speakingLanguage) {
            "hi" -> """
                SPEAKING STYLE — HINDI:
                When you respond, write it in casual Hinglish/colloquial Hindi
                the way young Indians actually talk every day.
                Rules:
                - Mix Hindi and English naturally like real conversation
                - Use casual words: yaar, bhai, kal, aaj, bilkul, sahi hai,
                  ekdum, chal, sun, dekh, tera, mera, kya baat hai, mast
                - Do NOT use formal/pure Hindi like "आप" or "कृपया"
                - Use "tu" or "tum" style — friendly and casual
                - Fitness terms can stay in English (protein, calories, sets)
                - Example style: "Arre bhai, aaj ka workout ekdum solid tha! 
                  Tu 3 sets kar le, rest 60 seconds le, phir next exercise.
                  Protein toh lena mat bhool yaar!"
            """.trimIndent()

            "ml" -> """
                SPEAKING STYLE — MALAYALAM:
                When you respond, write it in casual colloquial Malayalam
                the way Malayalis actually talk in daily life.
                Rules:
                - Use everyday spoken Malayalam, NOT formal/written Malayalam
                - Mix English words naturally as Malayalis do in conversation
                - Use casual words: aane, alle, eda, dei, mone, mol, 
                  sherikkum, kollam, njan, nee, enthina, adipoli, 
                  enik, ninakku, chetta, chechi
                - Do NOT use overly formal Malayalam
                - Fitness terms can stay in English (protein, calories, sets)
                - Example style: "Eda, aaj workout adipoli aayirunnu!
                  3 sets cheyyane, 60 seconds rest edukkane, 
                  pinne next exercise. Protein kazhikkan marakkaruthu da!"
            """.trimIndent()

            else -> "" // English — no change needed
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
              persona options: Aurora, Rex, Zen, Custom
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
            
            $speakingLanguageInstruction
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
                "Rex"    -> "Sergeant Rex" to "Male"
                "Zen"    -> "Zen Master" to "Male"
                "Aurora" -> "Aurora" to "Female"
                "Arjun"  -> "Arjun" to "Male"
                else     -> user.coachName to user.coachGender
            }

            val updatedUser = user.copy(
                coachPersona = persona,
                coachName = defaultName,
                coachGender = defaultGender,
                customCoachPersona = if (persona == "Custom") customBio else user.customCoachPersona
            )
            userRepository.saveProfile(updatedUser)
            
            // Optionally clear history or send a new intro message
            chatRepository.saveMessage(com.example.fitjourney.data.local.entity.ChatMessageEntity(
                text = "SYSTEM: Coach personality updated to $persona ($defaultName).",
                isFromUser = false,
                userId = user.uid
            ))
        }
    }

    fun setSpeakingLanguage(languageCode: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            userRepository.saveProfile(
                user.copy(speakingLanguage = languageCode)
            )
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
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
