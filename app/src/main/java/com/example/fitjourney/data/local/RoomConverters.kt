package com.example.fitjourney.data.local

import androidx.room.TypeConverter
import com.example.fitjourney.domain.model.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        val listType = object : TypeToken<List<Exercise>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromHabitLogList(value: List<com.example.fitjourney.domain.model.HabitLog>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHabitLogList(value: String): List<com.example.fitjourney.domain.model.HabitLog> {
        val listType = object : TypeToken<List<com.example.fitjourney.domain.model.HabitLog>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
