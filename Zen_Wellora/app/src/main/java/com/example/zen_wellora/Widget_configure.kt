package com.example.zen_wellora

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Widget_configure : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var tvPreviewTitle: TextView
    private lateinit var tvPreviewPercentage: TextView
    private lateinit var tvPreviewTime: TextView
    private lateinit var tvPreviewCompleted: TextView
    private lateinit var tvPreviewTotal: TextView
    private lateinit var tvPreviewStreak: TextView
    private lateinit var ivPreviewProgressFill: ImageView
    
    // Customization controls
    private lateinit var radioSmall: RadioButton
    private lateinit var radioMedium: RadioButton
    private lateinit var radioLarge: RadioButton
    private lateinit var radioLight: RadioButton
    private lateinit var radioDark: RadioButton
    private lateinit var spinnerFrequency: Spinner

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "WidgetConfigure"
        private const val PREFS_NAME = "WidgetPrefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_widget_configure)

        Log.d(TAG, "Widget Configure Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Get widget ID from intent - THIS IS CRITICAL
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        Log.d(TAG, "Received widget ID: $appWidgetId")

        // If no valid widget ID, check if this is a test launch
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.w(TAG, "No valid widget ID - this might be a test launch")
            // Don't finish immediately - let user see the configuration
            // showToast("No widget ID found - this is a preview")
        }

        initializeViews()
        setupClickListeners()
        setupCustomizationOptions()
        loadSampleData()
    }

    private fun initializeViews() {
        try {
            btnSave = findViewById(R.id.btn_save)
            btnCancel = findViewById(R.id.btn_cancel)

            // Initialize preview text views
            tvPreviewTitle = findViewById(R.id.widget_preview_title)
            tvPreviewPercentage = findViewById(R.id.widget_preview_percentage)
            tvPreviewTime = findViewById(R.id.widget_preview_time)
            tvPreviewCompleted = findViewById(R.id.preview_completed)
            tvPreviewTotal = findViewById(R.id.preview_total)
            tvPreviewStreak = findViewById(R.id.preview_streak)
            ivPreviewProgressFill = findViewById(R.id.preview_progress_fill)
            
            // Initialize customization controls
            radioSmall = findViewById(R.id.radio_small)
            radioMedium = findViewById(R.id.radio_medium)
            radioLarge = findViewById(R.id.radio_large)
            radioLight = findViewById(R.id.radio_light)
            radioDark = findViewById(R.id.radio_dark)
            spinnerFrequency = findViewById(R.id.spinner_frequency)

            Log.d(TAG, "Widget configuration views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            showToast("Error initializing widget configuration")
        }
    }

    private fun setupClickListeners() {
        // Save button - create widget
        btnSave.setOnClickListener {
            Log.d(TAG, "Save button clicked for widget ID: $appWidgetId")
            saveWidgetConfiguration()
        }

        // Cancel button - finish without creating widget
        btnCancel.setOnClickListener {
            Log.d(TAG, "Cancel button clicked")
            cancelWidgetConfiguration()
        }
    }

    private fun setupCustomizationOptions() {
        try {
            // Setup frequency spinner
            val frequencyOptions = arrayOf("15 minutes", "30 minutes", "1 hour", "2 hours")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencyOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFrequency.adapter = adapter
            spinnerFrequency.setSelection(1) // Default to 30 minutes
            
            // Set default selections
            radioMedium.isChecked = true
            radioLight.isChecked = true
            
            Log.d(TAG, "Widget customization options initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up customization options: ${e.message}")
        }
    }

    private fun loadSampleData() {
        try {
            // Load real data from HabitManager
            val habitManager = HabitManager(this)
            habitManager.loadHabitsFromStorage()
            
            val (completed, total) = habitManager.getTodayProgress()
            val percentage = habitManager.getTodayCompletionPercentage()

            // Update preview with real data
            updatePreview("$percentage%", completed, total)

            Log.d(TAG, "Real habit data loaded for widget preview: $completed/$total ($percentage%)")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading habit data: ${e.message}")
            // Fallback to sample data
            updatePreview("0%", 0, 0)
        }
    }

    private fun updatePreview(percentage: String, completed: Int, total: Int) {
        try {
            tvPreviewTitle.text = "Habit Progress"
            tvPreviewPercentage.text = percentage
            tvPreviewCompleted.text = completed.toString()
            tvPreviewTotal.text = total.toString()
            
            // Update time
            tvPreviewTime.text = "Updated now"
            
            // Update streak (get from HabitManager)
            val habitManager = HabitManager(this)
            habitManager.loadHabitsFromStorage()
            val streakCount = habitManager.getBestStreak()
            tvPreviewStreak.text = streakCount.toString()
            
            // Update progress circle rotation
            val progressValue = if (total > 0) (completed.toFloat() / total.toFloat()) * 360f else 0f
            ivPreviewProgressFill.rotation = progressValue - 90f
            
            Log.d(TAG, "Preview updated: $completed/$total ($percentage), streak: $streakCount")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview: ${e.message}")
        }
    }

    private fun saveWidgetConfiguration() {
        try {
            // If no valid widget ID, this might be a test - create a temporary one
            val workingAppWidgetId = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.w(TAG, "No valid widget ID, using temporary ID for testing")
                1 // Temporary ID for testing
            } else {
                appWidgetId
            }

            Log.d(TAG, "Saving widget configuration for ID: $workingAppWidgetId")

            // Get real data from HabitManager
            val habitManager = HabitManager(this)
            habitManager.loadHabitsFromStorage()
            
            val (completed, total) = habitManager.getTodayProgress()
            val percentage = habitManager.getTodayCompletionPercentage()
            val streakCount = habitManager.getBestStreak()

            // Get customization settings
            val widgetSize = when {
                radioSmall.isChecked -> "small"
                radioLarge.isChecked -> "large"
                else -> "medium"
            }
            val widgetTheme = if (radioDark.isChecked) "dark" else "light"
            val updateFrequency = spinnerFrequency.selectedItem.toString()
            
            // Save widget configuration to SharedPreferences (for backup)
            val editor = sharedPreferences.edit()
            editor.putString("widget_${workingAppWidgetId}_percentage", "$percentage%")
            editor.putInt("widget_${workingAppWidgetId}_completed", completed)
            editor.putInt("widget_${workingAppWidgetId}_total", total)
            editor.putInt("widget_${workingAppWidgetId}_streak", streakCount)
            editor.putLong("widget_${workingAppWidgetId}_timestamp", System.currentTimeMillis())
            editor.putString("widget_${workingAppWidgetId}_size", widgetSize)
            editor.putString("widget_${workingAppWidgetId}_theme", widgetTheme)
            editor.putString("widget_${workingAppWidgetId}_frequency", updateFrequency)
            editor.apply()

            Log.d(TAG, "Widget preferences saved with real data: $completed/$total ($percentage%)")

            // Update the widget immediately
            updateWidget(workingAppWidgetId)

            // Only set result if we have a valid widget ID
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
            } else {
                Log.w(TAG, "No valid widget ID - skipping result set")
            }

            showToast("Widget added successfully!")

            Log.d(TAG, "Widget configuration completed successfully")

            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving widget configuration: ${e.message}", e)
            showToast("Error saving widget configuration")
        }
    }

    private fun cancelWidgetConfiguration() {
        try {
            Log.d(TAG, "Cancelling widget configuration")

            // Only set result if we have a valid widget ID
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                setResult(RESULT_CANCELED)
            }

            finish()

            Log.d(TAG, "Widget configuration cancelled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling widget configuration: ${e.message}", e)
        }
    }

    private fun updateWidget(widgetId: Int) {
        try {
            Log.d(TAG, "Updating widget with ID: $widgetId")

            // Update the widget using AppWidgetManager
            val appWidgetManager = AppWidgetManager.getInstance(this)
            HabitWidget.updateAppWidget(this, appWidgetManager, widgetId)

            Log.d(TAG, "Widget update triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget: ${e.message}", e)
            showToast("Error updating widget")
        }
    }
    
    private fun refreshAllWidgets() {
        try {
            Log.d(TAG, "Refreshing all widgets")
            HabitWidget.refreshAllWidgets(this)
            Log.d(TAG, "All widgets refreshed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing widgets: ${e.message}", e)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}