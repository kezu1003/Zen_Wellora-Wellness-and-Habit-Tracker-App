package com.example.zen_wellora

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "NotificationPermissionHelper"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        const val BATTERY_OPTIMIZATION_REQUEST_CODE = 1002
    }
    
    fun checkAndRequestNotificationPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                false
            } else {
                true
            }
        } else {
            true // Permission not required below Android 13
        }
    }
    
    fun checkBatteryOptimization(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            powerManager?.let { !it.isIgnoringBatteryOptimizations(context.packageName) } ?: false
        } else {
            false
        }
    }
    
    fun requestBatteryOptimizationExemption(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE)
                Log.d(TAG, "Battery optimization exemption requested")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting battery optimization exemption: ${e.message}")
            // Fallback to general battery optimization settings
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                activity.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening battery optimization settings: ${e2.message}")
            }
        }
    }
    
    fun showNotificationSettings(activity: Activity) {
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
            Log.d(TAG, "Notification settings opened")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening notification settings: ${e.message}")
        }
    }
    
    fun getPermissionStatusMessage(): String {
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        val isBatteryOptimized = checkBatteryOptimization()
        
        return when {
            !hasNotificationPermission && isBatteryOptimized -> 
                "⚠️ Notifications disabled and battery optimization enabled. Reminders may not work properly."
            !hasNotificationPermission -> 
                "⚠️ Notification permission denied. Please enable in settings."
            isBatteryOptimized -> 
                "⚠️ Battery optimization enabled. Reminders may be delayed or missed."
            else -> 
                "✅ All permissions granted. Reminders should work properly."
        }
    }
}