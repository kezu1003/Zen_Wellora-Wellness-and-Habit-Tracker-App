package com.example.zen_wellora

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationTester(private val context: Context) {
    
    companion object {
        private const val TAG = "NotificationTester"
        private const val CHANNEL_ID = "test_channel"
        private const val TEST_NOTIFICATION_ID = 9999
    }
    
    fun runBasicNotificationTest(): String {
        val results = StringBuilder()
        
        try {
            results.append("=== BASIC NOTIFICATION TEST ===\n")
            
            // Step 1: Check if notifications are enabled at system level
            val notificationManager = NotificationManagerCompat.from(context)
            val systemEnabled = notificationManager.areNotificationsEnabled()
            results.append("1. System notifications enabled: $systemEnabled\n")
            
            if (!systemEnabled) {
                results.append("❌ PROBLEM: System notifications are disabled!\n")
                results.append("→ Go to Android Settings > Apps > Zen Wellora > Notifications\n")
                return results.toString()
            }
            
            // Step 2: Check POST_NOTIFICATIONS permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ActivityCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                results.append("2. POST_NOTIFICATIONS permission: $hasPermission\n")
                
                if (!hasPermission) {
                    results.append("❌ PROBLEM: POST_NOTIFICATIONS permission denied!\n")
                    results.append("→ App needs to request this permission\n")
                    return results.toString()
                }
            } else {
                results.append("2. POST_NOTIFICATIONS not required (Android < 13)\n")
            }
            
            // Step 3: Create basic notification channel
            results.append("3. Creating basic notification channel...\n")
            createBasicNotificationChannel()
            results.append("✓ Channel created\n")
            
            // Step 4: Create and send most basic notification possible
            results.append("4. Creating basic notification...\n")
            val success = sendBasicNotification()
            results.append("✓ Notification sent: $success\n")
            
            // Step 5: Verify notification was posted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val activeNotifications = systemNotificationManager.activeNotifications
                val found = activeNotifications.any { it.id == TEST_NOTIFICATION_ID }
                results.append("5. Notification verification: found=$found, total=${activeNotifications.size}\n")
                
                if (!found) {
                    results.append("❌ PROBLEM: Notification was sent but not found in active notifications!\n")
                } else {
                    results.append("✅ SUCCESS: Notification is active!\n")
                }
            } else {
                results.append("5. Cannot verify on this Android version\n")
            }
            
            results.append("=== TEST COMPLETE ===\n")
            
        } catch (e: Exception) {
            results.append("❌ ERROR: ${e.message}\n")
            Log.e(TAG, "Error in notification test", e)
        }
        
        return results.toString()
    }
    
    private fun createBasicNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Basic Test Channel",
                NotificationManager.IMPORTANCE_HIGH // Use high importance for testing
            ).apply {
                description = "Basic test notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Basic notification channel created")
        }
    }
    
    private fun sendBasicNotification(): Boolean {
        return try {
            val intent = Intent(context, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon
                .setContentTitle("🔔 BASIC TEST")
                .setContentText("This is the most basic notification possible!")
                .setPriority(NotificationCompat.PRIORITY_MAX) // Maximum priority
                .setDefaults(NotificationCompat.DEFAULT_ALL) // All defaults
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(TEST_NOTIFICATION_ID, notification)
            
            Log.d(TAG, "Basic notification sent with ID: $TEST_NOTIFICATION_ID")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send basic notification: ${e.message}", e)
            false
        }
    }
    
    fun clearTestNotification() {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(TEST_NOTIFICATION_ID)
            Log.d(TAG, "Test notification cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing test notification: ${e.message}")
        }
    }
}