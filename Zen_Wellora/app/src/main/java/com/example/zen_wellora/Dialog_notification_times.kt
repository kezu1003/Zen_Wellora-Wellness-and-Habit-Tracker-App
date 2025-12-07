package com.example.zen_wellora

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Dialog_notification_times : AppCompatActivity() {

    // UI Components
    private lateinit var tvSelectedTime: TextView
    private lateinit var btnPickTime: Button
    private lateinit var btnAddTime: Button
    private lateinit var btnCloseTimes: Button

    companion object {
        private const val TAG = "NotificationTimesActivity"
        const val EXTRA_SELECTED_TIME = "selected_time"
    }

    // Activity result launcher for time picker
    private val timePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra(EXTRA_SELECTED_TIME)?.let { selectedTime ->
                // Update the selected time text view
                tvSelectedTime.text = selectedTime
                Log.d(TAG, "Time received from picker: $selectedTime")
                showToast("Time selected: $selectedTime")
            }
        } else if (result.resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Time picker cancelled by user")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_notification_times)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()

        Log.d(TAG, "Manage Notification Times Activity Created")
    }

    private fun initializeViews() {
        try {
            tvSelectedTime = findViewById(R.id.tvSelectedTime)
            btnPickTime = findViewById(R.id.btnPickTime)
            btnAddTime = findViewById(R.id.btnAddTime)
            btnCloseTimes = findViewById(R.id.btnCloseTimes)

            Log.d(TAG, "All notification times views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing notification times views: ${e.message}")
            showToast("Error initializing notification times")
        }
    }

    private fun setupClickListeners() {
        try {
            // Time picker button - UPDATED to open Time Picker Activity
            btnPickTime.setOnClickListener {
                openTimePickerActivity()
            }

            // Add time button
            btnAddTime.setOnClickListener {
                addSelectedTime()
            }

            // Close button
            btnCloseTimes.setOnClickListener {
                closeActivity()
            }

            Log.d(TAG, "All notification times click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up notification times click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    // NEW METHOD: Open Time Picker Activity
    private fun openTimePickerActivity() {
        try {
            val intent = Intent(this, Dialog_time_picker::class.java)
            timePickerLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Opening Time Picker activity")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Time Picker: ${e.message}")
            showToast("Error opening time picker")
        }
    }

    private fun addSelectedTime() {
        try {
            val timeToAdd = tvSelectedTime.text.toString().trim()

            if (timeToAdd.isEmpty() || timeToAdd == "08:00 AM") {
                showToast("Please select a time first")
                return
            }

            showToast("Time added: $timeToAdd")
            Log.d(TAG, "Time added: $timeToAdd")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding time: ${e.message}")
            showToast("Error adding time")
        }
    }

    private fun closeActivity() {
        setResult(RESULT_OK)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        closeActivity()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Manage Notification Times Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Manage Notification Times Activity Destroyed")
    }
}