package com.example.fitjourney.ui.client.calculators

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

data class MacrosResult(
    val protein: Int = 0,
    val carbs: Int = 0,
    val fats: Int = 0,
    val calories: Int = 0
)

class CalculatorsViewModel : ViewModel() {

    private val _bmiResult = MutableStateFlow(0f)
    val bmiResult: StateFlow<Float> = _bmiResult.asStateFlow()

    private val _tdeeResult = MutableStateFlow(0f)
    val tdeeResult: StateFlow<Float> = _tdeeResult.asStateFlow()

    private val _macrosResult = MutableStateFlow(MacrosResult())
    val macrosResult: StateFlow<MacrosResult> = _macrosResult.asStateFlow()

    fun calculateBMI(weightKg: Float, heightCm: Float) {
        if (heightCm > 0) {
            val heightM = heightCm / 100
            _bmiResult.value = weightKg / (heightM.pow(2))
        }
    }

    fun calculateTDEE(weightKg: Float, heightCm: Float, age: Int, isMale: Boolean, activityMultiplier: Float) {
        if (weightKg > 0 && heightCm > 0 && age > 0) {
            // Mifflin-St Jeor Equation
            val bmr = if (isMale) {
                (10 * weightKg) + (6.25f * heightCm) - (5 * age) + 5
            } else {
                (10 * weightKg) + (6.25f * heightCm) - (5 * age) - 161
            }
            _tdeeResult.value = bmr * activityMultiplier
        }
    }

    fun calculateMacros(goal: String, calories: Int, weightKg: Float) {
        if (calories > 0 && weightKg > 0) {
            // Ratios based on Fitness Goals
            val (proteinFactor, fatPercent) = when (goal) {
                "Fat Loss" -> 2.4f to 0.20f   // High protein to preserve muscle, lower fat
                "Muscle Gain" -> 2.0f to 0.25f // Standard protein, moderate fat
                else -> 2.0f to 0.30f          // Maintenance: balanced
            }
            
            val proteinG = (weightKg * proteinFactor).toInt()
            val fatCal = (calories * fatPercent).toInt()
            val fatG = fatCal / 9
            
            val proteinCal = proteinG * 4
            val carbCal = calories - proteinCal - fatCal
            val carbG = if (carbCal > 0) carbCal / 4 else 0
            
            _macrosResult.value = MacrosResult(
                protein = proteinG,
                carbs = carbG,
                fats = fatG,
                calories = calories
            )
        }
    }

    fun calculateFullMacros(
        goal: String, 
        weightKg: Float, 
        heightCm: Float, 
        age: Int, 
        isMale: Boolean, 
        activityMultiplier: Float
    ) {
        if (weightKg > 0 && heightCm > 0 && age > 0) {
            // 1. Calculate BMR (Mifflin-St Jeor)
            val bmr = if (isMale) {
                (10 * weightKg) + (6.25f * heightCm) - (5 * age) + 5
            } else {
                (10 * weightKg) + (6.25f * heightCm) - (5 * age) - 161
            }
            
            // 2. Calculate TDEE (Maintenance)
            val tdee = bmr * activityMultiplier
            _tdeeResult.value = tdee
            
            // 3. Adjust Calories based on Goal
            val targetCalories = when (goal) {
                "Fat Loss" -> (tdee - 500).toInt()
                "Muscle Gain" -> (tdee + 300).toInt()
                else -> tdee.toInt()
            }
            
            // 4. Calculate Macros
            calculateMacros(goal, targetCalories, weightKg)
        }
    }
}
