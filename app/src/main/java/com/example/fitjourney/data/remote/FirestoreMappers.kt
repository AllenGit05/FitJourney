package com.example.fitjourney.data.remote

import com.example.fitjourney.data.local.entity.*
import com.example.fitjourney.domain.model.Exercise


fun WorkoutEntity.toWorkoutFirestoreMap() = mapOf(
    "id" to id,
    "date" to date,
    "duration" to totalDurationMinutes,
    "calories" to totalCaloriesBurned,
    "exercises" to exercises.map { it.toExerciseFirestoreMap() },
    "syncedAt" to System.currentTimeMillis()
)

fun Exercise.toExerciseFirestoreMap() = mapOf(
    "name" to name,
    "sets" to sets.map { set ->
        mapOf(
            "reps" to set.reps,
            "weight" to set.weight,
            "isCompleted" to set.isCompleted
        )
    },
    "duration" to durationMinutes,
)

fun DietEntity.toDietFirestoreMap() = mapOf(
    "id" to id.toString(),
    "mealType" to mealType,
    "foodName" to foodName,
    "calories" to calories,
    "protein" to proteinGrams,
    "carbs" to carbsGrams,
    "fats" to fatsGrams,
    "timestamp" to timestamp
)

fun WaterEntity.toWaterFirestoreMap() = mapOf(
    "id" to id,
    "date" to date,
    "ml" to ml
)

fun StepsEntity.toStepsFirestoreMap() = mapOf(
    "id" to id,
    "date" to date,
    "count" to count
)

fun WeightEntity.toWeightFirestoreMap() = mapOf(
    "id" to id,
    "date" to date,
    "weight" to weight
)

fun UserEntity.toUserFirestoreMap() = mapOf(
    "username" to username,
    "role" to role,
    "email" to email,
    "isPremium" to isPremium,
    "aiCredits" to aiCredits,
    "xp" to xp,
    "level" to level,
    "fitnessGoal" to fitnessGoal,
    "weight" to weight,
    "height" to height,
    "dob" to dob,
    "gender" to gender
)

fun HabitEntity.toHabitFirestoreMap() = mapOf(
    "id" to id,
    "name" to name,
    "icon" to icon,
    "currentStreak" to currentStreak,
    "bestStreak" to bestStreak,
    "isCompletedToday" to isCompletedToday,
    "logsJson" to logsJson
)

fun ProgressPhotoEntity.toPhotoFirestoreMap() = mapOf(
    "id" to id,
    "date" to date,
    "imageUrl" to imageUrl,
    "weight" to weight,
    "note" to note
)

fun BodyMeasurementEntity.toMeasurementFirestoreMap() = mapOf(
    "id" to id,
    "date" to date,
    "waist" to waist,
    "chest" to chest,
    "arms" to arms,
    "hips" to hips,
    "legs" to legs
)

fun ChatMessageEntity.toChatFirestoreMap() = mapOf(
    "id" to firestoreId.ifEmpty { id.toString() },
    "text" to text,
    "isFromUser" to isFromUser,
    "timestamp" to timestamp,
    "repliedToId" to repliedToId,
    "repliedToText" to repliedToText
)

fun WeeklyReportEntity.toReportFirestoreMap() = mapOf(
    "id" to id,
    "weekStartDate" to weekStartDate,
    "weekEndDate" to weekEndDate,
    "averageSteps" to averageSteps,
    "totalWorkouts" to totalWorkouts,
    "averageCalories" to averageCalories,
    "averageWaterMl" to averageWaterMl,
    "weightChangeKg" to weightChangeKg,
    "aiAnalysis" to aiAnalysis,
    "generatedAt" to generatedAt
)
