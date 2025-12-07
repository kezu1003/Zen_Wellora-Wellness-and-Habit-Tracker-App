package com.example.zen_wellora

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HydrationAlarmScheduler(private val context: Context) {
    
    companion object {
        private const val TAG = "HydrationAlarmScheduler"
        private const val REQUEST_CODE = 12345
    }
    
    fun scheduleHydrationAlarms(intervalMinutes: Long, startTime: String, endTime: String) {
        try {
            cancelHydrationAlarms()
            
            val startMinutes = parseTimeToMinutes(startTime)
            val endMinutes = parseTimeToMinutes(endTime)
            
            Log.d(TAG, "Scheduling hydration alarms: Start=$startMinutes min, End=$endMinutes min, Interval=${intervalMinutes}min")
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Calculate first alarm time
            val currentTime = Calendar.getInstance()
            val firstAlarmTime = getNextAlarmTime(currentTime, startMinutes, endMinutes, intervalMinutes)
            
            if (firstAlarmTime != null) {
                val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
                    putExtra("interval_minutes", intervalMinutes)
                    putExtra("start_minutes", startMinutes)
                    putExtra("end_minutes", endMinutes)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Use appropriate alarm method based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+ - check if exact alarms are allowed
                    if (alarmManager.canScheduleExactAlarms()) {
                        Log.d(TAG, "Android 12+: Using exact alarm (permission granted)")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            firstAlarmTime.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        Log.w(TAG, "Android 12+: Exact alarm permission not granted, using inexact alarm")
                        // Fallback to inexact alarm - still try setAndAllowWhileIdle for better reliability
                        try {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                firstAlarmTime.timeInMillis,
                                pendingIntent
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to set alarm: ${e.message}")
                            // Final fallback to basic set method
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                firstAlarmTime.timeInMillis,
                                pendingIntent
                            )
                        }
                    }
                } else {
                    Log.d(TAG, "Pre-Android 12: Using exact alarm")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        firstAlarmTime.timeInMillis,
                        pendingIntent
                    )
                }
                
                Log.d(TAG, "Hydration alarm scheduled for: ${firstAlarmTime.time}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling hydration alarms: ${e.message}")
        }
    }
    
    private fun getNextAlarmTime(currentTime: Calendar, startMinutes: Int, endMinutes: Int, intervalMinutes: Long): Calendar? {
        val alarmTime = Calendar.getInstance().apply {
            timeInMillis = currentTime.timeInMillis
        }
        
        // Convert start time to today's calendar
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startMinutes / 60)
            set(Calendar.MINUTE, startMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endMinutes / 60)
            set(Calendar.MINUTE, endMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If current time is before start time today, schedule for start time
        if (currentTime.before(todayStart)) {
            return todayStart
        }
        
        // If current time is after end time today, schedule for start time tomorrow
        if (currentTime.after(todayEnd)) {
            todayStart.add(Calendar.DAY_OF_MONTH, 1)
            return todayStart
        }
        
        // If we're within the window, schedule for next interval
        val nextAlarmTime = Calendar.getInstance().apply {
            timeInMillis = currentTime.timeInMillis + (intervalMinutes * 60 * 1000L)
        }
        
        // Make sure next alarm is still within today's window
        return if (nextAlarmTime.before(todayEnd) || nextAlarmTime.equals(todayEnd)) {
            nextAlarmTime
        } else {
            // Schedule for start time tomorrow
            todayStart.add(Calendar.DAY_OF_MONTH, 1)
            todayStart
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
                480 // Default to 8:00 AM
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time '$timeString': ${e.message}")
            480 // Default to 8:00 AM
        }
    }
    
    fun cancelHydrationAlarms() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HydrationAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            Log.d(TAG, "Hydration alarms cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling hydration alarms: ${e.message}")
        }
    }
}