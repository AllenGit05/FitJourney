package com.example.fitjourney.ui.client.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitjourney.domain.model.User
import com.example.fitjourney.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Editable state
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _height = MutableStateFlow("")
    val height: StateFlow<String> = _height.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _goalWeight = MutableStateFlow("")
    val goalWeight: StateFlow<String> = _goalWeight.asStateFlow()

    private val _activityLevel = MutableStateFlow("")
    val activityLevel: StateFlow<String> = _activityLevel.asStateFlow()

    private val _foodType = MutableStateFlow("")
    val foodType: StateFlow<String> = _foodType.asStateFlow()

    private val _dob = MutableStateFlow("")
    val dob: StateFlow<String> = _dob.asStateFlow()

    private val _coachName = MutableStateFlow("")
    val coachName: StateFlow<String> = _coachName.asStateFlow()

    private val _coachGender = MutableStateFlow("")
    val coachGender: StateFlow<String> = _coachGender.asStateFlow()

    private val _coachPersona = MutableStateFlow("")
    val coachPersona: StateFlow<String> = _coachPersona.asStateFlow()

    private val _customCoachPersona = MutableStateFlow("")
    val customCoachPersona: StateFlow<String> = _customCoachPersona.asStateFlow()

    private val _calorieGoal = MutableStateFlow(2000)
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    private val _fitnessGoal = MutableStateFlow("Maintain Weight")
    val fitnessGoal: StateFlow<String> = _fitnessGoal.asStateFlow()

    private val _showCreditStore = MutableStateFlow(false)
    val showCreditStore: StateFlow<Boolean> = _showCreditStore.asStateFlow()

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                user?.let {
                    _username.value = it.username
                    _gender.value = it.gender
                    _height.value = it.height
                    _weight.value = it.weight
                    _goalWeight.value = it.goalWeight
                    _activityLevel.value = it.activityLevel
                    _foodType.value = it.foodType
                    _dob.value = it.dob
                    _coachName.value = it.coachName
                    _coachGender.value = it.coachGender
                    _coachPersona.value = it.coachPersona
                    _customCoachPersona.value = it.customCoachPersona
                    _calorieGoal.value = it.calorieGoal
                    _fitnessGoal.value = it.fitnessGoal
                }
            }
        }
    }

    fun autoCalculateCalorieGoal() {
        val h = _height.value.toDoubleOrNull() ?: return
        val w = _weight.value.toDoubleOrNull() ?: return
        val age = calculateAge(_dob.value) ?: 25
        val genderStr = _gender.value
        
        // Mifflin-St Jeor
        val bmr = (10 * w) + (6.25 * h) - (5 * age) + (if (genderStr == "Male") 5 else -161)
        
        val multiplier = when (_activityLevel.value) {
            "Sedentary" -> 1.2
            "Low" -> 1.375
            "Moderate" -> 1.55
            "High" -> 1.725
            "Very High" -> 1.9
            else -> 1.2
        }
        
        val tdee = bmr * multiplier
        
        val adjusted = when (_fitnessGoal.value) {
            "Lose Weight" -> tdee - 500
            "Maintain Weight" -> tdee
            "Gain Weight" -> tdee + 500
            else -> tdee
        }
        
        _calorieGoal.value = adjusted.toInt()
    }

    private fun calculateAge(dobStr: String): Int? {
        return try {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val birthDate = sdf.parse(dobStr) ?: return null
            val today = java.util.Calendar.getInstance()
            val birth = java.util.Calendar.getInstance().apply { time = birthDate }
            var age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            null
        }
    }

    fun updateField(setter: (String) -> Unit, value: String) {
        setter(value)
    }

    // Setters for UI
    fun setUsername(v: String) { _username.value = v }
    fun setGender(v: String) { _gender.value = v }
    fun setHeight(v: String) { _height.value = v }
    fun setWeight(v: String) { _weight.value = v }
    fun setGoalWeight(v: String) { _goalWeight.value = v }
    fun setActivityLevel(v: String) { _activityLevel.value = v }
    fun setFoodType(v: String) { _foodType.value = v }
    fun setDob(v: String) { _dob.value = v }
    fun setCoachName(v: String) { _coachName.value = v }
    fun setCoachGender(v: String) { _coachGender.value = v }
    fun setCoachPersona(v: String) { 
        _coachPersona.value = v 
        when(v) {
            "Rex" -> {
                _coachName.value = "Sergeant Rex"
                _coachGender.value = "Male"
            }
            "Zen" -> {
                _coachName.value = "Zen Master"
                _coachGender.value = "Male"
            }
            "Aurora" -> {
                _coachName.value = "Aurora"
                _coachGender.value = "Female"
            }
        }
    }
    fun setCustomCoachPersona(v: String) { _customCoachPersona.value = v }
    fun setCalorieGoal(v: String) { _calorieGoal.value = v.toIntOrNull() ?: _calorieGoal.value }
    fun setFitnessGoal(v: String) { _fitnessGoal.value = v }

    fun cancelSubscription() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = user.copy(isPremium = false)
            authRepository.updateUserProfile(updatedUser)
            _isLoading.value = false
        }
    }

    fun showStore() { _showCreditStore.value = true }
    fun dismissCreditStore() { _showCreditStore.value = false }

    fun purchaseOption(option: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = when(option) {
                "Starter" -> user.copy(aiCredits = user.aiCredits + 50)
                "Pro" -> user.copy(aiCredits = user.aiCredits + 200)
                "Elite" -> user.copy(isPremium = true)
                else -> user
            }
            authRepository.updateUserProfile(updatedUser)
            _isLoading.value = false
            _showCreditStore.value = false
        }
    }

    fun updateProfileImage(uri: String?) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(profilePictureUri = uri)
            authRepository.updateUserProfile(updatedUser)
        }
    }

    fun removeProfileImage() {
        updateProfileImage(null)
    }

    fun saveProfile() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = user.copy(
                username = _username.value,
                gender = _gender.value,
                height = _height.value,
                weight = _weight.value,
                goalWeight = _goalWeight.value,
                activityLevel = _activityLevel.value,
                foodType = _foodType.value,
                dob = _dob.value,
                coachName = _coachName.value,
                coachGender = _coachGender.value,
                coachPersona = _coachPersona.value,
                customCoachPersona = _customCoachPersona.value,
                calorieGoal = _calorieGoal.value,
                fitnessGoal = _fitnessGoal.value
            )
            authRepository.updateUserProfile(updatedUser)
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
