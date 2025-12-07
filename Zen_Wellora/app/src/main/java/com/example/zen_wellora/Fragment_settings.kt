package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Fragment_settings : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var switchHydrationReminder: Switch
    private lateinit var spinnerInterval: Spinner
    private lateinit var btnManageTimes: Button
    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var radioLight: RadioButton
    private lateinit var radioDark: RadioButton
    private lateinit var radioSystem: RadioButton
    private lateinit var cbVibration: CheckBox
    private lateinit var cbSound: CheckBox
    private lateinit var btnExportData: Button
    private lateinit var btnClearData: Button
    private lateinit var tvAppInfo: TextView
    private lateinit var btnSaveSettings: Button

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "SettingsActivity"
        private const val PREFS_NAME = "user_prefs"
        const val REQUEST_CODE_MANAGE_TIMES = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_settings)

        Log.d(TAG, "Settings Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        initializeViews()
        setupClickListeners()
        setupBackPressedHandler()
        setupThemeListeners()
        loadSettings()
        setupSpinners()
    }

    private fun initializeViews() {
        try {
            // Back arrow
            ivBack = findViewById(R.id.iv_back)
            
            // Hydration reminder section
            switchHydrationReminder = findViewById(R.id.switchHydrationReminder)
            spinnerInterval = findViewById(R.id.spinnerInterval)
            btnManageTimes = findViewById(R.id.btnManageTimes)

            // Theme selection
            radioGroupTheme = findViewById(R.id.radioGroupTheme)
            radioLight = findViewById(R.id.radioLight)
            radioDark = findViewById(R.id.radioDark)
            radioSystem = findViewById(R.id.radioSystem)

            // Notification preferences
            cbVibration = findViewById(R.id.cbVibration)
            cbSound = findViewById(R.id.cbSound)

            // Data management
            btnExportData = findViewById(R.id.btnExportData)
            btnClearData = findViewById(R.id.btnClearData)
            tvAppInfo = findViewById(R.id.tvAppInfo)

            // Save button
            btnSaveSettings = findViewById(R.id.btnSaveSettings)

            Log.d(TAG, "All settings views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing settings views: ${e.message}")
            showToast("Error initializing settings")
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
            // Back arrow
            ivBack.setOnClickListener {
                Log.d(TAG, "Back arrow clicked")
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Manage notification times button - UPDATED
            btnManageTimes.setOnClickListener {
                Log.d(TAG, "Manage notification times clicked")
                openManageNotificationTimes()
            }

            // Export data button
            btnExportData.setOnClickListener {
                Log.d(TAG, "Export data clicked")
                showToast("Data export feature coming soon!")
            }

            // Clear data button
            btnClearData.setOnClickListener {
                Log.d(TAG, "Clear data clicked")
                showClearDataConfirmationDialog()
            }

            // Save settings button
            btnSaveSettings.setOnClickListener {
                Log.d(TAG, "Save settings clicked")
                saveSettings()
            }

            Log.d(TAG, "All settings click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up settings click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun setupSpinners() {
        try {
            // Reminder interval spinner
            val intervals = arrayOf("30 minutes", "1 hour", "2 hours", "3 hours", "4 hours")
            val intervalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
            intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerInterval.adapter = intervalAdapter

            Log.d(TAG, "Settings spinners initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up settings spinners: ${e.message}")
        }
    }

    private fun setupThemeListeners() {
        try {
            // Add listeners to theme radio buttons for immediate theme changes
            radioLight.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    applyTheme("light")
                }
            }
            
            radioDark.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    applyTheme("dark")
                }
            }
            
            radioSystem.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    applyTheme("system")
                }
            }
            
            Log.d(TAG, "Theme listeners setup successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up theme listeners: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSettings() {
        try {
            // Load settings from SharedPreferences
            val hydrationEnabled = sharedPreferences.getBoolean("hydration_reminder_enabled", true)
            val intervalIndex = sharedPreferences.getInt("reminder_interval_index", 1) // 1 hour default
            val theme = sharedPreferences.getString("app_theme", "system") ?: "system"
            val vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true)
            val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)

            // Apply settings to UI
            switchHydrationReminder.isChecked = hydrationEnabled
            spinnerInterval.setSelection(intervalIndex)

            // Set theme selection
            when (theme) {
                "light" -> radioLight.isChecked = true
                "dark" -> radioDark.isChecked = true
                else -> radioSystem.isChecked = true
            }

            cbVibration.isChecked = vibrationEnabled
            cbSound.isChecked = soundEnabled

            // Set app info
            tvAppInfo.text = "App Version: 1.0.0"

            Log.d(TAG, "Settings loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading settings: ${e.message}")
            showToast("Error loading settings")
        }
    }

    private fun applyTheme(theme: String) {
        try {
            val nightMode = when (theme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            AppCompatDelegate.setDefaultNightMode(nightMode)
            
            // Save the theme preference immediately
            sharedPreferences.edit {
                putString("app_theme", theme)
            }
            
            Log.d(TAG, "Theme applied immediately: $theme (night mode: $nightMode)")
            showToast("Theme changed to ${theme.capitalize()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme: ${e.message}")
            showToast("Error applying theme")
        }
    }

    private fun saveSettings() {
        try {
            // Get current settings from UI
            val hydrationEnabled = switchHydrationReminder.isChecked
            val intervalIndex = spinnerInterval.selectedItemPosition
            val vibrationEnabled = cbVibration.isChecked
            val soundEnabled = cbSound.isChecked

            // Get selected theme
            val theme = when {
                radioLight.isChecked -> "light"
                radioDark.isChecked -> "dark"
                else -> "system"
            }

            // Save to SharedPreferences using KTX extension
            sharedPreferences.edit {
                putBoolean("hydration_reminder_enabled", hydrationEnabled)
                putInt("reminder_interval_index", intervalIndex)
                putString("app_theme", theme)
                putBoolean("vibration_enabled", vibrationEnabled)
                putBoolean("sound_enabled", soundEnabled)
            }

            showToast("Settings saved successfully!")
            Log.d(TAG, "Settings saved: Hydration=$hydrationEnabled, Theme=$theme")

            // Return to home after saving
            navigateBackToHome()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving settings: ${e.message}")
            showToast("Error saving settings")
        }
    }

    // NEW METHOD: Open Manage Notification Times Activity
    private fun openManageNotificationTimes() {
        try {
            val intent = Intent(this, Dialog_notification_times::class.java)
            startActivityForResult(intent, REQUEST_CODE_MANAGE_TIMES)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Opening Manage Notification Times activity")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Manage Notification Times: ${e.message}")
            showToast("Error opening notification times")
        }
    }

    // Handle result from Manage Notification Times if needed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_TIMES) {
            when (resultCode) {
                RESULT_OK -> {
                    showToast("Notification times updated")
                    Log.d(TAG, "Notification times updated successfully")
                }
                RESULT_CANCELED -> {
                    Log.d(TAG, "Notification times management cancelled")
                }
            }
        }
    }

    private fun showClearDataConfirmationDialog() {
        try {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Clear All Data")
            builder.setMessage("Are you sure you want to clear all app data? This action cannot be undone.")

            builder.setPositiveButton("Yes, Clear All") { _, _ ->
                clearAllData()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()

            // Customize button colors
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.error)
            )
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.primary)
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error showing clear data dialog: ${e.message}")
        }
    }

    private fun clearAllData() {
        try {
            // Clear all data from SharedPreferences using KTX extension
            sharedPreferences.edit {
                clear()
            }

            showToast("All data cleared successfully!")
            Log.d(TAG, "All app data cleared")

            // Return to home after clearing data
            navigateBackToHome()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data: ${e.message}")
            showToast("Error clearing data")
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
        Log.d(TAG, "Settings Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Settings Activity Destroyed")
    }
}