package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Dialog_time_picker : AppCompatActivity() {

    // UI Components
    private lateinit var timePicker: TimePicker
    private lateinit var btnCancelTime: Button
    private lateinit var btnConfirmTime: Button

    companion object {
        private const val TAG = "TimePickerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_time_picker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()

        Log.d(TAG, "Time Picker Activity Created")
    }

    private fun initializeViews() {
        try {
            timePicker = findViewById(R.id.timePicker)
            btnCancelTime = findViewById(R.id.btnCancelTime)
            btnConfirmTime = findViewById(R.id.btnConfirmTime)

            // Set 12-hour format
            timePicker.setIs24HourView(false)

            Log.d(TAG, "All time picker views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing time picker views: ${e.message}")
            showToast("Error initializing time picker")
        }
    }

    private fun setupClickListeners() {
        try {
            // Cancel button
            btnCancelTime.setOnClickListener {
                cancelTimeSelection()
            }

            // Confirm button
            btnConfirmTime.setOnClickListener {
                confirmTimeSelection()
            }

            Log.d(TAG, "All time picker click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up time picker click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun confirmTimeSelection() {
        try {
            val hour: Int
            val minute: Int

            // Get time based on Android version
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.hour
                minute = timePicker.minute
            } else {
                hour = timePicker.currentHour
                minute = timePicker.currentMinute
            }

            val selectedTime = formatTime(hour, minute)

            Log.d(TAG, "Time selected - Hour: $hour, Minute: $minute, Formatted: $selectedTime")

            // Return the selected time to calling activity
            val resultIntent = Intent().apply {
                putExtra(Dialog_notification_times.EXTRA_SELECTED_TIME, selectedTime)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        } catch (e: Exception) {
            Log.e(TAG, "Error confirming time selection: ${e.message}")
            showToast("Error selecting time")
        }
    }

    private fun cancelTimeSelection() {
        try {
            setResult(RESULT_CANCELED)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Time selection cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling time selection: ${e.message}")
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return timeFormat.format(calendar.time)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: ${e.message}")
            // Fallback format
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
            return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        cancelTimeSelection()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Time Picker Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Time Picker Activity Destroyed")
    }
}