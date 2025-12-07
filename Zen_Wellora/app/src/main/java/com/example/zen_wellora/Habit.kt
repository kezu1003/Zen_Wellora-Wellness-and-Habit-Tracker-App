// Habit.kt
package com.example.zen_wellora

import java.util.Calendar

data class Habit(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val description: String,
    val icon: String = "exercise",
    val category: String = "General",
    var completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    var completedDates: MutableList<Long> = mutableListOf()
) {
    fun markCompleted() {
        val today = getStartOfDay()
        if (!completedDates.contains(today)) {
            completedDates.add(today)
        }
        completed = completedDates.contains(today)
    }

    fun unmarkCompleted() {
        val today = getStartOfDay()
        completedDates.remove(today)
        completed = completedDates.contains(today)
    }

    fun updateCompletionStatus() {
        val today = getStartOfDay()
        completed = completedDates.contains(today)
    }

    fun getCompletionPercentage(days: Int = 30): Float {
        if (completedDates.isEmpty()) return 0f

        val startDate = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val recentCompletions = completedDates.count { it >= startDate }
        return (recentCompletions.toFloat() / days) * 100
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}