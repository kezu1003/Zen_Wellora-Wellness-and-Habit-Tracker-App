package com.example.zen_wellora

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat

class NotificationPermissionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NotificationPermissionManager"
        const val REQUEST_CODE_POST_NOTIFICATIONS = 100
        const val REQUEST_CODE_EXACT_ALARM = 101
    }
    
    fun requestAllNotificationPermissions(activity: Activity) {
        Log.d(TAG, "Requesting all notification permissions...")
        
        // Step 1: Request POST_NOTIFICATIONS for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission")
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
                return // Wait for this permission first
            }
        }
        
        // Step 2: Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Requesting exact alarm permission")
                requestExactAlarmPermission(activity)
                return
            }
        }
        
        // Step 3: Check system notification settings
        checkSystemNotificationSettings(activity)
    }
    
    private fun requestExactAlarmPermission(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivityForResult(intent, REQUEST_CODE_EXACT_ALARM)
                Log.d(TAG, "Exact alarm permission requested")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting exact alarm permission: ${e.message}")
            // Fallback to general alarm settings
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening app settings: ${e2.message}")
            }
        }
    }
    
    private fun checkSystemNotificationSettings(activity: Activity) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.w(TAG, "System notifications are disabled")
            
            // Open notification settings
            try {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                }
                activity.startActivity(intent)
                Log.d(TAG, "Opened notification settings")
            } catch (e: Exception) {
                Log.e(TAG, "Error opening notification settings: ${e.message}")
            }
        } else {
            Log.d(TAG, "All notification permissions are granted!")
        }
    }
    
    fun checkAllPermissions(): PermissionStatus {
        val status = PermissionStatus()
        
        // Check system notifications
        val notificationManager = NotificationManagerCompat.from(context)
        status.systemNotificationsEnabled = notificationManager.areNotificationsEnabled()
        
        // Check POST_NOTIFICATIONS for Android 13+
        status.postNotificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required below Android 13
        }
        
        // Check exact alarm permission for Android 12+
        status.exactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Not required below Android 12
        }
        
        status.allPermissionsGranted = status.systemNotificationsEnabled && 
                                      status.postNotificationsPermission && 
                                      status.exactAlarmPermission
        
        Log.d(TAG, "Permission status: $status")
        return status
    }
    
    fun getPermissionStatusText(): String {
        val status = checkAllPermissions()
        val builder = StringBuilder()
        
        builder.append("=== NOTIFICATION PERMISSIONS ===\n")
        builder.append("System Notifications: ${if (status.systemNotificationsEnabled) "✅ Enabled" else "❌ Disabled"}\n")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.append("POST_NOTIFICATIONS: ${if (status.postNotificationsPermission) "✅ Granted" else "❌ Denied"}\n")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.append("Exact Alarms: ${if (status.exactAlarmPermission) "✅ Allowed" else "❌ Restricted"}\n")
        }
        
        builder.append("Overall Status: ${if (status.allPermissionsGranted) "✅ All Good" else "❌ Issues Found"}\n")
        
        if (!status.allPermissionsGranted) {
            builder.append("\nACTIONS NEEDED:\n")
            if (!status.systemNotificationsEnabled) {
                builder.append("• Enable notifications in Android Settings > Apps > Zen Wellora > Notifications\n")
            }
            if (!status.postNotificationsPermission) {
                builder.append("• Grant POST_NOTIFICATIONS permission when prompted\n")
            }
            if (!status.exactAlarmPermission) {
                builder.append("• Allow exact alarms in Android Settings > Apps > Zen Wellora > Permissions\n")
            }
        }
        
        return builder.toString()
    }
    
    data class PermissionStatus(
        var systemNotificationsEnabled: Boolean = false,
        var postNotificationsPermission: Boolean = false,
        var exactAlarmPermission: Boolean = false,
        var allPermissionsGranted: Boolean = false
    )
}