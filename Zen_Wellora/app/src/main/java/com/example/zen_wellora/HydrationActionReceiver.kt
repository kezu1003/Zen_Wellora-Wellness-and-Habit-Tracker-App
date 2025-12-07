package com.example.zen_wellora

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class HydrationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_WATER_COUNT = "water_consumed_today"
        private const val KEY_WATER_GOAL = "water_goal"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // IMMEDIATELY stop any alarm sound as the very first action
        Log.d("HydrationAction", "=== HYDRATION ACTION RECEIVED - STOPPING SOUND IMMEDIATELY ===")
        
        try {
            // NUCLEAR OPTION: Stop ALL sounds immediately
            Log.d("HydrationAction", "NUCLEAR SOUND STOP: Stopping all audio streams")
            
            // 1. Stop via AlarmSoundManager
            AlarmSoundManager.stopAlarmSound()
            Log.d("HydrationAction", "EMERGENCY STOP: Alarm sound stopped via singleton")
            
            // 2. System-level audio stop
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                // Stop all alarm streams
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, true)
                // Unmute immediately to allow future alarms
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, false)
                Log.d("HydrationAction", "System audio streams stopped")
            } catch (e: Exception) {
                Log.e("HydrationAction", "Error stopping system audio: ${e.message}")
            }
            
            // Immediate feedback toast to confirm sound stop was called
            Toast.makeText(context, "🔇 NUCLEAR STOP!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("HydrationAction", "CRITICAL: Failed to stop alarm sound: ${e.message}", e)
            Toast.makeText(context, "❌ Error stopping sound: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        
        when (intent.action) {
            "DRANK_WATER_ACTION" -> {
                Log.d("HydrationAction", "Processing 'I Drank Water' action")
                
                // Handle water consumption tracking
                trackWaterConsumption(context)
                Log.d("HydrationAction", "User drank water - tracked in preferences")

                // Show confirmation to user
                Toast.makeText(context, "🎉 Great job staying hydrated!", Toast.LENGTH_SHORT).show()

                // Cancel the current notification
                NotificationManagerCompat.from(context).cancel(NotificationHelper.HYDRATION_NOTIFICATION_ID)
                Log.d("HydrationAction", "Notification cancelled")
            }
            else -> {
                Log.w("HydrationAction", "Unknown action: ${intent.action}")
            }
        }
        
        // Final completion log
        Log.d("HydrationAction", "=== HYDRATION ACTION RECEIVER COMPLETED SUCCESSFULLY ===")
    }

    private fun trackWaterConsumption(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = sharedPreferences.getInt(KEY_WATER_COUNT, 0)
        val newCount = currentCount + 1

        sharedPreferences.edit()
            .putInt(KEY_WATER_COUNT, newCount)
            .apply()

        Log.d("HydrationAction", "Water count updated: $newCount")
    }
}