package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.*
import com.example.fitjourney.data.local.entity.*
import com.example.fitjourney.domain.model.Exercise
import com.example.fitjourney.domain.model.WorkoutSet
import kotlinx.coroutines.flow.first
import java.util.*

class PrototypeDataSeeder(
    private val workoutDao: WorkoutDao,
    private val dietDao: DietDao,
    private val stepsDao: StepsDao,
    private val weightDao: WeightDao,
    private val habitDao: HabitDao
) {
    suspend fun seedIfNeeded() {
        return // Seeding disabled - real users start with empty data
    }

    private suspend fun seedHistoricalData() {
        val cal = Calendar.getInstance()
        val random = Random()

        for (i in 0..7) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val timestamp = cal.timeInMillis

            // Seed Steps
            val stepCount = 5000 + random.nextInt(7000)
            stepsDao.insertSteps(StepsEntity(date = timestamp, count = stepCount, isSynced = false))

            // Seed Weight (slightly fluctuating)
            val baseWeight = 75.0f
            val weight = baseWeight - (i * 0.1f) + (random.nextFloat() * 0.5f)
            weightDao.insertWeight(WeightEntity(date = timestamp, weight = weight, isSynced = false))

            // Seed Diet Logs (3 meals + snack)
            seedDietDay(timestamp, random)

            // Seed Workouts (every other day)
            if (i % 2 == 0) {
                seedWorkoutDay(timestamp, random)
            }
        }
    }

    private suspend fun seedDietDay(timestamp: Long, random: Random) {
        val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        val foods = mapOf(
            "Breakfast" to listOf("Oatmeal", "Eggs & Toast", "Smoothie Bowl"),
            "Lunch" to listOf("Chicken Salad", "Quinoa Bowl", "Turkey Sandwich"),
            "Dinner" to listOf("Grilled Salmon", "Steak & Veggies", "Pasta Primavera"),
            "Snack" to listOf("Greek Yogurt", "Apple & Peanut Butter", "Protein Bar")
        )

        meals.forEach { mealType ->
            val foodName = foods[mealType]?.random() ?: "Healthy Meal"
            val calories = 300 + random.nextInt(400)
            dietDao.insertLog(DietEntity(
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                proteinGrams = (15 + random.nextInt(15)).toFloat(),
                carbsGrams = (20 + random.nextInt(30)).toFloat(),
                fatsGrams = (5 + random.nextInt(15)).toFloat(),
                timestamp = timestamp,
                isSynced = false
            ))
        }
    }

    private suspend fun seedWorkoutDay(timestamp: Long, random: Random) {
        val exercises = listOf(
            Exercise(
                name = "Push Ups",
                sets = List(3) { WorkoutSet(reps = 10 + random.nextInt(10), weight = 0f, isCompleted = true) },
                durationMinutes = 5,
                caloriesBurned = 50
            ),
            Exercise(
                name = "Squats",
                sets = List(3) { WorkoutSet(reps = 12 + random.nextInt(8), weight = 0f, isCompleted = true) },
                durationMinutes = 10,
                caloriesBurned = 80
            ),
            Exercise(
                name = "Plank",
                sets = List(2) { WorkoutSet(reps = 1, weight = 0f, isCompleted = true) },
                durationMinutes = 5,
                caloriesBurned = 30
            )
        )

        workoutDao.insertSession(WorkoutEntity(
            id = UUID.randomUUID().toString(),
            date = timestamp,
            totalDurationMinutes = 20 + random.nextInt(20),
            totalCaloriesBurned = 150 + random.nextInt(200),
            exercises = exercises,
            isSynced = false
        ))
    }
}
