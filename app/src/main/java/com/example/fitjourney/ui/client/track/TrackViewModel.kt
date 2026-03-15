package com.example.fitjourney.ui.client.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.repository.ProgressRepository
import com.example.fitjourney.domain.repository.UserRepository
import com.example.fitjourney.domain.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar

class TrackViewModel(
    private val userRepository: UserRepository,
    private val waterRepository: WaterRepository,
    private val progressRepository: ProgressRepository,
    private val apiRepository: com.example.fitjourney.domain.repository.ApiRepository
) : ViewModel() {

    val user = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val waterDrank: StateFlow<Int> = waterRepository.totalWaterToday
    
    val steps: StateFlow<Int> = progressRepository.stepsHistory
        .map { history -> history.filter { isToday(it.date) }.sumOf { it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addWater(ml: Int) {
        viewModelScope.launch {
            waterRepository.logWater(ml)
        }
    }

    fun removeWater(ml: Int) {
        viewModelScope.launch {
            val current = waterDrank.value
            val toRemove = if (current - ml < 0) current else ml
            if (toRemove > 0) {
                waterRepository.logWater(-toRemove)
            }
        }
    }

    fun logSteps(count: Int) {
        viewModelScope.launch {
            progressRepository.logSteps(count)
        }
    }

    fun removeSteps(count: Int) {
        viewModelScope.launch {
            val current = steps.value
            val toRemove = if (current - count < 0) current else count
            if (toRemove > 0) {
                progressRepository.logSteps(-toRemove)
            }
        }
    }

    fun updateGoals(stepGoal: Int, waterGoal: Int) {
        viewModelScope.launch {
            val current = user.value ?: return@launch
            userRepository.saveProfile(current.copy(stepGoal = stepGoal, waterGoal = waterGoal))
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        cal.timeInMillis = timestamp
        return cal.get(Calendar.DAY_OF_YEAR) == todayDay && cal.get(Calendar.YEAR) == todayYear
    }
}
