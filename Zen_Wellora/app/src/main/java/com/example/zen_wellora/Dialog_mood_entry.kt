package com.example.zen_wellora

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Dialog_mood_entry : AppCompatActivity() {

    // UI Components
    private lateinit var btnSelectDate: Button
    private lateinit var btnSelectTime: Button
    private lateinit var gvEmojis: GridView
    private lateinit var etNotes: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button

    private var selectedDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    private var selectedEmoji = "😊"
    private var isEditMode = false
    private var editMoodEntry: Fragment_mood_journal.MoodEntry? = null
    private val emojiList = listOf("😊", "😄", "😍", "🥰", "😎", "😢", "😠", "😴", "😰", "🤒")

    companion object {
        private const val TAG = "AddMoodEntryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_mood_entry)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        setupEmojiGrid()
        loadEditData()
        updateDateTimeButtons()

        Log.d(TAG, "Add Mood Entry Activity Created")
    }

    private fun initializeViews() {
        try {
            // Date and time buttons
            btnSelectDate = findViewById(R.id.btnSelectDate)
            btnSelectTime = findViewById(R.id.btnSelectTime)

            // Emoji grid
            gvEmojis = findViewById(R.id.gvEmojis)

            // Notes field
            etNotes = findViewById(R.id.etNotes)

            // Action buttons
            btnCancel = findViewById(R.id.btnCancel)
            btnSave = findViewById(R.id.btnSave)

            Log.d(TAG, "All add mood entry views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing add mood entry views: ${e.message}")
            showToast("Error initializing add mood entry")
        }
    }

    private fun setupClickListeners() {
        try {
            // Date selection button
            btnSelectDate.setOnClickListener {
                showDatePicker()
            }

            // Time selection button
            btnSelectTime.setOnClickListener {
                showTimePicker()
            }

            // Cancel button
            btnCancel.setOnClickListener {
                closeActivity()
            }

            // Save button
            btnSave.setOnClickListener {
                saveMoodEntry()
            }

            Log.d(TAG, "All add mood entry click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up add mood entry click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun setupEmojiGrid() {
        try {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emojiList)
            gvEmojis.adapter = adapter

            gvEmojis.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedEmoji = emojiList[position]
                showToast("Selected: $selectedEmoji")
                Log.d(TAG, "Emoji selected: $selectedEmoji")
            }

            Log.d(TAG, "Emoji grid set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up emoji grid: ${e.message}")
        }
    }

    private fun loadEditData() {
        try {
            // Check if we're in edit mode
            editMoodEntry = intent.getSerializableExtra("edit_mood") as? Fragment_mood_journal.MoodEntry
            if (editMoodEntry != null) {
                isEditMode = true
                
                // Load existing data
                selectedEmoji = editMoodEntry!!.emoji
                etNotes.setText(editMoodEntry!!.description)
                
                // Set date and time from existing entry
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = editMoodEntry!!.date
                selectedDate = calendar
                selectedTime = calendar
                
                Log.d(TAG, "Edit mode: Loading existing mood entry")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading edit data: ${e.message}")
        }
    }

    private fun showDatePicker() {
        try {
            val today = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    updateDateTimeButtons()
                    Log.d(TAG, "Date selected: $year-${month + 1}-$dayOfMonth")
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
            
            // Restrict to current date only
            datePicker.datePicker.maxDate = today.timeInMillis
            datePicker.datePicker.minDate = today.timeInMillis
            
            datePicker.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing date picker: ${e.message}")
            showToast("Error selecting date")
        }
    }

    private fun showTimePicker() {
        try {
            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)
                    updateDateTimeButtons()
                    Log.d(TAG, "Time selected: $hourOfDay:$minute")
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing time picker: ${e.message}")
            showToast("Error selecting time")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateTimeButtons() {
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            btnSelectDate.text = dateFormat.format(selectedDate.time)
            btnSelectTime.text = timeFormat.format(selectedTime.time)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating date/time buttons: ${e.message}")
        }
    }

    private fun saveMoodEntry() {
        try {
            val notes = etNotes.text.toString().trim()
            val moodDescription = getMoodDescription(selectedEmoji)

            // Combine date and time
            val combinedDateTime = Calendar.getInstance().apply {
                set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
            }

            val resultIntent = Intent()
            
            if (isEditMode && editMoodEntry != null) {
                // Edit existing mood entry
                val updatedMood = editMoodEntry!!.copy(
                    mood = moodDescription,
                    description = notes.ifEmpty { "No notes" },
                    date = combinedDateTime.timeInMillis,
                    emoji = selectedEmoji
                )
                resultIntent.putExtra("edited_mood", updatedMood)
                Log.d(TAG, "Mood entry updated: $moodDescription, Emoji: $selectedEmoji")
            } else {
                // Create new mood entry
                val newMood = Fragment_mood_journal.MoodEntry(
                    id = System.currentTimeMillis().toInt(),
                    mood = moodDescription,
                    description = notes.ifEmpty { "No notes" },
                    date = combinedDateTime.timeInMillis,
                    emoji = selectedEmoji
                )
                resultIntent.putExtra("new_mood", newMood)
                Log.d(TAG, "Mood entry saved: $moodDescription, Emoji: $selectedEmoji")
            }

            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entry: ${e.message}")
            showToast("Error saving mood entry")
        }
    }

    private fun getMoodDescription(emoji: String): String {
        return when (emoji) {
            "😊" -> "Happy"
            "😄" -> "Excited"
            "😍" -> "Loved"
            "🥰" -> "Loving"
            "😎" -> "Cool"
            "😢" -> "Sad"
            "😠" -> "Angry"
            "😴" -> "Tired"
            "😰" -> "Anxious"
            "🤒" -> "Sick"
            else -> "Neutral"
        }
    }

    private fun closeActivity() {
        try {
            setResult(RESULT_CANCELED)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Add mood entry activity closed")
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
        Log.d(TAG, "Add Mood Entry Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Add Mood Entry Activity Destroyed")
    }
}