package com.example.zen_wellora

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    companion object {
        const val REMINDER_WORK_TAG = "hydration_reminder_work"
        const val HABIT_REMINDER_WORK_TAG = "habit_reminder_work"
        private const val TAG = "ReminderScheduler"
    }

    fun scheduleReminders(intervalMinutes: Long, startTime: String, endTime: String) {
        try {
            cancelReminders() // Cancel existing reminders first
            
            // Parse start and end times
            val startMinutes = parseTimeToMinutes(startTime)
            val endMinutes = parseTimeToMinutes(endTime)
            
            Log.d(TAG, "Scheduling reminders: Start=$startMinutes min, End=$endMinutes min, Requested=${intervalMinutes}min, Actual=${if (intervalMinutes < 15) 15 else intervalMinutes}min")
            
            // Create input data for the worker
            val inputData = Data.Builder()
                .putInt("start_minutes", startMinutes)
                .putInt("end_minutes", endMinutes)
                .putLong("interval_minutes", intervalMinutes)
                .build()
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false) // Allow when battery is low
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            // WorkManager minimum interval is 15 minutes
            val actualInterval = if (intervalMinutes < 15) 15L else intervalMinutes
            
            val reminderWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                actualInterval, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(REMINDER_WORK_TAG)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                REMINDER_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWork
            )
            
            Log.d(TAG, "Hydration reminders scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminders: ${e.message}")
        }
    }
    
    fun scheduleHabitReminders(reminderTimes: List<String>) {
        try {
            cancelHabitReminders()
            
            if (reminderTimes.isEmpty()) {
                Log.d(TAG, "No habit reminder times provided")
                return
            }
            
            Log.d(TAG, "Scheduling habit reminders for times: $reminderTimes")
            
            // Create input data for habit reminders
            val inputData = Data.Builder()
                .putStringArray("reminder_times", reminderTimes.toTypedArray())
                .build()
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            val habitReminderWork = PeriodicWorkRequestBuilder<HabitReminderWorker>(
                1, TimeUnit.HOURS // Check every hour for habit reminders
            )
                .setConstraints(constraints)
                .addTag(HABIT_REMINDER_WORK_TAG)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                HABIT_REMINDER_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                habitReminderWork
            )
            
            Log.d(TAG, "Habit reminders scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling habit reminders: ${e.message}")
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
                480 // Default to 8:00 AM (8 * 60 = 480 minutes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time '$timeString': ${e.message}")
            480 // Default to 8:00 AM
        }
    }

    fun cancelReminders() {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(REMINDER_WORK_TAG)
            Log.d(TAG, "Hydration reminders cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminders: ${e.message}")
        }
    }
    
    fun cancelHabitReminders() {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(HABIT_REMINDER_WORK_TAG)
            Log.d(TAG, "Habit reminders cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling habit reminders: ${e.message}")
        }
    }
    
    fun cancelAllReminders() {
        cancelReminders()
        cancelHabitReminders()
    }
}
