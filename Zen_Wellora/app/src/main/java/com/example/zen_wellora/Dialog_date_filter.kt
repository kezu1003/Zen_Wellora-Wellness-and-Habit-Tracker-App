package com.example.zen_wellora

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Dialog_date_filter : AppCompatActivity() {

    // UI Components
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnApplyFilter: Button
    private lateinit var btnClearFilter: Button

    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()

    companion object {
        private const val TAG = "DateFilterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_date_filter)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        resetDates()
        updateDateButtons()

        Log.d(TAG, "Date Filter Activity Created")
    }

    private fun initializeViews() {
        try {
            // Date selection buttons
            btnStartDate = findViewById(R.id.btnStartDate)
            btnEndDate = findViewById(R.id.btnEndDate)

            // Action buttons
            btnApplyFilter = findViewById(R.id.btnApplyFilter)
            btnClearFilter = findViewById(R.id.btnClearFilter)

            Log.d(TAG, "All date filter views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing date filter views: ${e.message}")
            showToast("Error initializing date filter")
        }
    }

    private fun setupClickListeners() {
        try {
            // Start date selection button
            btnStartDate.setOnClickListener {
                showStartDatePicker()
            }

            // End date selection button
            btnEndDate.setOnClickListener {
                showEndDatePicker()
            }

            // Apply filter button
            btnApplyFilter.setOnClickListener {
                applyFilter()
            }

            // Clear filter button
            btnClearFilter.setOnClickListener {
                clearFilter()
            }

            Log.d(TAG, "All date filter click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up date filter click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun showStartDatePicker() {
        try {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    startDate.set(year, month, dayOfMonth)
                    updateDateButtons()
                    Log.d(TAG, "Start date selected: $year-${month + 1}-$dayOfMonth")
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing start date picker: ${e.message}")
            showToast("Error selecting start date")
        }
    }

    private fun showEndDatePicker() {
        try {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    endDate.set(year, month, dayOfMonth)
                    updateDateButtons()
                    Log.d(TAG, "End date selected: $year-${month + 1}-$dayOfMonth")
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing end date picker: ${e.message}")
            showToast("Error selecting end date")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateButtons() {
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            btnStartDate.text = dateFormat.format(startDate.time)
            btnEndDate.text = dateFormat.format(endDate.time)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating date buttons: ${e.message}")
        }
    }

    private fun applyFilter() {
        try {
            // Validate date range
            if (startDate.timeInMillis > endDate.timeInMillis) {
                showToast("Start date cannot be after end date")
                return
            }

            // Return filter results to mood journal
            val resultIntent = Intent().apply {
                putExtra("start_date", startDate.timeInMillis)
                putExtra("end_date", endDate.timeInMillis)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Date filter applied: ${startDate.time} to ${endDate.time}")

        } catch (e: Exception) {
            Log.e(TAG, "Error applying filter: ${e.message}")
            showToast("Error applying filter")
        }
    }

    private fun clearFilter() {
        try {
            // Return cancelled result to mood journal
            setResult(RESULT_CANCELED)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Date filter cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing filter: ${e.message}")
        }
    }

    private fun resetDates() {
        try {
            // Set default date range (last 7 days)
            endDate = Calendar.getInstance()
            startDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting dates: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        clearFilter()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Date Filter Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Date Filter Activity Destroyed")
    }
}