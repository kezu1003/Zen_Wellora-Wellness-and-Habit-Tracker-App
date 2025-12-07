package com.example.zen_wellora

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val TAG = "HydrationWorker"
    }

    override fun doWork(): Result {
        return try {
            val startMinutes = inputData.getInt("start_minutes", 480) // 8:00 AM default
            val endMinutes = inputData.getInt("end_minutes", 1200) // 8:00 PM default
            val intervalMinutes = inputData.getLong("interval_minutes", 120L) // 2 hours default
            
            val currentTime = Calendar.getInstance()
            val currentMinutes = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
            
            Log.d(TAG, "Checking reminder time: Current=$currentMinutes, Start=$startMinutes, End=$endMinutes")
            
            // Check if current time is within the reminder window
            if (isWithinReminderWindow(currentMinutes, startMinutes, endMinutes)) {
                Log.d(TAG, "Within reminder window, showing hydration notification")
                NotificationHelper(applicationContext).showHydrationReminder()
            } else {
                Log.d(TAG, "Outside reminder window, skipping notification")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in hydration reminder worker: ${e.message}")
            Result.failure()
        }
    }
    
    private fun isWithinReminderWindow(currentMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
        return if (startMinutes <= endMinutes) {
            // Same day window (e.g., 8:00 AM to 8:00 PM)
            currentMinutes >= startMinutes && currentMinutes <= endMinutes
        } else {
            // Overnight window (e.g., 10:00 PM to 6:00 AM)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }
}
