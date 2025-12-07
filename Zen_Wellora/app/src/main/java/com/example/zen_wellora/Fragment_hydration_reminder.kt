package com.example.zen_wellora

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Calendar

class Fragment_hydration_reminder : AppCompatActivity() {

    // UI Components
    private lateinit var switchReminderEnabled: SwitchMaterial
    private lateinit var tvReminderStatus: TextView
    private lateinit var btnStartTime: Button
    private lateinit var btnEndTime: Button
    private lateinit var tvScheduleSummary: TextView
    private lateinit var seekBarWaterGoal: SeekBar
    private lateinit var tvWaterGoal: TextView
    private lateinit var btnSaveReminder: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var hydrationAlarmScheduler: HydrationAlarmScheduler
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper

    companion object {
        private const val TAG = "HydrationReminder"
        private const val PREFS_NAME = "user_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_hydration_reminder)

        Log.d(TAG, "Hydration Reminder Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences and new components
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        reminderScheduler = ReminderScheduler(this)
        hydrationAlarmScheduler = HydrationAlarmScheduler(this)
        notificationHelper = NotificationHelper(this)
        notificationPermissionHelper = NotificationPermissionHelper(this)

        initializeViews()
        setupClickListeners()
        loadHydrationSettings()
    }

    private fun initializeViews() {
        try {
            // Main toggle switch
            switchReminderEnabled = findViewById(R.id.switchReminderEnabled)
            tvReminderStatus = findViewById(R.id.tvReminderStatus)

            // Time buttons
            btnStartTime = findViewById(R.id.btnStartTime)
            btnEndTime = findViewById(R.id.btnEndTime)
            tvScheduleSummary = findViewById(R.id.tvScheduleSummary)

            // Water goal controls
            seekBarWaterGoal = findViewById(R.id.seekBarWaterGoal)
            tvWaterGoal = findViewById(R.id.tvWaterGoal)
            
            // Save button
            btnSaveReminder = findViewById(R.id.btnSaveReminder)

            Log.d(TAG, "All hydration views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing hydration views: ${e.message}")
            showToast("Error initializing hydration page")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        try {
            // Toggle switch listener
            switchReminderEnabled.setOnCheckedChangeListener { _, isChecked ->
                updateReminderStatus(isChecked)
            }

            // Start time button
            btnStartTime.setOnClickListener {
                showTimePicker(true) // true for start time
            }

            // End time button
            btnEndTime.setOnClickListener {
                showTimePicker(false) // false for end time
            }

            // Water goal seekbar
            seekBarWaterGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateWaterGoalText(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Not needed
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Not needed
                }
            })

            // Save button
            btnSaveReminder.setOnClickListener {
                saveHydrationSettings()
            }

            Log.d(TAG, "All hydration click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up hydration click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute, isStartTime)
            },
            currentHour,
            currentMinute,
            false // 24-hour format set to false for AM/PM
        )

        // Set appropriate title based on whether it's start or end time
        if (isStartTime) {
            timePickerDialog.setTitle("Select Start Time")
        } else {
            timePickerDialog.setTitle("Select End Time")
        }

        timePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun onTimeSelected(hour: Int, minute: Int, isStartTime: Boolean) {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            // Format time with better readability
            val timeText = android.text.format.DateFormat.format("h:mm a", calendar).toString()

            if (isStartTime) {
                btnStartTime.text = timeText
                Log.d(TAG, "Start time selected: $timeText")
            } else {
                btnEndTime.text = timeText
                Log.d(TAG, "End time selected: $timeText")
            }

            // Update summary immediately with new times
            updateScheduleSummary(
                btnStartTime.text.toString(),
                btnEndTime.text.toString(),
                seekBarWaterGoal.progress + 4
            )

            showToast("${if (isStartTime) "Start" else "End"} time set to $timeText")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing time selection: ${e.message}")
            showToast("Error setting time")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateReminderStatus(isEnabled: Boolean) {
        try {
            if (isEnabled) {
                tvReminderStatus.text = "Currently active"
                tvReminderStatus.setTextColor(getColor(R.color.success))
            } else {
                tvReminderStatus.text = "Currently inactive"
                tvReminderStatus.setTextColor(getColor(R.color.text_secondary))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reminder status: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWaterGoalText(progress: Int) {
        try {
            val glasses = progress + 4 // Starting from 4 glasses (progress 0 = 4 glasses)
            tvWaterGoal.text = "$glasses glasses"

            // Update summary with new water goal
            updateScheduleSummary(
                btnStartTime.text.toString(),
                btnEndTime.text.toString(),


                glasses
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating water goal text: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadHydrationSettings() {
        try {
            // Load saved hydration settings or use defaults
            val isReminderEnabled = sharedPreferences.getBoolean("hydration_reminders_enabled", false)
            val waterGoal = sharedPreferences.getInt("water_goal", 8)
            val startTime = sharedPreferences.getString("hydration_start_time", "08:00 AM") ?: "08:00 AM"
            val endTime = sharedPreferences.getString("hydration_end_time", "08:00 PM") ?: "08:00 PM"

            // Apply settings to UI
            switchReminderEnabled.isChecked = isReminderEnabled
            updateReminderStatus(isReminderEnabled)

            // Set water goal (convert from actual glasses to seekbar progress)
            val seekbarProgress = (waterGoal - 4).coerceIn(0, 12)
            seekBarWaterGoal.progress = seekbarProgress
            updateWaterGoalText(seekbarProgress)

            // Set time buttons with better formatting
            btnStartTime.text = formatTimeForDisplay(startTime)
            btnEndTime.text = formatTimeForDisplay(endTime)

            // Update schedule summary
            updateScheduleSummary(startTime, endTime, waterGoal)

            Log.d(TAG, "Hydration settings loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading hydration settings: ${e.message}")
            showToast("Error loading settings")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateScheduleSummary(startTime: String, endTime: String, waterGoal: Int) {
        try {
            val summary = "Reminders: Every 2 hours from $startTime to $endTime | Goal: $waterGoal glasses"
            tvScheduleSummary.text = summary

            // Show the summary when settings are configured
            tvScheduleSummary.visibility = TextView.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error updating schedule summary: ${e.message}")
        }
    }

    private fun saveHydrationSettings() {
        try {
            // Get current values from UI
            val isReminderEnabled = switchReminderEnabled.isChecked
            val waterGoal = seekBarWaterGoal.progress + 4 // Convert seekbar progress to actual glasses
            val startTime = btnStartTime.text.toString()
            val endTime = btnEndTime.text.toString()

            // Save to SharedPreferences
            sharedPreferences.edit().apply {
                putBoolean("hydration_reminders_enabled", isReminderEnabled)
                putInt("water_goal", waterGoal)
                putString("hydration_start_time", startTime)
                putString("hydration_end_time", endTime)
                apply()
            }

            // Schedule or cancel reminders based on toggle
            if (isReminderEnabled) {
                // Check notification permissions first
                if (!notificationPermissionHelper.checkAndRequestNotificationPermission(this)) {
                    Log.w(TAG, "Notification permission not granted, requesting permission")
                    showToast("Please grant notification permission to enable reminders")
                    // Switch will be updated when permission is granted
                    return
                }
                
                // Use AlarmManager for more reliable scheduling
                val intervalMinutes = 120L // 2 hours - you can make this dynamic based on user selection
                
                // Cancel any existing reminders first
                reminderScheduler.cancelReminders()
                hydrationAlarmScheduler.cancelHydrationAlarms()
                
                // Schedule new reminders using AlarmManager
                hydrationAlarmScheduler.scheduleHydrationAlarms(intervalMinutes, startTime, endTime)
                showToast("Hydration reminders scheduled!")
                
                Log.d(TAG, "Hydration alarms scheduled with AlarmManager")

                // Test notification immediately
                notificationHelper.showHydrationReminder()
                Log.d(TAG, "Test notification sent immediately")
            } else {
                // Cancel both WorkManager and AlarmManager reminders
                reminderScheduler.cancelReminders()
                hydrationAlarmScheduler.cancelHydrationAlarms()
                showToast("Hydration reminders cancelled")
            }

            // Update UI
            updateScheduleSummary(startTime, endTime, waterGoal)
            showToast("Hydration settings saved successfully!")
            
            // Debug scheduling info
            debugHydrationScheduling()

            Log.d(TAG, "Hydration settings saved: Enabled=$isReminderEnabled, Goal=$waterGoal glasses, Start=$startTime, End=$endTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving hydration settings: ${e.message}")
            showToast("Error saving settings")
        }
    }

    private fun formatTimeForDisplay(timeString: String): String {
        return try {
            // If time is already in good format, return as is
            if (timeString.matches(Regex("\\d{1,2}:\\d{2}\\s*[AaPp][Mm]"))) {
                timeString
            } else {
                // Try to parse and reformat if needed
                val parts = timeString.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 8
                    val minute = parts[1].split(" ")[0].toIntOrNull() ?: 0
                    val ampm = if (parts[1].contains("PM", ignoreCase = true)) "PM" else "AM"
                    String.format("%d:%02d %s", hour, minute, ampm)
                } else {
                    timeString
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: ${e.message}")
            timeString
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun debugHydrationScheduling() {
        try {
            Log.d(TAG, "=== DEBUG: Hydration Scheduling Status ===")
            
            // Check notification permission
            val hasNotificationPermission = notificationPermissionHelper.checkAndRequestNotificationPermission(this)
            Log.d(TAG, "Notification permission: $hasNotificationPermission")
            
            // Check battery optimization
            val isBatteryOptimized = notificationPermissionHelper.checkBatteryOptimization()
            Log.d(TAG, "Battery optimized: $isBatteryOptimized")
            
            // Check current settings
            val isEnabled = switchReminderEnabled.isChecked
            val startTime = btnStartTime.text.toString()
            val endTime = btnEndTime.text.toString()
            val waterGoal = seekBarWaterGoal.progress + 4
            
            Log.d(TAG, "Settings: Enabled=$isEnabled, Start=$startTime, End=$endTime, Goal=$waterGoal")
            
            // Show permission status message
            val statusMessage = notificationPermissionHelper.getPermissionStatusMessage()
            Log.d(TAG, "Permission Status: $statusMessage")
            
            // Test notification immediately
            if (hasNotificationPermission) {
                Log.d(TAG, "Sending test notification...")
                notificationHelper.showHydrationReminder()
                showToast("Test notification sent! Check your notification panel.")
            } else {
                showToast("Cannot send test notification - permission required")
            }
            
            Log.d(TAG, "=== END DEBUG INFO ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error in debug scheduling: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Hydration Reminder Activity Resumed")
        // Reload settings when returning to the activity
        loadHydrationSettings()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Hydration Reminder Activity Started")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Hydration Reminder Activity Paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Hydration Reminder Activity Destroyed")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Notification permission granted")
                    showToast("Notification permission granted! You can now enable hydration reminders.")
                    // Reload settings to reflect the change
                    loadHydrationSettings()
                } else {
                    Log.w(TAG, "Notification permission denied")
                    showToast("Notification permission denied. Reminders will not work without permission.")
                    // Turn off the switch since permission was denied
                    switchReminderEnabled.isChecked = false
                    updateReminderStatus(false)
                }
            }
        }
    }
}