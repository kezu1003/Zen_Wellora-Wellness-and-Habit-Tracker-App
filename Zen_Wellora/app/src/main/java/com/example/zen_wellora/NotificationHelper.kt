package com.example.zen_wellora

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        const val HYDRATION_CHANNEL_ID = "hydration_reminder_channel"
        const val HABIT_CHANNEL_ID = "habit_reminder_channel"
        const val HYDRATION_NOTIFICATION_ID = 1001
        const val HABIT_NOTIFICATION_ID = 1002
        private const val TAG = "NotificationHelper"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Get user preferences
            val soundEnabled = sharedPreferences.getBoolean("notification_sound", true)
            val vibrationEnabled = sharedPreferences.getBoolean("notification_vibration", true)
            val priorityIndex = sharedPreferences.getInt("priority_index", 1) // Medium default
            
            // Convert priority index to importance level
            val importance = when (priorityIndex) {
                0 -> NotificationManager.IMPORTANCE_LOW
                1 -> NotificationManager.IMPORTANCE_DEFAULT
                2 -> NotificationManager.IMPORTANCE_HIGH
                3 -> NotificationManager.IMPORTANCE_HIGH
                else -> NotificationManager.IMPORTANCE_DEFAULT
            }
            
            Log.d(TAG, "Creating notification channels with Sound=$soundEnabled, Vibration=$vibrationEnabled, Priority=$priorityIndex")
            
            // Hydration channel
            val hydrationChannel = NotificationChannel(
                HYDRATION_CHANNEL_ID, 
                "Hydration Reminders", 
                importance
            ).apply {
                description = "Reminders to drink water throughout the day"
                enableVibration(vibrationEnabled)
                setShowBadge(true)
                
                if (soundEnabled) {
                    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    Log.d(TAG, "Setting notification sound: $defaultSoundUri")
                    setSound(defaultSoundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build())
                } else {
                    Log.d(TAG, "Sound disabled - setting silent channel")
                    setSound(null, null)
                }
                
                if (vibrationEnabled) {
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
            }
            
            // Habit channel
            val habitChannel = NotificationChannel(
                HABIT_CHANNEL_ID, 
                "Habit Reminders", 
                importance
            ).apply {
                description = "Reminders to complete your daily habits"
                enableVibration(vibrationEnabled)
                setShowBadge(true)
                
                if (soundEnabled) {
                    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    Log.d(TAG, "Setting habit notification sound: $defaultSoundUri")
                    setSound(defaultSoundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build())
                } else {
                    Log.d(TAG, "Sound disabled - setting silent habit channel")
                    setSound(null, null)
                }
                
                if (vibrationEnabled) {
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
            }

            notificationManager.createNotificationChannel(hydrationChannel)
            notificationManager.createNotificationChannel(habitChannel)
            
            Log.d(TAG, "Notification channels created successfully")
        }
    }

    fun showHydrationReminder() {
        try {
            Log.d(TAG, "=== Starting hydration reminder ===")
            
            // Debug notification settings
            debugNotificationSettings()
            
            // Check if notifications are globally enabled
            val globalEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
            val waterEnabled = sharedPreferences.getBoolean("water_reminders_enabled", true)
            
            Log.d(TAG, "Settings check: Global=$globalEnabled, Water=$waterEnabled")
            
            if (!globalEnabled || !waterEnabled) {
                Log.w(TAG, "Hydration notifications disabled in settings")
                return
            }
            
            // Check notification permission
            if (!hasNotificationPermission()) {
                Log.w(TAG, "No notification permission - cannot show notification")
                return
            }
            
            // Get custom message from advanced settings
            val customMessage = sharedPreferences.getString("water_custom_message", 
                "Time to drink water! Stay hydrated 💧") ?: "Time to drink water! Stay hydrated 💧"
            
            // Get user preferences for notification behavior
            val soundEnabled = sharedPreferences.getBoolean("notification_sound", true)
            val vibrationEnabled = sharedPreferences.getBoolean("notification_vibration", true)
            val priorityIndex = sharedPreferences.getInt("priority_index", 1)
            
            // Convert priority index to notification priority
            val priority = when (priorityIndex) {
                0 -> NotificationCompat.PRIORITY_LOW
                1 -> NotificationCompat.PRIORITY_DEFAULT
                2 -> NotificationCompat.PRIORITY_HIGH
                3 -> NotificationCompat.PRIORITY_MAX
                else -> NotificationCompat.PRIORITY_DEFAULT
            }
            
            Log.d(TAG, "Notification config: Sound=$soundEnabled, Vibration=$vibrationEnabled, Priority=$priority, Message='$customMessage'")
            
            // Create intent to open the app when notification is tapped
            val intent = Intent(context, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("source", "hydration_notification")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // For Android 8+, recreate channel if sound settings have changed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && soundEnabled) {
                Log.d(TAG, "Ensuring notification channel has sound enabled")
                forceRecreateChannelsWithSound()
            }
            
            val notificationBuilder = NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("💧 Time to Hydrate!")
                .setContentText(customMessage)
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                // Add action button to mark as drank
                .addAction(
                    getNotificationIcon(),
                    "I Drank Water",
                    createDrankWaterPendingIntent()
                )
            
            // Apply sound and vibration settings based on Android version
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // For older Android versions, set defaults on notification
                var defaults = 0
                if (soundEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_SOUND
                    Log.d(TAG, "Pre-Android 8: Enabling notification sound")
                }
                if (vibrationEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
                }
                notificationBuilder.setDefaults(defaults)
            } else {
                // For Android 8+, sound is handled by the channel, but we can still set defaults as fallback
                if (soundEnabled && vibrationEnabled) {
                    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
                    Log.d(TAG, "Android 8+: Setting DEFAULT_ALL for notification")
                } else if (soundEnabled) {
                    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
                    Log.d(TAG, "Android 8+: Setting DEFAULT_SOUND for notification")
                } else if (vibrationEnabled) {
                    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                }
            }
            
            val notification = notificationBuilder.build()

            Log.d(TAG, "Attempting to show notification with ID: $HYDRATION_NOTIFICATION_ID")
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(HYDRATION_NOTIFICATION_ID, notification)
            Log.d(TAG, "Hydration notification posted successfully")
            
            // Play custom alarm sound regardless of notification channel settings
            if (soundEnabled) {
                Log.d(TAG, "Playing custom alarm sound for hydration reminder via singleton")
                AlarmSoundManager.playAlarmSound(context)
            } else {
                Log.d(TAG, "Sound disabled - not playing alarm")
            }
            
            // Verify notification was actually posted
            verifyNotificationPosted()

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException showing hydration notification: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing hydration notification: ${e.message}", e)
        }
    }

    private fun getNotificationIcon(): Int {
        return try {
            // Try to use custom water drop icon
            context.resources.getDrawable(R.drawable.ic_water_drop, null)
            R.drawable.ic_water_drop
        } catch (e: Exception) {
            // Fallback to system icon
            android.R.drawable.ic_dialog_info
        }
    }

    private fun createDrankWaterPendingIntent(): PendingIntent {
        val drankIntent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = "DRANK_WATER_ACTION"
        }
        return PendingIntent.getBroadcast(
            context,
            1,
            drankIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showHabitReminder() {
        try {
            // Create intent to open the habit tracker when notification is tapped
            val intent = Intent(context, DailyHabitTracker::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("source", "habit_notification")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, HABIT_CHANNEL_ID)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("✅ Time to Build Habits!")
                .setContentText("Don't forget to complete your daily habits today 🌟")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                // Add action button to open habit tracker
                .addAction(
                    getHabitIcon(),
                    "View Habits",
                    pendingIntent
                )
                .build()

            if (hasNotificationPermission()) {
                NotificationManagerCompat.from(context).notify(HABIT_NOTIFICATION_ID, notification)
                Log.d("NotificationHelper", "Habit reminder notification shown successfully")
            } else {
                Log.w("NotificationHelper", "Notification permission not granted")
            }

        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "SecurityException: ${e.message}")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error showing habit notification: ${e.message}")
        }
    }
    
    private fun getHabitIcon(): Int {
        return try {
            // Try to use custom habits icon
            context.resources.getDrawable(R.drawable.ic_habits, null)
            R.drawable.ic_habits
        } catch (e: Exception) {
            // Fallback to check circle icon
            try {
                context.resources.getDrawable(R.drawable.ic_check_circle, null)
                R.drawable.ic_check_circle
            } catch (e2: Exception) {
                android.R.drawable.ic_dialog_info
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check both system notification settings and our specific permission
            val systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            val permissionGranted = try {
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                Log.w("NotificationHelper", "Error checking POST_NOTIFICATIONS permission: ${e.message}")
                false
            }
            systemEnabled && permissionGranted
        } else {
            // For Android 12 and below, just check if notifications are enabled system-wide
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    private fun debugNotificationSettings() {
        try {
            Log.d(TAG, "=== Notification Debug Info ===")
            
            val notificationManager = NotificationManagerCompat.from(context)
            val systemEnabled = notificationManager.areNotificationsEnabled()
            Log.d(TAG, "System notifications enabled: $systemEnabled")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionGranted = context.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                Log.d(TAG, "POST_NOTIFICATIONS permission: $permissionGranted")
            }
            
            // Check device audio settings
            debugDeviceAudioSettings()
            
            // Check notification channel status (Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val hydrationChannel = systemNotificationManager.getNotificationChannel(HYDRATION_CHANNEL_ID)
                if (hydrationChannel != null) {
                    Log.d(TAG, "Hydration channel - Importance: ${hydrationChannel.importance}, Sound: ${hydrationChannel.sound}, Vibration: ${hydrationChannel.shouldVibrate()}")
                } else {
                    Log.w(TAG, "Hydration notification channel not found!")
                }
            }
            
            // Check user preferences
            val globalEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
            val waterEnabled = sharedPreferences.getBoolean("water_reminders_enabled", true)
            val soundEnabled = sharedPreferences.getBoolean("notification_sound", true)
            val vibrationEnabled = sharedPreferences.getBoolean("notification_vibration", true)
            
            Log.d(TAG, "User settings - Global: $globalEnabled, Water: $waterEnabled, Sound: $soundEnabled, Vibration: $vibrationEnabled")
            Log.d(TAG, "=== End Debug Info ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error in debug notification settings: ${e.message}")
        }
    }
    
    private fun debugDeviceAudioSettings() {
        try {
            Log.d(TAG, "=== Device Audio Debug ===")
            
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Check ringer mode
            val ringerMode = audioManager.ringerMode
            val ringerModeString = when (ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> "SILENT"
                AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
                AudioManager.RINGER_MODE_NORMAL -> "NORMAL"
                else -> "UNKNOWN ($ringerMode)"
            }
            Log.d(TAG, "Ringer mode: $ringerModeString")
            
            // Check notification volume
            val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            Log.d(TAG, "Notification volume: $notificationVolume/$maxNotificationVolume")
            
            // Check system volume
            val systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
            val maxSystemVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
            Log.d(TAG, "System volume: $systemVolume/$maxSystemVolume")
            
            // Check Do Not Disturb (Android 6+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val filterLevel = notificationManager.currentInterruptionFilter
                    val dndStatus = when (filterLevel) {
                        NotificationManager.INTERRUPTION_FILTER_NONE -> "ALL BLOCKED"
                        NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "PRIORITY ONLY"
                        NotificationManager.INTERRUPTION_FILTER_ALARMS -> "ALARMS ONLY"
                        NotificationManager.INTERRUPTION_FILTER_ALL -> "ALL ALLOWED"
                        else -> "UNKNOWN ($filterLevel)"
                    }
                    Log.d(TAG, "Do Not Disturb: $dndStatus")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not check Do Not Disturb status: ${e.message}")
                }
            }
            
            Log.d(TAG, "=== End Audio Debug ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error debugging device audio: ${e.message}")
        }
    }
    
    private fun verifyNotificationPosted() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val activeNotifications = notificationManager.activeNotifications
                val found = activeNotifications.any { it.id == HYDRATION_NOTIFICATION_ID }
                Log.d(TAG, "Verification: Notification found in active notifications: $found")
                Log.d(TAG, "Total active notifications: ${activeNotifications.size}")
            } else {
                Log.d(TAG, "Cannot verify active notifications on this Android version")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying notification posted: ${e.message}")
        }
    }
    
    fun forceRecreateChannelsWithSound() {
        try {
            Log.d(TAG, "Force recreating notification channels with sound enabled")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // Delete existing channels to force recreation
                try {
                    notificationManager.deleteNotificationChannel(HYDRATION_CHANNEL_ID)
                    notificationManager.deleteNotificationChannel(HABIT_CHANNEL_ID)
                    Log.d(TAG, "Deleted existing notification channels")
                } catch (e: Exception) {
                    Log.w(TAG, "Error deleting existing channels: ${e.message}")
                }
                
                // Force create new channels with sound
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                Log.d(TAG, "Using sound URI: $defaultSoundUri")
                
                val hydrationChannel = NotificationChannel(
                    HYDRATION_CHANNEL_ID,
                    "Hydration Reminders",
                    NotificationManager.IMPORTANCE_HIGH // Use HIGH for sound
                ).apply {
                    description = "Reminders to drink water throughout the day"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    setShowBadge(true)
                    setSound(defaultSoundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build())
                }
                
                val habitChannel = NotificationChannel(
                    HABIT_CHANNEL_ID,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_HIGH // Use HIGH for sound
                ).apply {
                    description = "Reminders to complete your daily habits"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    setShowBadge(true)
                    setSound(defaultSoundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build())
                }
                
                notificationManager.createNotificationChannel(hydrationChannel)
                notificationManager.createNotificationChannel(habitChannel)
                
                Log.d(TAG, "Created new channels with HIGH importance and sound")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error force recreating channels: ${e.message}", e)
        }
    }
    
    fun stopNotificationAlarm() {
        try {
            Log.d(TAG, "Stopping notification alarm sound via singleton")
            AlarmSoundManager.stopAlarmSound()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping notification alarm: ${e.message}")
        }
    }
    
    fun showTestNotification() {
        try {
            Log.d(TAG, "=== Showing FORCED test notification with sound ===")
            
            // First, force recreate channels with sound
            forceRecreateChannelsWithSound()
            
            // Force show notification regardless of settings for testing
            val intent = Intent(context, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("source", "test_notification")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🔔 SOUND TEST")
                .setContentText("Testing notification with sound!")
                .setPriority(NotificationCompat.PRIORITY_MAX) // Maximum priority
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Force all defaults
                .build()

            Log.d(TAG, "Posting test notification with forced sound...")
            NotificationManagerCompat.from(context).notify(HYDRATION_NOTIFICATION_ID + 1000, notification)
            Log.d(TAG, "Test notification posted with ID: ${HYDRATION_NOTIFICATION_ID + 1000}")
            
            // Always play custom alarm sound for test notifications
            Log.d(TAG, "Playing test alarm sound via singleton")
            AlarmSoundManager.playAlarmSound(context)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing test notification: ${e.message}", e)
        }
    }
}
