package com.example.zen_wellora

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Fragment_mood_calendar : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var btnPrevMonth: Button
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnNextMonth: Button
    private lateinit var gvCalendar: GridView

    private var currentDate = Calendar.getInstance()
    private val moodEntries = mutableListOf<Fragment_mood_journal.MoodEntry>()
    private val calendarDays = mutableListOf<CalendarDay>()

    companion object {
        private const val TAG = "MoodCalendarActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_mood_calendar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        loadMoodEntries()
        setupCalendar()

        Log.d(TAG, "Mood Calendar Activity Created")
    }

    private fun initializeViews() {
        try {
            // Back arrow
            ivBack = findViewById(R.id.iv_back)
            
            // Month navigation
            btnPrevMonth = findViewById(R.id.btnPrevMonth)
            tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
            btnNextMonth = findViewById(R.id.btnNextMonth)

            // Calendar grid
            gvCalendar = findViewById(R.id.gvCalendar)

            Log.d(TAG, "All calendar view views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing calendar view views: ${e.message}")
            showToast("Error initializing calendar view")
        }
    }

    private fun setupClickListeners() {
        try {
            // Back arrow
            ivBack.setOnClickListener {
                Log.d(TAG, "Back arrow clicked")
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Previous month button
            btnPrevMonth.setOnClickListener {
                navigateToPreviousMonth()
            }

            // Next month button
            btnNextMonth.setOnClickListener {
                navigateToNextMonth()
            }

            Log.d(TAG, "All calendar view click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up calendar view click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoodEntries() {
        try {
            moodEntries.clear()

            // Load real mood entries from SharedPreferences
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val moodEntriesJson = sharedPreferences.getString("mood_entries", "[]")
            
            if (!moodEntriesJson.isNullOrEmpty() && moodEntriesJson != "[]") {
                try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<Fragment_mood_journal.MoodEntry>>() {}.type
                    val loadedEntries = gson.fromJson<List<Fragment_mood_journal.MoodEntry>>(moodEntriesJson, type)
                    
                    if (loadedEntries != null) {
                        moodEntries.addAll(loadedEntries)
                        Log.d(TAG, "Loaded ${moodEntries.size} real mood entries from SharedPreferences")
                    } else {
                        Log.d(TAG, "No mood entries found in SharedPreferences")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing mood entries from SharedPreferences: ${e.message}")
                    // If parsing fails, start with empty list
                }
            } else {
                Log.d(TAG, "Mood calendar is empty for new user - starting fresh")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading mood entries: ${e.message}")
        }
    }

    private fun setupCalendar() {
        try {
            generateCalendarDays()
            updateMonthDisplay()

            // Use ArrayAdapter instead of custom BaseAdapter for simplicity
            val dayTexts = calendarDays.map { day ->
                if (day.isCurrentMonth) {
                    if (day.hasMood) {
                        "${day.dayNumber}\n${day.moodEntry?.emoji ?: ""}"
                    } else {
                        day.dayNumber
                    }
                } else {
                    ""
                }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dayTexts)
            gvCalendar.adapter = adapter

            gvCalendar.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val day = calendarDays[position]
                if (day.isCurrentMonth && day.hasMood) {
                    showMoodDetails(day)
                }
            }

            Log.d(TAG, "Calendar set up successfully for ${currentDate.get(Calendar.MONTH)}/${currentDate.get(Calendar.YEAR)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up calendar: ${e.message}")
        }
    }

    private fun generateCalendarDays() {
        try {
            calendarDays.clear()

            val calendar = currentDate.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            // Get first day of month and number of days
            val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Add empty days for previous month
            for (i in 1 until firstDayOfMonth) {
                calendarDays.add(CalendarDay("", false, false, null))
            }

            // Add days for current month
            for (day in 1..daysInMonth) {
                val dayCalendar = Calendar.getInstance().apply {
                    set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), day)
                }

                // Check if this day has any mood entries
                val moodForDay = getMoodForDate(dayCalendar.timeInMillis)
                val hasMood = moodForDay != null

                calendarDays.add(CalendarDay(day.toString(), true, hasMood, moodForDay))
            }

            Log.d(TAG, "Generated ${calendarDays.size} calendar days for month")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating calendar days: ${e.message}")
        }
    }

    private fun getMoodForDate(date: Long): Fragment_mood_journal.MoodEntry? {
        try {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis

            return moodEntries.firstOrNull { moodEntry ->
                moodEntry.date in startOfDay until endOfDay
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting mood for date: ${e.message}")
            return null
        }
    }

    private fun navigateToPreviousMonth() {
        try {
            currentDate.add(Calendar.MONTH, -1)
            setupCalendar()
            Log.d(TAG, "Navigated to previous month")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to previous month: ${e.message}")
        }
    }

    private fun navigateToNextMonth() {
        try {
            currentDate.add(Calendar.MONTH, 1)
            setupCalendar()
            Log.d(TAG, "Navigated to next month")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to next month: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMonthDisplay() {
        try {
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            tvCurrentMonth.text = monthFormat.format(currentDate.time)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating month display: ${e.message}")
            tvCurrentMonth.text = "Calendar"
        }
    }

    private fun showMoodDetails(day: CalendarDay) {
        try {
            day.moodEntry?.let { mood ->
                val message = "${mood.emoji} ${mood.mood}\n${mood.description}"
                showToast(message)
                Log.d(TAG, "Showing mood details: ${mood.mood}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing mood details: ${e.message}")
        }
    }

    private fun navigateBackToMoodJournal() {
        try {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Navigated back to mood journal")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back to mood journal: ${e.message}")
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        navigateBackToMoodJournal()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Mood Calendar Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Mood Calendar Activity Destroyed")
    }

    // Data class for Calendar Day
    data class CalendarDay(
        val dayNumber: String,
        val isCurrentMonth: Boolean,
        val hasMood: Boolean,
        val moodEntry: Fragment_mood_journal.MoodEntry?
    )
}