package com.example.fitjourney.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.*
import com.example.fitjourney.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

class ClientDashboardViewModel(
    private val workoutRepository: WorkoutRepository,
    private val dietRepository: com.example.fitjourney.domain.repository.DietRepository,
    private val authRepository: com.example.fitjourney.domain.repository.AuthRepository,
    private val userRepository: com.example.fitjourney.domain.repository.UserRepository,
    private val waterRepository: com.example.fitjourney.domain.repository.WaterRepository,
    private val progressRepository: com.example.fitjourney.domain.repository.ProgressRepository,
    private val apiRepository: com.example.fitjourney.domain.repository.ApiRepository
) : ViewModel() {
    
    private val _greetingGenerated = AtomicBoolean(false)
    
    val currentUser = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val caloriesEaten: StateFlow<Int> = dietRepository.totalCaloriesToday

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned.asStateFlow()

    val dailyCalorieGoal: StateFlow<Int> = currentUser
        .map { it?.calorieGoal ?: 2000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    val waterDrank: StateFlow<Int> = waterRepository.totalWaterToday

    val waterGoal: StateFlow<Int> = currentUser
        .map { it?.waterGoal ?: 2500 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2500)

    val steps: StateFlow<Int> = progressRepository.stepsHistory
        .map { history -> history.filter { isToday(it.date) }.sumOf { it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stepGoal: StateFlow<Int> = currentUser
        .map { it?.stepGoal ?: 10000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)

    val proteinConsumed: StateFlow<Int> = dietRepository.totalProteinToday

    private val _proteinTarget = MutableStateFlow(150)
    val proteinTarget: StateFlow<Int> = _proteinTarget.asStateFlow()

    private val _dailyInsight = MutableStateFlow("Tap into your potential today! Your protein intake is looking great.")
    val dailyInsight: StateFlow<String> = _dailyInsight.asStateFlow()

    val isWorkoutCompleted: StateFlow<Boolean> = workoutRepository.workoutHistory
        .map { history ->
            history.any { isToday(it.date) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val weeklyStreak: StateFlow<List<Boolean>> = workoutRepository.workoutHistory
        .map { history ->
            val result = mutableListOf<Boolean>()
            // Go back 6 days + today
            for (i in 6 downTo 0) {
                val checkCal = Calendar.getInstance()
                checkCal.add(Calendar.DAY_OF_YEAR, -i)
                val day = checkCal.get(Calendar.DAY_OF_YEAR)
                val year = checkCal.get(Calendar.YEAR)
                
                val completed = history.any { session ->
                    val sCal = Calendar.getInstance()
                    sCal.timeInMillis = session.date
                    sCal.get(Calendar.DAY_OF_YEAR) == day && sCal.get(Calendar.YEAR) == year
                }
                result.add(completed)
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { false })
    
    // Logic to generate insight would go here, for now it's static
    
    init {
        viewModelScope.launch {
            authRepository.recordDailyActivity()
            if (_greetingGenerated.compareAndSet(false, true)) {
                generateDailyGreetingAI()
            }
        }
    }

    private var greetingGenerationStarted = false

    private fun generateDailyGreetingAI() {
        if (greetingGenerationStarted) return
        greetingGenerationStarted = true
        viewModelScope.launch {
            val user = currentUser
                .filterNotNull()
                .first()
                .let { u ->
                    kotlinx.coroutines.withTimeoutOrNull(3000L) { u }
                } ?: return@launch
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            if (user.lastGreetingDate == todayStr && user.lastGreeting.isNotEmpty()) {
                _dailyInsight.value = user.lastGreeting
                return@launch
            }

            try {
                val workoutHistory = workoutRepository.workoutHistory.first()
                val last7DaysWorkouts = workoutHistory.filter {
                    it.date > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                }.size

                val persona = user.coachPersona
                val tonePrompt = when (persona) {
                    "Rex" -> "extremely tough, military style, no-excuses motivator"
                    "Zen" -> "calm, encouraging, mindful and peaceful"
                    else -> "professional, data-driven, friendly and highly positive"
                }


                val accentStyle = when(user.englishAccent) {
                    "en-in" -> "Write in warm Indian English. Use 'yaar' or 'achha' naturally and sparingly. Do NOT begin every message with 'yaar'."
                    "en-gb" -> "Write in British English with British expressions."
                    "en-au" -> "Write in Australian English, relaxed and upbeat."
                    else    -> "Write in standard American English."
                }

                val prompt = """
                    You are $persona, a fitness coach with a $tonePrompt tone.
                    User: ${user.username}.
                    Context: They have completed $last7DaysWorkouts workouts in the last 7 days.
 
                    Generate a 2-part greeting for the dashboard:
                    1. A warm welcome back (e.g., "Welcome back, [Name]! Ready to crush it?")
                    2. A short, single-line powerful motivation or insight based on their progress.
 
                    Keep the entire response under 40 words. Use their accent style naturally.
                    Separate the welcome and the insight with a newline.
                    Style: $accentStyle
                """.trimIndent()

                val greeting = apiRepository.generateContent(prompt)
                val updatedUser = user.copy(lastGreeting = greeting, lastGreetingDate = todayStr)
                userRepository.saveProfile(updatedUser)
                _dailyInsight.value = greeting
            } catch (e: Exception) {
                _dailyInsight.value = "Welcome back! Every step you take today is a step closer to the best version of yourself."
            }
        }
    }

    
    private fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        
        cal.timeInMillis = timestamp
        return cal.get(Calendar.DAY_OF_YEAR) == todayDay && cal.get(Calendar.YEAR) == todayYear
    }

    fun grantXp(amount: Int) {
        viewModelScope.launch {
            authRepository.grantXp(amount)
        }
    }

    fun updateProfileImage(uri: String?) {
        viewModelScope.launch {
            userRepository.updateProfilePicture(uri)
        }
    }

    fun removeProfileImage() {
        viewModelScope.launch {
            userRepository.updateProfilePicture(null)
        }
    }
}
