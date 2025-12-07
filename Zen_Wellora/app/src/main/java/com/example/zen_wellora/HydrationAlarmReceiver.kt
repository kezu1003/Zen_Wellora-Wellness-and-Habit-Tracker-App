package com.example.zen_wellora

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class HydrationAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "HydrationAlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Hydration alarm received")
        
        try {
            val intervalMinutes = intent.getLongExtra("interval_minutes", 120L)
            val startMinutes = intent.getIntExtra("start_minutes", 480) // 8:00 AM
            val endMinutes = intent.getIntExtra("end_minutes", 1200) // 8:00 PM
            
            val currentTime = Calendar.getInstance()
            val currentMinutes = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
            
            Log.d(TAG, "Checking alarm time: Current=$currentMinutes, Start=$startMinutes, End=$endMinutes")
            
            // Check if current time is within the reminder window
            if (isWithinReminderWindow(currentMinutes, startMinutes, endMinutes)) {
                Log.d(TAG, "Within reminder window, showing hydration notification")
                
                // Show the hydration notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showHydrationReminder()
                
                // Schedule the next alarm
                val alarmScheduler = HydrationAlarmScheduler(context)
                
                // Get time strings (we need to convert minutes back to time strings)
                val startTimeString = minutesToTimeString(startMinutes)
                val endTimeString = minutesToTimeString(endMinutes)
                
                alarmScheduler.scheduleHydrationAlarms(intervalMinutes, startTimeString, endTimeString)
                
            } else {
                Log.d(TAG, "Outside reminder window, skipping notification")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in hydration alarm receiver: ${e.message}")
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
    
    private fun minutesToTimeString(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, mins)
        }
        return android.text.format.DateFormat.format("hh:mm a", calendar).toString()
    }
}