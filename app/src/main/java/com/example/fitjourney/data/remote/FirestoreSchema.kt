package com.example.fitjourney.data.remote

object FirestoreSchema {

    // Root collections
    const val USERS = "users"
    const val SYNC_METADATA = "sync_metadata"

    // Sub-collections under users/{userId}/
    const val WORKOUTS = "workouts"
    const val DIET = "diet"
    const val WATER = "water"
    const val STEPS = "steps"
    const val WEIGHT = "weight"
    const val CHAT_MESSAGES = "chat_messages"
    const val HABITS = "habits"
    const val PROGRESS = "progress"
    const val BODY_MEASUREMENTS = "body_measurements"

    const val WEEKLY_REPORTS = "weekly_reports"

    // Field names — must match exactly in all operations
    object UserFields {
        const val UID = "uid"
        const val NAME = "name"
        const val EMAIL = "email"
        const val ROLE = "role"
        const val AGE = "age"
        const val GENDER = "gender"
        const val HEIGHT_CM = "heightCm"
        const val WEIGHT_KG = "weightKg"
        const val GOAL_TYPE = "goalType"
        const val ACTIVITY_LEVEL = "activityLevel"
        const val COACH_PERSONA = "coachPersona"
        const val COACH_NAME = "coachName"
        const val CREDIT_BALANCE = "creditBalance"
        const val IS_PREMIUM = "isPremium"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }

    object SyncFields {
        const val LAST_SYNCED_AT = "lastSyncedAt"
        const val PENDING_COUNT = "pendingCount"
        const val DEVICE_ID = "deviceId"
    }
}
