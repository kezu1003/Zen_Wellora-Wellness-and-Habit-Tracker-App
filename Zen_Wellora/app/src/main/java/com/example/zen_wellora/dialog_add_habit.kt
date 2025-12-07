package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class dialog_add_habit : AppCompatActivity() {

    // UI Components
    private lateinit var ivCloseDialog: ImageView
    private lateinit var tilHabitName: TextInputLayout
    private lateinit var etHabitName: TextInputEditText
    private lateinit var tilHabitDescription: TextInputLayout
    private lateinit var etHabitDescription: TextInputEditText
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var tvCancel: TextView
    private lateinit var cvSaveHabit: CardView

    private lateinit var habitManager: HabitManager
    private lateinit var rvHabitIcons: RecyclerView
    private lateinit var habitIconAdapter: HabitIconAdapter
    private var selectedCategory = ""
    private var selectedIcon: HabitIcon? = null
    private var isEditMode = false
    private var habitId: Long = -1

    companion object {
        private const val TAG = "AddHabitActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_add_habit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize HabitManager
        habitManager = HabitManager(this)
        
        // Load existing habits first
        habitManager.loadHabitsFromStorage()

        initializeViews()
        setupClickListeners()
        setupRecyclerView()
        loadExistingHabitData()

        Log.d(TAG, "Add Habit Activity Created")
    }

    private fun initializeViews() {
        try {
            // Header views
            ivCloseDialog = findViewById(R.id.iv_close_dialog)

            // Input fields
            tilHabitName = findViewById(R.id.til_habit_name)
            etHabitName = findViewById(R.id.et_habit_name)
            tilHabitDescription = findViewById(R.id.til_habit_description)
            etHabitDescription = findViewById(R.id.et_habit_description)

            // Icon selection
            rvHabitIcons = findViewById(R.id.rv_habit_icons)

            // Category selection
            chipGroupCategories = findViewById(R.id.chip_group_categories)

            // Action buttons
            tvCancel = findViewById(R.id.tv_cancel)
            cvSaveHabit = findViewById(R.id.cv_save_habit)

            Log.d(TAG, "All add habit views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing add habit views: ${e.message}")
            showToast("Error initializing add habit")
        }
    }

    private fun setupClickListeners() {
        try {
            // Close button
            ivCloseDialog.setOnClickListener {
                closeActivity()
            }

            // Cancel button
            tvCancel.setOnClickListener {
                closeActivity()
            }

            // Save habit button
            cvSaveHabit.setOnClickListener {
                saveHabit()
            }

            // Category selection
            chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
                onCategorySelected(checkedIds)
            }

            Log.d(TAG, "All add habit click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up add habit click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun loadExistingHabitData() {
        try {
            // Check if we're editing an existing habit
            habitId = intent.getLongExtra("habit_id", -1)
            if (habitId != -1L) {
                isEditMode = true
                val habit = habitManager.getHabitById(habitId)
                habit?.let {
                    etHabitName.setText(it.name)
                    etHabitDescription.setText(it.description)
                    selectedCategory = it.category
                    // Create HabitIcon with appropriate drawable based on icon name
                    val iconRes = when (it.icon) {
                        "exercise" -> R.drawable.ic_exercise
                        "meditation" -> R.drawable.ic_meditation
                        "reading" -> R.drawable.ic_reading
                        "water" -> R.drawable.ic_water
                        "sleep" -> R.drawable.ic_sleep
                        "study" -> R.drawable.ic_study
                        "workout" -> R.drawable.ic_workout
                        "journal" -> R.drawable.ic_journal
                        "walking" -> R.drawable.ic_walking
                        "yoga" -> R.drawable.ic_yoga
                        "music" -> R.drawable.ic_music
                        "art" -> R.drawable.ic_art
                        else -> R.drawable.ic_exercise // Default fallback
                    }
                    selectedIcon = HabitIcon(it.icon, iconRes, it.icon)
                    
                    // Set the selected icon in the adapter
                    habitIconAdapter.setSelectedIcon(it.icon)
                    
                    // TODO: Set the category chip as selected
                    Log.d(TAG, "Loading existing habit for editing: ${it.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading existing habit data: ${e.message}")
        }
    }

    private fun setupRecyclerView() {
        try {
            // Create list of available habit icons
            val habitIcons = listOf(
                HabitIcon("exercise", R.drawable.ic_exercise, "Exercise"),
                HabitIcon("meditation", R.drawable.ic_meditation, "Meditation"),
                HabitIcon("reading", R.drawable.ic_reading, "Reading"),
                HabitIcon("water", R.drawable.ic_water, "Water"),
                HabitIcon("sleep", R.drawable.ic_sleep, "Sleep"),
                HabitIcon("study", R.drawable.ic_study, "Study"),
                HabitIcon("workout", R.drawable.ic_workout, "Workout"),
                HabitIcon("journal", R.drawable.ic_journal, "Journal"),
                HabitIcon("walking", R.drawable.ic_walking, "Walking"),
                HabitIcon("yoga", R.drawable.ic_yoga, "Yoga"),
                HabitIcon("music", R.drawable.ic_music, "Music"),
                HabitIcon("art", R.drawable.ic_art, "Art")
            )

            // Create adapter
            habitIconAdapter = HabitIconAdapter(habitIcons) { icon ->
                selectedIcon = icon
                Log.d(TAG, "Icon selected: ${icon.name}")
            }

            // Setup RecyclerView
            rvHabitIcons.apply {
                layoutManager = GridLayoutManager(this@dialog_add_habit, 6)
                adapter = habitIconAdapter
            }

            Log.d(TAG, "Habit icons RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}")
            showToast("Error setting up icon selection")
        }
    }

    private fun onCategorySelected(checkedIds: List<Int>) {
        try {
            if (checkedIds.isNotEmpty()) {
                val selectedChip = findViewById<Chip>(checkedIds[0])
                selectedCategory = selectedChip.text.toString()
                Log.d(TAG, "Category selected: $selectedCategory")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting category: ${e.message}")
        }
    }

    private fun saveHabit() {
        try {
            // Get input values
            val habitName = etHabitName.text.toString().trim()
            val habitDescription = etHabitDescription.text.toString().trim()

            // Validate inputs
            if (habitName.isEmpty()) {
                tilHabitName.error = "Please enter habit name"
                return
            }

            if (selectedCategory.isEmpty()) {
                showToast("Please select a category")
                return
            }

            if (selectedIcon == null) {
                showToast("Please select an icon")
                return
            }

            // Clear errors
            tilHabitName.error = null

            // Save or update habit using HabitManager
            val success = if (isEditMode && habitId != -1L) {
                habitManager.editHabit(habitId, habitName, habitDescription, selectedCategory, selectedIcon!!.name)
            } else {
                habitManager.addHabit(habitName, habitDescription, selectedCategory, selectedIcon!!.name)
            }

            if (success) {
                // Return success result
                val resultIntent = Intent()
                setResult(RESULT_OK, resultIntent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                val action = if (isEditMode) "updated" else "added"
                Log.d(TAG, "Habit $action: $habitName, Category: $selectedCategory")
                showToast("Habit '$habitName' $action successfully!")
            } else {
                showToast("Error saving habit")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error saving habit: ${e.message}")
            showToast("Error saving habit")
        }
    }


    private fun closeActivity() {
        try {
            setResult(RESULT_CANCELED)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Add habit activity closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing activity: ${e.message}")
            finish()
        }
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
        Log.d(TAG, "Add Habit Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Add Habit Activity Destroyed")
    }
}