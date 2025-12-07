package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Dialog_advanced_habit : AppCompatActivity() {

    // UI Components
    private lateinit var etHabitMessage: EditText
    private lateinit var seekBarHabitDuration: SeekBar
    private lateinit var tvHabitDuration: TextView
    private lateinit var cbEnableSnooze: CheckBox
    private lateinit var spinnerSnoozeDuration: Spinner
    private lateinit var cbEnableStreakReminders: CheckBox
    private lateinit var cbEnableMotivationalMessages: CheckBox
    private lateinit var spinnerReminderFrequency: Spinner
    private lateinit var btnCancelAdvanced: Button
    private lateinit var btnSaveAdvanced: Button

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "AdvancedHabitSettings"
        private const val PREFS_NAME = "user_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_advanced_habit)

        Log.d(TAG, "Advanced Habit Settings Activity Created")

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
        setupSpinners()
        loadAdvancedHabitSettings()
    }

    private fun initializeViews() {
        try {
            // Input fields
            etHabitMessage = findViewById(R.id.etHabitMessage)
            seekBarHabitDuration = findViewById(R.id.seekBarHabitDuration)
            tvHabitDuration = findViewById(R.id.tvHabitDuration)
            cbEnableSnooze = findViewById(R.id.cbEnableSnooze)
            spinnerSnoozeDuration = findViewById(R.id.spinnerSnoozeDuration)
            cbEnableStreakReminders = findViewById(R.id.cbEnableStreakReminders)
            cbEnableMotivationalMessages = findViewById(R.id.cbEnableMotivationalMessages)
            spinnerReminderFrequency = findViewById(R.id.spinnerReminderFrequency)

            // Buttons
            btnCancelAdvanced = findViewById(R.id.btnCancelAdvanced)
            btnSaveAdvanced = findViewById(R.id.btnSaveAdvanced)

            Log.d(TAG, "All advanced habit views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing advanced habit views: ${e.message}")
            showToast("Error initializing advanced habit settings")
        }
    }

    private fun setupBackPressedHandler() {
        // Modern way to handle back button/gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelAndReturn()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        try {
            // SeekBar listener for duration
            seekBarHabitDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvHabitDuration.text = "$progress seconds"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Not needed
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Not needed
                }
            })

            // Snooze checkbox listener
            cbEnableSnooze.setOnCheckedChangeListener { _, isChecked ->
                spinnerSnoozeDuration.isEnabled = isChecked
            }

            // Cancel button
            btnCancelAdvanced.setOnClickListener {
                Log.d(TAG, "Cancel advanced habit settings clicked")
                cancelAndReturn()
            }

            // Save button
            btnSaveAdvanced.setOnClickListener {
                Log.d(TAG, "Save advanced habit settings clicked")
                saveAdvancedHabitSettings()
            }

            Log.d(TAG, "All advanced habit click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up advanced habit click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun setupSpinners() {
        try {
            // Snooze duration spinner
            val snoozeDurations = arrayOf("1 minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour")
            val snoozeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, snoozeDurations)
            snoozeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSnoozeDuration.adapter = snoozeAdapter

            // Reminder frequency spinner
            val reminderFrequencies = arrayOf("Once daily", "Twice daily", "Every 4 hours", "Every 6 hours", "Custom")
            val frequencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reminderFrequencies)
            frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerReminderFrequency.adapter = frequencyAdapter

            Log.d(TAG, "Advanced habit spinners initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up advanced habit spinners: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAdvancedHabitSettings() {
        try {
            // Load settings from SharedPreferences
            val habitMessage = sharedPreferences.getString("habit_custom_message", "Time to work on your habits! 🌱")
                ?: "Time to work on your habits! 🌱"
            val habitDuration = sharedPreferences.getInt("habit_notification_duration", 30)
            val snoozeEnabled = sharedPreferences.getBoolean("habit_snooze_enabled", true)
            val snoozeDurationIndex = sharedPreferences.getInt("habit_snooze_duration_index", 2) // 10 minutes default
            val streakRemindersEnabled = sharedPreferences.getBoolean("habit_streak_reminders_enabled", true)
            val motivationalMessagesEnabled = sharedPreferences.getBoolean("habit_motivational_messages_enabled", true)
            val reminderFrequencyIndex = sharedPreferences.getInt("habit_reminder_frequency_index", 0) // Once daily default

            // Apply settings to UI
            etHabitMessage.setText(habitMessage)
            seekBarHabitDuration.progress = habitDuration
            tvHabitDuration.text = "$habitDuration seconds"
            cbEnableSnooze.isChecked = snoozeEnabled
            spinnerSnoozeDuration.setSelection(snoozeDurationIndex)
            spinnerSnoozeDuration.isEnabled = snoozeEnabled
            cbEnableStreakReminders.isChecked = streakRemindersEnabled
            cbEnableMotivationalMessages.isChecked = motivationalMessagesEnabled
            spinnerReminderFrequency.setSelection(reminderFrequencyIndex)

            Log.d(TAG, "Advanced habit settings loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading advanced habit settings: ${e.message}")
            showToast("Error loading advanced habit settings")
        }
    }

    private fun saveAdvancedHabitSettings() {
        try {
            val customMessage = etHabitMessage.text.toString().trim()
            val notificationDuration = seekBarHabitDuration.progress
            val snoozeEnabled = cbEnableSnooze.isChecked
            val snoozeDurationIndex = spinnerSnoozeDuration.selectedItemPosition
            val streakRemindersEnabled = cbEnableStreakReminders.isChecked
            val motivationalMessagesEnabled = cbEnableMotivationalMessages.isChecked
            val reminderFrequencyIndex = spinnerReminderFrequency.selectedItemPosition

            // Validate inputs
            if (customMessage.isEmpty()) {
                etHabitMessage.error = "Please enter a custom message"
                etHabitMessage.requestFocus()
                return
            }

            // Save to SharedPreferences using KTX extension
            sharedPreferences.edit {
                putString("habit_custom_message", customMessage)
                putInt("habit_notification_duration", notificationDuration)
                putBoolean("habit_snooze_enabled", snoozeEnabled)
                putInt("habit_snooze_duration_index", snoozeDurationIndex)
                putBoolean("habit_streak_reminders_enabled", streakRemindersEnabled)
                putBoolean("habit_motivational_messages_enabled", motivationalMessagesEnabled)
                putInt("habit_reminder_frequency_index", reminderFrequencyIndex)
            }

            showToast("Advanced habit settings saved successfully!")
            Log.d(TAG, "Advanced habit settings saved: Duration=$notificationDuration, Snooze=$snoozeEnabled, Streak=$streakRemindersEnabled")

            // Return to notification management
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving advanced habit settings: ${e.message}")
            showToast("Error saving advanced habit settings")
        }
    }

    private fun cancelAndReturn() {
        setResult(RESULT_CANCELED)
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
        cancelAndReturn()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Advanced Habit Settings Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Advanced Habit Settings Activity Destroyed")
    }
}

