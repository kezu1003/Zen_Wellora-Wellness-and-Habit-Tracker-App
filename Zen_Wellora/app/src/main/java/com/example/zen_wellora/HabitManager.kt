// HabitManager.kt
package com.example.zen_wellora

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HabitManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val habits = mutableListOf<Habit>()
    private val reminderScheduler = ReminderScheduler(context)

    companion object {
        private const val HABITS_KEY = "user_habits"
    }

    // Add new habit
    fun addHabit(name: String, description: String, category: String, icon: String = "exercise"): Boolean {
        return try {
            // Validate inputs
            if (name.trim().isEmpty()) {
                return false
            }
            
            // Load existing habits first to ensure we don't lose them
            loadHabitsFromStorage()
            
            val newHabit = Habit(
                name = name.trim(),
                description = description.trim(),
                category = category.trim(),
                icon = icon.trim()
            )
            habits.add(newHabit)
            saveHabitsToStorage()
        } catch (e: Exception) {
            false
        }
    }

    // Edit existing habit
    fun editHabit(habitId: Long, newName: String, newDescription: String, newCategory: String, newIcon: String = "exercise"): Boolean {
        return try {
            // Validate inputs
            if (newName.trim().isEmpty()) {
                return false
            }
            
            // Load existing habits first to ensure we have the latest data
            loadHabitsFromStorage()
            
            val habit = habits.find { it.id == habitId }
            habit?.let {
                // Since we're using data class, we need to replace the habit
                val updatedHabit = it.copy(
                    name = newName.trim(),
                    description = newDescription.trim(),
                    category = newCategory.trim(),
                    icon = newIcon.trim()
                )
                habits.remove(it)
                habits.add(updatedHabit)
                return saveHabitsToStorage()
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    // Delete habit
    fun deleteHabit(habitId: Long): Boolean {
        return try {
            val iterator = habits.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().id == habitId) {
                    iterator.remove()
                    return saveHabitsToStorage()
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    // Mark habit as completed
    fun markHabitCompleted(habitId: Long): Boolean {
        return try {
            val habit = habits.find { it.id == habitId }
            habit?.let {
                it.markCompleted()
                return saveHabitsToStorage()
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    // Unmark habit
    fun unmarkHabit(habitId: Long): Boolean {
        return try {
            val habit = habits.find { it.id == habitId }
            habit?.let {
                it.unmarkCompleted()
                return saveHabitsToStorage()
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    // Get all habits
    fun getAllHabits(): List<Habit> {
        return habits.toList()
    }

    // Get habit by ID
    fun getHabitById(habitId: Long): Habit? {
        return habits.find { it.id == habitId }
    }

    // Calculate today's progress
    fun getTodayProgress(): Pair<Int, Int> {
        val total = habits.size
        val completed = habits.count { it.completed }
        return Pair(completed, total)
    }

    // Calculate completion percentage
    fun getTodayCompletionPercentage(): Int {
        val (completed, total) = getTodayProgress()
        return if (total > 0) (completed * 100) / total else 0
    }

    // Get best streak
    fun getBestStreak(): Int {
        return sharedPreferences.getInt("best_streak", 0)
    }

    // Save habits to SharedPreferences
    private fun saveHabitsToStorage(): Boolean {
        return try {
            val habitsJson = gson.toJson(habits)
            val success = sharedPreferences.edit().putString(HABITS_KEY, habitsJson).commit()
            
            // Update widget when habits change
            if (success) {
                updateWidget()
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }

    // Load habits from SharedPreferences
    fun loadHabitsFromStorage() {
        try {
            val habitsJson = sharedPreferences.getString(HABITS_KEY, "[]")
            val type = object : TypeToken<List<Habit>>() {}.type
            val loadedHabits: List<Habit> = gson.fromJson(habitsJson, type) ?: emptyList()
            habits.clear()
            habits.addAll(loadedHabits)
            
            // Update completion status for all habits
            habits.forEach { it.updateCompletionStatus() }
        } catch (e: Exception) {
            // If loading fails, clear habits list
            habits.clear()
        }
    }

    // Update streak in SharedPreferences
    fun updateStreak() {
        val currentStreak = sharedPreferences.getInt("current_streak", 0)
        val bestStreak = sharedPreferences.getInt("best_streak", 0)

        val newStreak = currentStreak + 1
        val newBestStreak = maxOf(bestStreak, newStreak)

        sharedPreferences.edit().apply {
            putInt("current_streak", newStreak)
            putInt("best_streak", newBestStreak)
            apply()
        }
    }
    
    // Habit reminder methods
    fun setHabitReminderTimes(reminderTimes: List<String>) {
        try {
            val timesJson = gson.toJson(reminderTimes)
            sharedPreferences.edit().putString("habit_reminder_times", timesJson).apply()
            
            // Schedule habit reminders
            if (reminderTimes.isNotEmpty()) {
                reminderScheduler.scheduleHabitReminders(reminderTimes)
            } else {
                reminderScheduler.cancelHabitReminders()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    fun getHabitReminderTimes(): List<String> {
        return try {
            val timesJson = sharedPreferences.getString("habit_reminder_times", "[]")
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(timesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun enableHabitReminders(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("habit_reminders_enabled", enabled).apply()
        
        if (enabled) {
            val reminderTimes = getHabitReminderTimes()
            if (reminderTimes.isNotEmpty()) {
                reminderScheduler.scheduleHabitReminders(reminderTimes)
            }
        } else {
            reminderScheduler.cancelHabitReminders()
        }
    }
    
    fun areHabitRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean("habit_reminders_enabled", false)
    }
    
    // Update widget when habit data changes
    private fun updateWidget() {
        try {
            val intent = Intent(context, HabitWidget::class.java)
            intent.action = "UPDATE_WIDGET"
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            // Widget update failed, but don't crash the app
        }
    }
}
