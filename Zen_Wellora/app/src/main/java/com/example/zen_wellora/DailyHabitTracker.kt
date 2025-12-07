package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyHabitTracker : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var tvDailyPercentage: TextView
    private lateinit var tvHabitProgress: TextView
    private lateinit var tvBestStreak: TextView
    private lateinit var ivPrevDay: ImageView
    private lateinit var tvSelectedDate: TextView
    private lateinit var ivNextDay: ImageView
    private lateinit var tvEditHabits: TextView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var rvHabits: RecyclerView
    private lateinit var llEmptyState: android.widget.LinearLayout
    private lateinit var cvAddFirstHabit: androidx.cardview.widget.CardView

    private lateinit var habitManager: HabitManager
    private lateinit var habitsAdapter: HabitsAdapter
    private var currentDate = Calendar.getInstance()

    companion object {
        private const val TAG = "HabitTrackerActivity"
    }

    // Activity result launcher for add habit
    private val addHabitLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh habits list when returning from add habit
            habitManager.loadHabitsFromStorage()
            updateHabitProgress()
            updateHabitsList()
            showToast("Habit added successfully!")
            Log.d(TAG, "New habit added, refreshing list")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "Starting Habit Tracker Activity creation")
            
            enableEdgeToEdge()
            setContentView(R.layout.activity_daily_habit_tracker)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Initialize HabitManager with error handling
            try {
                habitManager = HabitManager(this)
                Log.d(TAG, "HabitManager initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize HabitManager: ${e.message}", e)
                showToast("Error initializing habit manager")
                finish()
                return
            }

            // Initialize components with individual error handling
            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views - finishing activity")
                finish()
                return
            }
            
            setupClickListeners()
            setupRecyclerView()
            loadHabitData()
            updateDateDisplay()

            Log.d(TAG, "Habit Tracker Activity Created Successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate: ${e.message}", e)
            showToast("Error starting habit tracker")
            finish()
        }
    }

    private fun initializeViews(): Boolean {
        return try {
            Log.d(TAG, "Initializing habit tracker views...")
            
            // Header views
            ivBack = findViewById(R.id.iv_back)
                ?: throw IllegalStateException("iv_back not found")

            // Progress views
            tvDailyPercentage = findViewById(R.id.tv_daily_percentage)
                ?: throw IllegalStateException("tv_daily_percentage not found")
            tvHabitProgress = findViewById(R.id.tv_habit_progress)
                ?: throw IllegalStateException("tv_habit_progress not found")
            tvBestStreak = findViewById(R.id.tv_best_streak)
                ?: throw IllegalStateException("tv_best_streak not found")

            // Date navigation views
            ivPrevDay = findViewById(R.id.iv_prev_day)
                ?: throw IllegalStateException("iv_prev_day not found")
            tvSelectedDate = findViewById(R.id.tv_selected_date)
                ?: throw IllegalStateException("tv_selected_date not found")
            ivNextDay = findViewById(R.id.iv_next_day)
                ?: throw IllegalStateException("iv_next_day not found")

            // Action views
            tvEditHabits = findViewById(R.id.tv_edit_habits)
                ?: throw IllegalStateException("tv_edit_habits not found")
            fabAddHabit = findViewById(R.id.fab_add_habit)
                ?: throw IllegalStateException("fab_add_habit not found")

            // Habits list views
            rvHabits = findViewById(R.id.rv_habits)
                ?: throw IllegalStateException("rv_habits not found")
            llEmptyState = findViewById(R.id.ll_empty_state)
                ?: throw IllegalStateException("ll_empty_state not found")
            cvAddFirstHabit = findViewById(R.id.cv_add_first_habit)
                ?: throw IllegalStateException("cv_add_first_habit not found")

            Log.d(TAG, "All habit tracker views initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing habit tracker views: ${e.message}", e)
            showToast("Error initializing habit tracker views")
            false
        }
    }

    private fun setupRecyclerView() {
        try {
            Log.d(TAG, "Setting up RecyclerView...")
            
            // Safely get habits with null check
            val initialHabits = try {
                habitManager.getAllHabits()
            } catch (e: Exception) {
                Log.w(TAG, "Error getting initial habits: ${e.message}")
                emptyList<Habit>()
            }
            
            habitsAdapter = HabitsAdapter(initialHabits) { habit, action ->
                try {
                    when (action) {
                        "complete" -> {
                            habitManager.markHabitCompleted(habit.id)
                            updateHabitProgress()
                            updateHabitsList()
                            showToast("Habit '${habit.name}' completed!")
                        }
                        "edit" -> {
                            editHabit(habit)
                        }
                        "delete" -> {
                            deleteHabit(habit)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling habit action '$action': ${e.message}")
                    showToast("Error performing habit action")
                }
            }

            rvHabits.apply {
                layoutManager = LinearLayoutManager(this@DailyHabitTracker)
                adapter = habitsAdapter
            }
            
            Log.d(TAG, "RecyclerView setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}", e)
            showToast("Error setting up habits list")
        }
    }

    private fun setupClickListeners() {
        try {
            // Back button - navigate back to home
            ivBack.setOnClickListener {
                navigateBackToHome()
            }

            // Date navigation
            ivPrevDay.setOnClickListener {
                navigateToPreviousDay()
            }

            ivNextDay.setOnClickListener {
                navigateToNextDay()
            }

            // Edit habits
            tvEditHabits.setOnClickListener {
                toggleEditMode()
            }

            // Add habit FAB
            fabAddHabit.setOnClickListener {
                openAddHabitActivity()
            }

            // Add first habit from empty state
            cvAddFirstHabit.setOnClickListener {
                openAddHabitActivity()
            }

            Log.d(TAG, "All habit tracker click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up habit tracker click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun openAddHabitActivity() {
        try {
            val intent = Intent(this, dialog_add_habit::class.java)
            addHabitLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Opening Add Habit activity")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Add Habit: ${e.message}")
            showToast("Error opening add habit")
        }
    }

    private fun editHabit(habit: Habit) {
        try {
            val intent = Intent(this, dialog_add_habit::class.java).apply {
                putExtra("habit_id", habit.id)
                putExtra("habit_name", habit.name)
                putExtra("habit_description", habit.description)
                putExtra("habit_category", habit.category)
            }
            addHabitLauncher.launch(intent)
            Log.d(TAG, "Editing habit: ${habit.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error editing habit: ${e.message}")
            showToast("Error editing habit")
        }
    }

    private fun deleteHabit(habit: Habit) {
        try {
            habitManager.deleteHabit(habit.id)
            updateHabitProgress()
            updateHabitsList()
            showToast("Habit '${habit.name}' deleted")
            Log.d(TAG, "Habit deleted: ${habit.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting habit: ${e.message}")
            showToast("Error deleting habit")
        }
    }

    private fun toggleEditMode() {
        // Toggle between view and edit mode
        val isEditMode = habitsAdapter.toggleEditMode()
        tvEditHabits.text = if (isEditMode) "Done" else "Edit"
        showToast(if (isEditMode) "Edit mode enabled" else "Edit mode disabled")
    }

    private fun loadHabitData() {
        try {
            habitManager.loadHabitsFromStorage()
            updateHabitProgress()
            updateHabitsList()
            Log.d(TAG, "Habit data loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading habit data: ${e.message}")
            showToast("Error loading habit data")
        }
    }

    private fun updateHabitProgress() {
        try {
            val percentage = habitManager.getTodayCompletionPercentage()
            val (completed, total) = habitManager.getTodayProgress()
            val bestStreak = habitManager.getBestStreak()

            // Update UI
            tvDailyPercentage.text = "$percentage%"
            tvHabitProgress.text = "$completed/$total"
            tvBestStreak.text = "$bestStreak days"

            Log.d(TAG, "Habit progress updated: $completed/$total ($percentage%)")
            
            // Update widget with new data
            updateWidget()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating habit progress: ${e.message}")
        }
    }
    
    private fun updateWidget() {
        try {
            val intent = Intent(this, HabitWidget::class.java)
            intent.action = "UPDATE_WIDGET"
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget: ${e.message}")
        }
    }

    private fun updateHabitsList() {
        try {
            val habits = try {
                habitManager.getAllHabits()
            } catch (e: Exception) {
                Log.w(TAG, "Error getting habits for update: ${e.message}")
                emptyList<Habit>()
            }
            
            if (::habitsAdapter.isInitialized) {
                habitsAdapter.updateHabits(habits)
            } else {
                Log.w(TAG, "HabitsAdapter not initialized when trying to update")
                return
            }

            // Show/hide empty state with null checks
            try {
                if (habits.isEmpty()) {
                    rvHabits.visibility = android.view.View.GONE
                    llEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    rvHabits.visibility = android.view.View.VISIBLE
                    llEmptyState.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating visibility: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating habits list: ${e.message}", e)
        }
    }

    private fun navigateToPreviousDay() {
        try {
            currentDate.add(Calendar.DAY_OF_MONTH, -1)
            updateDateDisplay()
            loadHabitDataForDate()
            Log.d(TAG, "Navigated to previous day")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to previous day: ${e.message}")
        }
    }

    private fun navigateToNextDay() {
        try {
            val today = Calendar.getInstance()
            if (currentDate.before(today)) {
                currentDate.add(Calendar.DAY_OF_MONTH, 1)
                updateDateDisplay()
                loadHabitDataForDate()
                Log.d(TAG, "Navigated to next day")
            } else {
                showToast("Cannot navigate to future dates")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to next day: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateDisplay() {
        try {
            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

            val displayText = if (isToday(currentDate)) {
                "Today, ${dateFormat.format(currentDate.time)}"
            } else if (isYesterday(currentDate)) {
                "Yesterday, ${dateFormat.format(currentDate.time)}"
            } else {
                dateFormat.format(currentDate.time)
            }

            tvSelectedDate.text = displayText
        } catch (e: Exception) {
            Log.e(TAG, "Error updating date display: ${e.message}")
            tvSelectedDate.text = "Today"
        }
    }

    private fun loadHabitDataForDate() {
        try {
            // For now, we'll use the same data for all dates
            // In a real app, you would load data specific to the selected date
            loadHabitData()
            Log.d(TAG, "Loaded habit data for selected date")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading habit data for date: ${e.message}")
        }
    }

    private fun isToday(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(date: Calendar): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return date.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                date.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }

    private fun navigateBackToHome() {
        try {
            // You can pass back any updated data if needed
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Navigated back to home")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back to home: ${e.message}")
            finish()
        }
    }

    private fun showToast(message: String) {
        try {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Skipping toast (activity finishing/destroyed): $message")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: ${e.message}")
        }
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        navigateBackToHome()
    }

    override fun onResume() {
        super.onResume()
        
        try {
            Log.d(TAG, "Habit Tracker Activity Resumed")
            
            // Only refresh if habitManager is properly initialized
            if (::habitManager.isInitialized) {
                // Refresh data when returning to habit tracker
                try {
                    habitManager.loadHabitsFromStorage()
                    updateHabitProgress()
                    updateHabitsList()
                    Log.d(TAG, "Data refreshed successfully on resume")
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing data on resume: ${e.message}")
                    showToast("Error refreshing habit data")
                }
            } else {
                Log.w(TAG, "HabitManager not initialized on resume - skipping refresh")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Habit Tracker Activity Destroyed")
    }
}