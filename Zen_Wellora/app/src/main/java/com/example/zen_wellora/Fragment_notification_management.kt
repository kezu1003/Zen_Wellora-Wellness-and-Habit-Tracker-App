package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class Fragment_notification_management : AppCompatActivity() {

    // UI Components
    private lateinit var switchGlobalNotifications: SwitchMaterial
    private lateinit var tvGlobalStatus: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchWaterReminders: Switch
    private lateinit var layoutWaterSettings: LinearLayout
    private lateinit var spinnerWaterStyle: Spinner
    private lateinit var btnWaterAdvanced: Button
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchHabitReminders: Switch
    private lateinit var layoutHabitSettings: LinearLayout
    private lateinit var spinnerHabitType: Spinner
    private lateinit var btnHabitAdvanced: Button
    private lateinit var switchNotificationSound: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchNotificationVibration: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchNotificationLED: Switch
    private lateinit var spinnerNotificationPriority: Spinner
    private lateinit var btnTestNotification: Button
    private lateinit var btnSaveNotificationSettings: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var habitManager: HabitManager
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var notificationPermissionManager: NotificationPermissionManager

    companion object {
        private const val TAG = "NotificationManagement"
        private const val PREFS_NAME = "user_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_notification_management)

        Log.d(TAG, "Notification Management Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        habitManager = HabitManager(this)
        reminderScheduler = ReminderScheduler(this)
        notificationPermissionManager = NotificationPermissionManager(this)

        initializeViews()
        setupClickListeners()
        setupBackPressedHandler()
        loadNotificationSettings()
        setupSpinners()
    }

    private fun initializeViews() {
        try {
            // Global notifications
            switchGlobalNotifications = findViewById(R.id.switchGlobalNotifications)
            tvGlobalStatus = findViewById(R.id.tvGlobalStatus)

            // Water reminders
            switchWaterReminders = findViewById(R.id.switchWaterReminders)
            layoutWaterSettings = findViewById(R.id.layoutWaterSettings)
            spinnerWaterStyle = findViewById(R.id.spinnerWaterStyle)
            btnWaterAdvanced = findViewById(R.id.btnWaterAdvanced)

            // Habit reminders
            switchHabitReminders = findViewById(R.id.switchHabitReminders)
            layoutHabitSettings = findViewById(R.id.layoutHabitSettings)
            spinnerHabitType = findViewById(R.id.spinnerHabitType)
            btnHabitAdvanced = findViewById(R.id.btnHabitAdvanced)

            // Notification preferences
            switchNotificationSound = findViewById(R.id.switchNotificationSound)
            switchNotificationVibration = findViewById(R.id.switchNotificationVibration)
            switchNotificationLED = findViewById(R.id.switchNotificationLED)
            spinnerNotificationPriority = findViewById(R.id.spinnerNotificationPriority)

            // Buttons
            btnTestNotification = findViewById(R.id.btnTestNotification)
            btnSaveNotificationSettings = findViewById(R.id.btnSaveNotificationSettings)

            Log.d(TAG, "All notification views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing notification views: ${e.message}")
            showToast("Error initializing notification settings")
        }
    }

    private fun setupBackPressedHandler() {
        // Modern way to handle back button/gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToHome()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        try {
            // Global notifications toggle
            switchGlobalNotifications.setOnCheckedChangeListener { _, isChecked ->
                updateGlobalNotificationStatus(isChecked)
            }

            // Water reminders toggle
            switchWaterReminders.setOnCheckedChangeListener { _, isChecked ->
                layoutWaterSettings.visibility = if (isChecked) LinearLayout.VISIBLE else LinearLayout.GONE
            }

            // Habit reminders toggle
            switchHabitReminders.setOnCheckedChangeListener { _, isChecked ->
                layoutHabitSettings.visibility = if (isChecked) LinearLayout.VISIBLE else LinearLayout.GONE
            }

            // Advanced buttons
            btnWaterAdvanced.setOnClickListener {
                Log.d(TAG, "Water advanced settings clicked")
                navigateToAdvancedWaterSettings()
            }

            btnHabitAdvanced.setOnClickListener {
                Log.d(TAG, "Habit advanced settings clicked")
                navigateToAdvancedHabitSettings()
            }

            // Test notification button
            btnTestNotification.setOnClickListener {
                Log.d(TAG, "Test notification clicked")
                showTestNotification()
            }

            // Save settings button
            btnSaveNotificationSettings.setOnClickListener {
                Log.d(TAG, "Save notification settings clicked")
                saveNotificationSettings()
            }

            Log.d(TAG, "All notification click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up notification click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun navigateToAdvancedWaterSettings() {
        try {
            val intent = Intent(this, Dialog_advanced_water::class.java)

            // Pass current water settings if needed
            val waterRemindersEnabled = sharedPreferences.getBoolean("water_reminders_enabled", true)
            val waterStyleIndex = sharedPreferences.getInt("water_style_index", 0)

            intent.putExtra("water_reminders_enabled", waterRemindersEnabled)
            intent.putExtra("water_style_index", waterStyleIndex)

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Advanced Water Settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to advanced water settings: ${e.message}")
            showToast("Error opening advanced water settings")
        }
    }

    private fun navigateToAdvancedHabitSettings() {
        try {
            val intent = Intent(this, Dialog_advanced_habit::class.java)

            // Pass current habit settings if needed
            val habitRemindersEnabled = sharedPreferences.getBoolean("habit_reminders_enabled", true)
            val habitTypeIndex = sharedPreferences.getInt("habit_type_index", 0)

            intent.putExtra("habit_reminders_enabled", habitRemindersEnabled)
            intent.putExtra("habit_type_index", habitTypeIndex)

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Advanced Habit Settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to advanced habit settings: ${e.message}")
            showToast("Error opening advanced habit settings")
        }
    }

    private fun setupSpinners() {
        try {
            // Water notification style spinner
            val waterStyles = arrayOf("Gentle Reminder", "Standard Alert", "Urgent Notification")
            val waterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, waterStyles)
            waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerWaterStyle.adapter = waterAdapter

            // Habit reminder type spinner
            val habitTypes = arrayOf("Daily Reminders", "Weekly Summary", "Goal Completion")
            val habitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, habitTypes)
            habitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerHabitType.adapter = habitAdapter

            // Notification priority spinner
            val priorityLevels = arrayOf("Low", "Medium", "High", "Urgent")
            val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityLevels)
            priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerNotificationPriority.adapter = priorityAdapter

            Log.d(TAG, "All spinners initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up spinners: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadNotificationSettings() {
        try {
            // Load settings from SharedPreferences
            val globalEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
            val waterEnabled = sharedPreferences.getBoolean("water_reminders_enabled", true)
            val habitEnabled = sharedPreferences.getBoolean("habit_reminders_enabled", true)
            val soundEnabled = sharedPreferences.getBoolean("notification_sound", true)
            val vibrationEnabled = sharedPreferences.getBoolean("notification_vibration", true)
            val ledEnabled = sharedPreferences.getBoolean("notification_led", false)

            // Apply settings to UI
            switchGlobalNotifications.isChecked = globalEnabled
            updateGlobalNotificationStatus(globalEnabled)

            switchWaterReminders.isChecked = waterEnabled
            layoutWaterSettings.visibility = if (waterEnabled) LinearLayout.VISIBLE else LinearLayout.GONE

            switchHabitReminders.isChecked = habitEnabled
            layoutHabitSettings.visibility = if (habitEnabled) LinearLayout.VISIBLE else LinearLayout.GONE

            switchNotificationSound.isChecked = soundEnabled
            switchNotificationVibration.isChecked = vibrationEnabled
            switchNotificationLED.isChecked = ledEnabled

            // Set spinner selections
            val waterStyleIndex = sharedPreferences.getInt("water_style_index", 0)
            val habitTypeIndex = sharedPreferences.getInt("habit_type_index", 0)
            val priorityIndex = sharedPreferences.getInt("priority_index", 1)

            spinnerWaterStyle.setSelection(waterStyleIndex)
            spinnerHabitType.setSelection(habitTypeIndex)
            spinnerNotificationPriority.setSelection(priorityIndex)

            Log.d(TAG, "Notification settings loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notification settings: ${e.message}")
            showToast("Error loading notification settings")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateGlobalNotificationStatus(isEnabled: Boolean) {
        if (isEnabled) {
            tvGlobalStatus.text = "Enabled for all reminders"
            // Enable all other controls
            switchWaterReminders.isEnabled = true
            switchHabitReminders.isEnabled = true
            switchNotificationSound.isEnabled = true
            switchNotificationVibration.isEnabled = true
            switchNotificationLED.isEnabled = true
        } else {
            tvGlobalStatus.text = "All notifications disabled"
            // Disable all other controls
            switchWaterReminders.isEnabled = false
            switchHabitReminders.isEnabled = false
            switchNotificationSound.isEnabled = false
            switchNotificationVibration.isEnabled = false
            switchNotificationLED.isEnabled = false
        }
    }

    private fun saveNotificationSettings() {
        try {
            val globalEnabled = switchGlobalNotifications.isChecked
            val waterEnabled = switchWaterReminders.isChecked && globalEnabled
            val habitEnabled = switchHabitReminders.isChecked && globalEnabled
            
            // Save all settings to SharedPreferences using KTX extension
            sharedPreferences.edit {
                putBoolean("notifications_enabled", globalEnabled)
                putBoolean("water_reminders_enabled", waterEnabled)
                putBoolean("habit_reminders_enabled", habitEnabled)
                putBoolean("notification_sound", switchNotificationSound.isChecked)
                putBoolean("notification_vibration", switchNotificationVibration.isChecked)
                putBoolean("notification_led", switchNotificationLED.isChecked)
                putInt("water_style_index", spinnerWaterStyle.selectedItemPosition)
                putInt("habit_type_index", spinnerHabitType.selectedItemPosition)
                putInt("priority_index", spinnerNotificationPriority.selectedItemPosition)
            }
            
            // Recreate notification channels with new settings
            val notificationHelper = NotificationHelper(this)
            Log.d(TAG, "Notification channels recreated with updated preferences")
            
            // Apply water reminder settings
            if (waterEnabled) {
                // Get existing water reminder settings or use defaults
                val startTime = sharedPreferences.getString("hydration_start_time", "08:00 AM") ?: "08:00 AM"
                val endTime = sharedPreferences.getString("hydration_end_time", "08:00 PM") ?: "08:00 PM"
                val intervalMinutes = 120L // 2 hours default
                
                // Use both WorkManager and AlarmManager for more reliable scheduling
                val hydrationAlarmScheduler = HydrationAlarmScheduler(this)
                
                // Cancel existing reminders first
                reminderScheduler.cancelReminders()
                hydrationAlarmScheduler.cancelHydrationAlarms()
                
                // Schedule new reminders
                hydrationAlarmScheduler.scheduleHydrationAlarms(intervalMinutes, startTime, endTime)
                Log.d(TAG, "Water reminders enabled and scheduled with AlarmManager")
                
                // Test notification immediately to confirm it works
                notificationHelper.showHydrationReminder()
                Log.d(TAG, "Test hydration notification sent")
            } else {
                // Cancel all hydration reminders
                val hydrationAlarmScheduler = HydrationAlarmScheduler(this)
                reminderScheduler.cancelReminders()
                hydrationAlarmScheduler.cancelHydrationAlarms()
                Log.d(TAG, "Water reminders disabled and cancelled")
            }
            
            // Apply habit reminder settings
            if (habitEnabled) {
                // Set default habit reminder times if none exist
                val existingTimes = habitManager.getHabitReminderTimes()
                if (existingTimes.isEmpty()) {
                    val defaultTimes = listOf("09:00 AM", "06:00 PM")
                    habitManager.setHabitReminderTimes(defaultTimes)
                    Log.d(TAG, "Default habit reminder times set: $defaultTimes")
                } else {
                    habitManager.enableHabitReminders(true)
                    Log.d(TAG, "Habit reminders enabled with existing times: $existingTimes")
                }
            } else {
                habitManager.enableHabitReminders(false)
                Log.d(TAG, "Habit reminders disabled")
            }

            showToast("Notification settings saved successfully!")
            Log.d(TAG, "Notification settings saved and applied to schedulers")

            // Return to home after saving
            navigateBackToHome()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification settings: ${e.message}")
            showToast("Error saving notification settings")
        }
    }

    private fun runBasicNotificationTest() {
        try {
            Log.d(TAG, "=== RUNNING BASIC NOTIFICATION TEST ===")
            
            // First check permissions
            val permissionStatus = notificationPermissionManager.getPermissionStatusText()
            Log.d(TAG, permissionStatus)
            
            // If permissions are missing, request them
            val permissions = notificationPermissionManager.checkAllPermissions()
            if (!permissions.allPermissionsGranted) {
                Log.w(TAG, "Missing permissions - requesting them...")
                showToast("Requesting notification permissions...")
                notificationPermissionManager.requestAllNotificationPermissions(this)
                return
            }
            
            // Run basic notification test
            val tester = NotificationTester(this)
            val results = tester.runBasicNotificationTest()
            
            Log.d(TAG, "PERMISSION STATUS:\n$permissionStatus")
            Log.d(TAG, "TEST RESULTS:\n$results")
            
            // Show results to user
            showToast("Test completed - check notification panel and logs")
            
            // Show key results
            val keyResults = results.lines().filter { 
                it.contains("✅") || it.contains("❌") || it.contains("SUCCESS") || it.contains("PROBLEM")
            }.take(2).joinToString(" | ")
            
            if (keyResults.isNotEmpty()) {
                showToast(keyResults)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in basic notification test: ${e.message}")
            showToast("Test error: ${e.message}")
        }
    }
    
    private fun showTestNotification() {
        try {
            val notificationHelper = NotificationHelper(this)
            
            Log.d(TAG, "=== Test Notification Requested ===")
            
            // First run basic test to diagnose issues
            runBasicNotificationTest()
            
            // Then show regular notifications if enabled
            if (switchWaterReminders.isChecked) {
                Log.d(TAG, "Also testing water reminder notification...")
                notificationHelper.showHydrationReminder()
            }
            
            if (switchHabitReminders.isChecked) {
                Log.d(TAG, "Also testing habit reminder notification...")
                notificationHelper.showHabitReminder()
            }
            
            showToast("🔔 Test notifications sent! Check your notification panel and logs.")
            Log.d(TAG, "Test notifications completed - check notification panel")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending test notification: ${e.message}")
            showToast("Error sending test notification: ${e.message}")
        }
    }

    private fun navigateBackToHome() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    @Deprecated(
        "Deprecated in Android",
        ReplaceWith("onBackPressedDispatcher", "androidx.activity.OnBackPressedDispatcher")
    )
    override fun onBackPressed() {
        // This method is deprecated but we're marking it as deprecated too
        // The modern approach is handled by onBackPressedDispatcher above
        navigateBackToHome()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Notification Management Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Notification Management Activity Destroyed")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            NotificationPermissionManager.REQUEST_CODE_POST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "POST_NOTIFICATIONS permission granted")
                    showToast("✅ Notification permission granted!")
                    // Continue with permission requests if needed
                    notificationPermissionManager.requestAllNotificationPermissions(this)
                } else {
                    Log.w(TAG, "POST_NOTIFICATIONS permission denied")
                    showToast("❌ Notification permission denied - notifications will not work")
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            NotificationPermissionManager.REQUEST_CODE_EXACT_ALARM -> {
                Log.d(TAG, "Returned from exact alarm permission request")
                val permissions = notificationPermissionManager.checkAllPermissions()
                if (permissions.exactAlarmPermission) {
                    showToast("✅ Exact alarm permission granted!")
                } else {
                    showToast("❌ Exact alarm permission needed for reliable reminders")
                }
                // Continue with permission checks
                notificationPermissionManager.requestAllNotificationPermissions(this)
            }
        }
    }
}