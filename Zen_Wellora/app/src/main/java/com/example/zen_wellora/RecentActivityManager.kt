package com.example.zen_wellora

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentActivityManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("recent_activities", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "RecentActivityManager"
        private const val ACTIVITIES_KEY = "recent_activities_list"
        private const val MAX_ACTIVITIES = 10
    }
    
    // Add a new activity
    fun addActivity(activity: RecentActivity) {
        try {
            val activities = getActivities().toMutableList()
            
            // Add new activity at the beginning
            activities.add(0, activity)
            
            // Keep only the most recent activities
            if (activities.size > MAX_ACTIVITIES) {
                activities.subList(MAX_ACTIVITIES, activities.size).clear()
            }
            
            saveActivities(activities)
            Log.d(TAG, "Activity added: ${activity.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding activity: ${e.message}")
        }
    }
    
    // Get all activities
    fun getActivities(): List<RecentActivity> {
        return try {
            val json = sharedPreferences.getString(ACTIVITIES_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<RecentActivity>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading activities: ${e.message}")
            emptyList()
        }
    }
    
    // Save activities to SharedPreferences
    private fun saveActivities(activities: List<RecentActivity>) {
        try {
            val json = gson.toJson(activities)
            sharedPreferences.edit().putString(ACTIVITIES_KEY, json).apply()
            Log.d(TAG, "Activities saved: ${activities.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving activities: ${e.message}")
        }
    }
    
    // Clear all activities
    fun clearActivities() {
        try {
            sharedPreferences.edit().remove(ACTIVITIES_KEY).apply()
            Log.d(TAG, "All activities cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing activities: ${e.message}")
        }
    }
    
    // Add sample activities for demonstration
    fun addSampleActivities() {
        try {
            val sampleActivities = listOf(
                RecentActivity(
                    type = ActivityType.HABIT_COMPLETED,
                    title = "Completed Morning Meditation",
                    description = "Meditation session completed successfully",
                    points = 10,
                    iconRes = R.drawable.ic_meditation
                ),
                RecentActivity(
                    type = ActivityType.MOOD_LOGGED,
                    title = "Logged mood as Happy",
                    description = "Mood tracking entry added",
                    points = 5,
                    iconRes = R.drawable.mood_emoji
                ),
                RecentActivity(
                    type = ActivityType.WATER_LOGGED,
                    title = "Drank 2 glasses of water",
                    description = "Hydration goal progress",
                    points = 5,
                    iconRes = R.drawable.ic_water
                ),
                RecentActivity(
                    type = ActivityType.EXERCISE_LOGGED,
                    title = "Completed 30-min workout",
                    description = "Fitness activity logged",
                    points = 15,
                    iconRes = R.drawable.ic_exercise
                ),
                RecentActivity(
                    type = ActivityType.HABIT_ADDED,
                    title = "Added new habit: Reading",
                    description = "New reading habit created",
                    points = 0,
                    iconRes = R.drawable.ic_reading
                )
            )
            
            // Clear existing activities and add sample ones
            clearActivities()
            sampleActivities.forEach { addActivity(it) }
            
            Log.d(TAG, "Sample activities added")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding sample activities: ${e.message}")
        }
    }
}










