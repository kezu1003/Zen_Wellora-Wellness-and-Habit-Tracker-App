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

class Dialog_advanced_water : AppCompatActivity() {

    // UI Components
    private lateinit var etWaterMessage: EditText
    private lateinit var seekBarWaterDuration: SeekBar
    private lateinit var tvWaterDuration: TextView
    private lateinit var cbEnableSnooze: CheckBox
    private lateinit var spinnerSnoozeDuration: Spinner
    private lateinit var btnCancelAdvanced: Button
    private lateinit var btnSaveAdvanced: Button

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "AdvancedWaterSettings"
        private const val PREFS_NAME = "user_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_advanced_water)

        Log.d(TAG, "Advanced Water Settings Activity Created")

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
        loadAdvancedWaterSettings()
    }

    private fun initializeViews() {
        try {
            // Input fields
            etWaterMessage = findViewById(R.id.etWaterMessage)
            seekBarWaterDuration = findViewById(R.id.seekBarWaterDuration)
            tvWaterDuration = findViewById(R.id.tvWaterDuration)
            cbEnableSnooze = findViewById(R.id.cbEnableSnooze)
            spinnerSnoozeDuration = findViewById(R.id.spinnerSnoozeDuration)

            // Buttons
            btnCancelAdvanced = findViewById(R.id.btnCancelAdvanced)
            btnSaveAdvanced = findViewById(R.id.btnSaveAdvanced)

            Log.d(TAG, "All advanced water views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing advanced water views: ${e.message}")
            showToast("Error initializing advanced water settings")
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
            seekBarWaterDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvWaterDuration.text = "$progress seconds"
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
                Log.d(TAG, "Cancel advanced settings clicked")
                cancelAndReturn()
            }

            // Save button
            btnSaveAdvanced.setOnClickListener {
                Log.d(TAG, "Save advanced water settings clicked")
                saveAdvancedWaterSettings()
            }

            Log.d(TAG, "All advanced water click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up advanced water click listeners: ${e.message}")
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

            Log.d(TAG, "Advanced water spinners initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up advanced water spinners: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAdvancedWaterSettings() {
        try {
            // Load settings from SharedPreferences
            val waterMessage = sharedPreferences.getString("water_custom_message", "Time to drink water! Stay hydrated 💧")
                ?: "Time to drink water! Stay hydrated 💧"
            val waterDuration = sharedPreferences.getInt("water_notification_duration", 30)
            val snoozeEnabled = sharedPreferences.getBoolean("water_snooze_enabled", true)
            val snoozeDurationIndex = sharedPreferences.getInt("water_snooze_duration_index", 2) // 10 minutes default (now index 2)

            // Apply settings to UI
            etWaterMessage.setText(waterMessage)
            seekBarWaterDuration.progress = waterDuration
            tvWaterDuration.text = "$waterDuration seconds"
            cbEnableSnooze.isChecked = snoozeEnabled
            spinnerSnoozeDuration.setSelection(snoozeDurationIndex)
            spinnerSnoozeDuration.isEnabled = snoozeEnabled

            Log.d(TAG, "Advanced water settings loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading advanced water settings: ${e.message}")
            showToast("Error loading advanced water settings")
        }
    }

    private fun saveAdvancedWaterSettings() {
        try {
            val customMessage = etWaterMessage.text.toString().trim()
            val notificationDuration = seekBarWaterDuration.progress
            val snoozeEnabled = cbEnableSnooze.isChecked
            val snoozeDurationIndex = spinnerSnoozeDuration.selectedItemPosition

            // Validate inputs
            if (customMessage.isEmpty()) {
                etWaterMessage.error = "Please enter a custom message"
                etWaterMessage.requestFocus()
                return
            }

            // Save to SharedPreferences using KTX extension
            sharedPreferences.edit {
                putString("water_custom_message", customMessage)
                putInt("water_notification_duration", notificationDuration)
                putBoolean("water_snooze_enabled", snoozeEnabled)
                putInt("water_snooze_duration_index", snoozeDurationIndex)
            }

            showToast("Advanced water settings saved successfully!")
            Log.d(TAG, "Advanced water settings saved: Duration=$notificationDuration, Snooze=$snoozeEnabled")

            // Return to notification management
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving advanced water settings: ${e.message}")
            showToast("Error saving advanced water settings")
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
        Log.d(TAG, "Advanced Water Settings Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Advanced Water Settings Activity Destroyed")
    }
}