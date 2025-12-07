package com.example.zen_wellora

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val TAG = "HabitReminderWorker"
        private const val PREFS_NAME = "user_prefs"
        private const val LAST_HABIT_NOTIFICATION_KEY = "last_habit_notification_date"
    }

    override fun doWork(): Result {
        return try {
            val reminderTimes = inputData.getStringArray("reminder_times") ?: arrayOf()
            
            if (reminderTimes.isEmpty()) {
                Log.d(TAG, "No habit reminder times configured")
                return Result.success()
            }
            
            val currentTime = Calendar.getInstance()
            val currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime.time)
            val currentMinutes = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
            
            Log.d(TAG, "Checking habit reminders at $currentTimeString")
            
            // Check if we should show a reminder at this time
            val shouldShowReminder = reminderTimes.any { reminderTime ->
                val reminderMinutes = parseTimeToMinutes(reminderTime)
                // Show reminder if we're within 30 minutes of the scheduled time
                val timeDiff = kotlin.math.abs(currentMinutes - reminderMinutes)
                timeDiff <= 30 || (1440 - timeDiff) <= 30 // Account for day wraparound
            }
            
            if (shouldShowReminder && !hasShownTodaysNotification()) {
                Log.d(TAG, "Showing habit reminder notification")
                NotificationHelper(applicationContext).showHabitReminder()
                markTodaysNotificationShown()
            } else {
                Log.d(TAG, "No habit reminder needed at this time")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in habit reminder worker: ${e.message}")
            Result.failure()
        }
    }
    
    private fun parseTimeToMinutes(timeString: String): Int {
        return try {
            val format12Hour = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val format24Hour = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            val time = try {
                format12Hour.parse(timeString)
            } catch (e: Exception) {
                format24Hour.parse(timeString)
            }
            
            if (time != null) {
                val calendar = Calendar.getInstance().apply {
                    this.time = time
                }
                val hours = calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = calendar.get(Calendar.MINUTE)
                hours * 60 + minutes
            } else {
                540 // Default to 9:00 AM
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time '$timeString': ${e.message}")
            540 // Default to 9:00 AM
        }
    }
    
    private fun hasShownTodaysNotification(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotificationDate = prefs.getString(LAST_HABIT_NOTIFICATION_KEY, "")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        return lastNotificationDate == today
    }
    
    private fun markTodaysNotificationShown() {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        prefs.edit().putString(LAST_HABIT_NOTIFICATION_KEY, today).apply()
    }
}