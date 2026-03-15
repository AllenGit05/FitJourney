package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.local.dao.HabitDao
import com.example.fitjourneyag.data.local.entity.HabitEntity
import com.example.fitjourneyag.domain.model.Habit
import com.example.fitjourneyag.domain.model.HabitLog
import com.example.fitjourneyag.domain.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class HabitRepositoryImpl(
    private val habitDao: HabitDao
) : HabitRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override val habits: StateFlow<List<Habit>> = habitDao.getAllHabits()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    init {
        repositoryScope.launch {
            seedData()
        }
    }

    private suspend fun seedData() {
        val current = habitDao.getAllHabits().first()
        if (current.isEmpty()) {
            val defaults = listOf(
                Habit(name = "Drink Water", icon = "WaterDrop"),
                Habit(name = "Sleep before 11pm", icon = "Bedtime"),
                Habit(name = "Walk 10k Steps", icon = "DirectionsWalk"),
                Habit(name = "Meditation", icon = "SelfImprovement")
            )
            defaults.forEach { habitDao.insertHabit(it.toEntity()) }
        }
    }

    override suspend fun toggleHabit(habitId: String, date: Long) {
        val habit = habits.value.find { it.id == habitId } ?: return
        val today = isSameDay(date, System.currentTimeMillis())
        val existingLog = habit.logs.find { isSameDay(it.date, date) }
        
        val newLogs = if (existingLog != null) {
            habit.logs.filter { it.id != existingLog.id }
        } else {
            habit.logs + HabitLog(date = date)
        }

        // Recalculate streak
        val streak = calculateStreak(newLogs)
        
        val updatedHabit = habit.copy(
            logs = newLogs,
            isCompletedToday = if (today) existingLog == null else habit.isCompletedToday,
            currentStreak = streak,
            bestStreak = if (streak > habit.bestStreak) streak else habit.bestStreak
        )
        habitDao.insertHabit(updatedHabit.toEntity())
    }

    override suspend fun addHabit(name: String, icon: String) {
        val newHabit = Habit(name = name, icon = icon)
        habitDao.insertHabit(newHabit.toEntity())
    }

    override suspend fun deleteHabit(habitId: String) {
        val habit = habits.value.find { it.id == habitId } ?: return
        habitDao.deleteHabit(habit.toEntity())
    }

    private fun isSameDay(ts1: Long, ts2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun calculateStreak(logs: List<HabitLog>): Int {
        if (logs.isEmpty()) return 0
        val sortedDates = logs.map { it.date }.sortedDescending()
        
        var streak = 0
        val cal = Calendar.getInstance()
        
        val now = System.currentTimeMillis()
        var currentCheck = now
        
        val completedToday = logs.any { isSameDay(it.date, now) }
        if (!completedToday) {
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -1)
            currentCheck = cal.timeInMillis
        }

        for (date in sortedDates) {
            if (isSameDay(date, currentCheck)) {
                streak++
                cal.timeInMillis = currentCheck
                cal.add(Calendar.DAY_OF_YEAR, -1)
                currentCheck = cal.timeInMillis
            } else if (date < currentCheck) {
                break
            }
        }
        return streak
    }

    private fun HabitEntity.toDomain(): Habit = Habit(
        id = id,
        name = name,
        icon = icon,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        isCompletedToday = isCompletedToday,
        logs = try {
            val listType = object : com.google.gson.reflect.TypeToken<List<HabitLog>>() {}.type
            com.google.gson.Gson().fromJson(logsJson, listType) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    )

    private fun Habit.toEntity(): HabitEntity = HabitEntity(
        id = id,
        name = name,
        icon = icon,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        isCompletedToday = isCompletedToday,
        logsJson = com.google.gson.Gson().toJson(logs)
    )
}
