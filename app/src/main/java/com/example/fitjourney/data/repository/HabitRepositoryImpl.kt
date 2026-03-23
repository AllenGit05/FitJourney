package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.HabitDao
import com.example.fitjourney.data.local.entity.HabitEntity
import com.example.fitjourney.domain.model.Habit
import com.example.fitjourney.domain.model.HabitLog
import com.example.fitjourney.domain.repository.HabitRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.io.Closeable

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : HabitRepository, Closeable {
    
    // The scope's lifetime matches the Application lifecycle as this is a singleton in AppContainer.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
        val entity = habitDao.getHabitById(habitId) ?: return 
        
        // Weekly Reset Check
        val currentWeek = getWeekString()
        var freezesUsed = entity.freezesUsedThisWeek
        var resetWeek = entity.lastFreezeResetDate
        
        if (resetWeek != currentWeek) {
            freezesUsed = 0
            resetWeek = currentWeek
        }

        val today = isSameDay(date, System.currentTimeMillis())
        val existingLog = habit.logs.find { isSameDay(it.date, date) }
        
        val newLogs = if (existingLog != null) {
            habit.logs.filter { it.id != existingLog.id }
        } else {
            habit.logs + HabitLog(date = date)
        }

        // Recalculate streak with freeze
        val (streak, usedFreezesInCalculation) = calculateStreak(newLogs)
        
        val updatedHabit = habit.copy(
            logs = newLogs,
            isCompletedToday = if (today) existingLog == null else habit.isCompletedToday,
            currentStreak = streak,
            bestStreak = if (streak > habit.bestStreak) streak else habit.bestStreak,
            isMastered = streak >= 21,
            isMilestone = streak >= 21,
            freezesUsedThisWeek = usedFreezesInCalculation
        )


        
        habitDao.insertHabit(updatedHabit.toEntity().copy(
            isSynced = false,
            freezesUsedThisWeek = usedFreezesInCalculation,
            lastFreezeResetDate = resetWeek
        ))
        syncManager.startSync()
    }

    private fun getWeekString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.WEEK_OF_YEAR)}"
    }

    override suspend fun addHabit(name: String, icon: String) {
        val newHabit = Habit(name = name, icon = icon)
        habitDao.insertHabit(newHabit.toEntity().copy(isSynced = false))
        syncManager.startSync()
    }

    override suspend fun deleteHabit(habitId: String) {
        habitDao.softDelete(habitId)
        syncManager.startSync()
    }

    override fun close() {
        repositoryScope.cancel()
    }

    private fun isSameDay(ts1: Long, ts2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun calculateStreak(logs: List<HabitLog>): Pair<Int, Int> {
        if (logs.isEmpty()) return Pair(0, 0)
        val sortedDates = logs.map { it.date }.sortedDescending()
        
        var streak = 0
        var freezesUsed = 0
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
                // Check if it's the day before (exactly 1 day gap)
                cal.timeInMillis = currentCheck
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val dayBefore = cal.timeInMillis
                
                if (isSameDay(date, dayBefore) && freezesUsed < 1) {
                    // Use a freeze for the gap
                    freezesUsed++
                    streak += 2 // Count both the frozen gap and the current date item
                    
                    cal.timeInMillis = dayBefore
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    currentCheck = cal.timeInMillis
                } else if (isSameDay(date, currentCheck)) {
                   // Continue
                } else {
                    break
                }
            }
        }
        return Pair(streak, freezesUsed)
    }

    private fun HabitEntity.toDomain(): Habit = Habit(
        id = id,
        name = name,
        icon = icon,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        isCompletedToday = isCompletedToday,
        isMastered = isMastered,
        isMilestone = isMilestone,
        freezesUsedThisWeek = freezesUsedThisWeek,

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
        isMastered = isMastered,
        isMilestone = isMilestone,
        freezesUsedThisWeek = freezesUsedThisWeek,

        logsJson = com.google.gson.Gson().toJson(logs)
    )

}
