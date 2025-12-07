package com.example.zen_wellora

import java.text.SimpleDateFormat
import java.util.*

data class RecentActivity(
    val id: Long = System.currentTimeMillis(),
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val points: Int = 0,
    val iconRes: Int = R.drawable.check_circle
) {
    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now" // Less than 1 minute
            diff < 3600000 -> "${diff / 60000} minutes ago" // Less than 1 hour
            diff < 86400000 -> "${diff / 3600000} hours ago" // Less than 1 day
            diff < 604800000 -> "${diff / 86400000} days ago" // Less than 1 week
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
}

enum class ActivityType {
    HABIT_COMPLETED,
    HABIT_ADDED,
    MOOD_LOGGED,
    WATER_LOGGED,
    EXERCISE_LOGGED,
    MEDITATION_LOGGED,
    SLEEP_LOGGED,
    STUDY_LOGGED,
    WORKOUT_LOGGED,
    JOURNAL_LOGGED,
    WALKING_LOGGED,
    YOGA_LOGGED,
    MUSIC_LOGGED,
    ART_LOGGED
}










