package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Fragment_mood_journal : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var btnAddMood: AppCompatButton
    private lateinit var btnFilterDate: AppCompatButton
    private lateinit var btnViewCalendar: AppCompatButton
    private lateinit var btnShareSummary: AppCompatButton
    private lateinit var rvMoodEntries: RecyclerView
    private lateinit var tvEmptyState: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var moodAdapter: MoodEntryAdapter
    private val moodEntries = mutableListOf<MoodEntry>()
    private val filteredMoodEntries = mutableListOf<MoodEntry>()
    private var isFiltered = false

    companion object {
        private const val TAG = "MoodJournalActivity"
        private const val PREFS_NAME = "user_prefs"
        private const val MOOD_ENTRIES_KEY = "mood_entries"
    }

    // Activity result launcher for add mood entry
    private val addMoodLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Handle new mood entry
            result.data?.getSerializableExtra("new_mood")?.let { newMood ->
                if (newMood is MoodEntry) {
                    addNewMoodEntry(newMood)
                }
            }

            // Handle edited mood entry
            result.data?.getSerializableExtra("edited_mood")?.let { editedMood ->
                if (editedMood is MoodEntry) {
                    updateMoodEntry(editedMood)
                }
            }
        }
    }

    // Activity result launcher for date filter
    private val dateFilterLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Apply date filter when returning from filter activity
            result.data?.getLongExtra("start_date", 0L)?.let { startDate ->
                result.data?.getLongExtra("end_date", 0L)?.let { endDate ->
                    if (startDate > 0L && endDate > 0L) {
                        applyDateFilter(startDate, endDate)
                        showToast("Date filter applied")
                        Log.d(TAG, "Date filter applied: $startDate to $endDate")
                    }
                }
            }
        } else if (result.resultCode == RESULT_CANCELED) {
            // Clear filter if cancelled
            clearDateFilter()
            showToast("Date filter cleared")
            Log.d(TAG, "Date filter cleared")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_mood_journal)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadMoodEntries()
        updateEmptyState()

        Log.d(TAG, "Mood Journal Activity Created")
    }

    private fun initializeViews() {
        try {
            // Back arrow
            ivBack = findViewById(R.id.iv_back)
            
            // Action buttons
            btnAddMood = findViewById(R.id.btnAddMood)
            btnFilterDate = findViewById(R.id.btnFilterDate)
            btnViewCalendar = findViewById(R.id.btnViewCalendar)
            btnShareSummary = findViewById(R.id.btnShareSummary)

            // Mood entries list
            rvMoodEntries = findViewById(R.id.rvMoodEntries)
            tvEmptyState = findViewById(R.id.tvEmptyState)

            Log.d(TAG, "All mood journal views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing mood journal views: ${e.message}")
            showToast("Error initializing mood journal")
        }
    }

    private fun setupRecyclerView() {
        try {
            val layoutManager = LinearLayoutManager(this)
            rvMoodEntries.layoutManager = layoutManager

            // Initialize adapter
            moodAdapter = MoodEntryAdapter(
                moodEntries,
                onEditClick = { moodEntry ->
                    editMoodEntry(moodEntry)
                },
                onDeleteClick = { moodEntry ->
                    deleteMoodEntry(moodEntry)
                }
            )

            rvMoodEntries.adapter = moodAdapter
            Log.d(TAG, "RecyclerView set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}")
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
            
            // Add Mood button - opens Add Mood Entry activity
            btnAddMood.setOnClickListener {
                openAddMoodEntry()
            }

            // Filter Date button - opens Date Filter activity
            btnFilterDate.setOnClickListener {
                openDateFilter()
            }

            // View Calendar button - opens Calendar View activity
            btnViewCalendar.setOnClickListener {
                openCalendarView()
            }

            // Share Summary button - shares mood summary
            btnShareSummary.setOnClickListener {
                shareMoodSummary()
            }

            Log.d(TAG, "All mood journal click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up mood journal click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    // Open Add Mood Entry Activity
    private fun openAddMoodEntry() {
        try {
            val intent = Intent(this, Dialog_mood_entry::class.java)
            addMoodLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Opening Add Mood Entry activity")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Add Mood Entry: ${e.message}")
            showToast("Error opening add mood entry")
        }
    }

    // Open Date Filter Activity
    private fun openDateFilter() {
        try {
            val intent = Intent(this, Dialog_date_filter::class.java)
            dateFilterLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Opening Date Filter activity")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Date Filter: ${e.message}")
            showToast("Error opening date filter")
        }
    }

    // Open Calendar View Activity
    private fun openCalendarView() {
        try {
            val intent = Intent(this, Fragment_mood_calendar::class.java)

            // Pass mood entries to calendar view
            intent.putExtra("mood_entries", ArrayList(moodEntries))

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "Opening Calendar View activity with ${moodEntries.size} mood entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Calendar View: ${e.message}")
            showToast("Error opening calendar view")
        }
    }

    // Add new mood entry
    private fun addNewMoodEntry(newMood: MoodEntry) {
        try {
            moodEntries.add(0, newMood)
            saveMoodEntries()
            moodAdapter.addEntry(newMood)
            updateEmptyState()

            // Update mood count in SharedPreferences
            val currentMoodCount = sharedPreferences.getInt("moods_count", 0)
            sharedPreferences.edit().putInt("moods_count", currentMoodCount + 1).apply()

            showToast("Mood entry added: ${newMood.mood}")
            Log.d(TAG, "New mood entry added: ${newMood.mood}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding new mood entry: ${e.message}")
            showToast("Error adding mood entry")
        }
    }

    // Edit mood entry
    private fun editMoodEntry(moodEntry: MoodEntry) {
        try {
            val intent = Intent(this, Dialog_mood_entry::class.java).apply {
                putExtra("edit_mood", moodEntry)
            }
            addMoodLauncher.launch(intent)
            Log.d(TAG, "Editing mood entry: ${moodEntry.mood}")
        } catch (e: Exception) {
            Log.e(TAG, "Error editing mood entry: ${e.message}")
            showToast("Error editing mood entry")
        }
    }

    // Update existing mood entry
    private fun updateMoodEntry(updatedMood: MoodEntry) {
        try {
            val index = moodEntries.indexOfFirst { it.id == updatedMood.id }
            if (index != -1) {
                moodEntries[index] = updatedMood
                saveMoodEntries()
                moodAdapter.updateEntry(updatedMood)
                showToast("Mood entry updated: ${updatedMood.mood}")
                Log.d(TAG, "Mood entry updated: ${updatedMood.mood}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating mood entry: ${e.message}")
            showToast("Error updating mood entry")
        }
    }

    // Delete mood entry
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        try {
            AlertDialog.Builder(this)
                .setTitle("Delete Mood Entry")
                .setMessage("Are you sure you want to delete this mood entry?")
                .setPositiveButton("Delete") { _, _ ->
                    moodEntries.remove(moodEntry)
                    saveMoodEntries()
                    moodAdapter.removeEntry(moodEntry)
                    updateEmptyState()

                    // Update mood count in SharedPreferences
                    val currentMoodCount = sharedPreferences.getInt("moods_count", 0)
                    sharedPreferences.edit().putInt("moods_count", maxOf(0, currentMoodCount - 1)).apply()

                    showToast("Mood entry deleted")
                    Log.d(TAG, "Mood entry deleted: ${moodEntry.mood}")
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting mood entry: ${e.message}")
            showToast("Error deleting mood entry")
        }
    }

    // Apply date filter
    private fun applyDateFilter(startDate: Long, endDate: Long) {
        try {
            filteredMoodEntries.clear()
            filteredMoodEntries.addAll(moodEntries.filter { moodEntry ->
                moodEntry.date in startDate..endDate
            })
            isFiltered = true
            moodAdapter.updateEntries(filteredMoodEntries)
            updateEmptyState()
            Log.d(TAG, "Date filter applied: ${filteredMoodEntries.size} entries match the criteria")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying date filter: ${e.message}")
            showToast("Error applying date filter")
        }
    }

    // Clear date filter
    private fun clearDateFilter() {
        try {
            isFiltered = false
            filteredMoodEntries.clear()
            moodAdapter.updateEntries(moodEntries)
            updateEmptyState()
            Log.d(TAG, "Date filter cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing date filter: ${e.message}")
        }
    }

    // Share mood summary
    private fun shareMoodSummary() {
        try {
            if (moodEntries.isEmpty()) {
                showToast("No mood entries to share")
                return
            }

            val entriesToShare = if (isFiltered) filteredMoodEntries else moodEntries

            val summary = buildMoodSummary(entriesToShare)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "My Mood Journal Summary")
                putExtra(Intent.EXTRA_TEXT, summary)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Mood Summary"))
            Log.d(TAG, "Sharing mood summary")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing mood summary: ${e.message}")
            showToast("Error sharing mood summary")
        }
    }

    // Build mood summary text
    private fun buildMoodSummary(entries: List<MoodEntry>): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

        val summary = StringBuilder()
        summary.append("📊 My Mood Journal Summary\n\n")
        summary.append("Total Entries: ${entries.size}\n\n")

        // Group by mood type
        val moodCounts = entries.groupingBy { it.mood }.eachCount()
        moodCounts.forEach { (mood, count) ->
            val emoji = entries.firstOrNull { it.mood == mood }?.emoji ?: ""
            summary.append("$emoji $mood: $count\n")
        }

        summary.append("\nRecent Entries:\n")
        entries.take(5).forEach { entry ->
            summary.append("${dateFormat.format(java.util.Date(entry.date))}: ${entry.emoji} ${entry.mood}\n")
            if (entry.description.isNotEmpty() && entry.description != "No notes") {
                summary.append("   - ${entry.description}\n")
            }
        }

        // Add date range if filtered
        if (isFiltered) {
            summary.append("\n📅 Filtered Date Range")
        }

        summary.append("\n\nGenerated by Zen Wellora")
        return summary.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoodEntries() {
        try {
            moodEntries.clear()

            // Load real mood entries from SharedPreferences
            val moodEntriesJson = sharedPreferences.getString(MOOD_ENTRIES_KEY, "[]")
            if (!moodEntriesJson.isNullOrEmpty() && moodEntriesJson != "[]") {
                try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<MoodEntry>>() {}.type
                    val loadedEntries = gson.fromJson<List<MoodEntry>>(moodEntriesJson, type)
                    
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
                Log.d(TAG, "Mood journal is empty for new user - starting fresh")
            }

            // Update the adapter with the loaded entries
            moodAdapter.updateEntries(moodEntries)
            updateEmptyState()

            Log.d(TAG, "Mood entries loaded successfully: ${moodEntries.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading mood entries: ${e.message}")
            showToast("Error loading mood entries")
        }
    }

    private fun saveMoodEntries() {
        try {
            val gson = com.google.gson.Gson()
            val moodEntriesJson = gson.toJson(moodEntries)
            
            sharedPreferences.edit().apply {
                putString(MOOD_ENTRIES_KEY, moodEntriesJson)
                apply()
            }
            
            Log.d(TAG, "Mood entries saved to SharedPreferences: ${moodEntries.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entries: ${e.message}")
            showToast("Error saving mood entries")
        }
    }

    private fun updateEmptyState() {
        try {
            val entriesToShow = if (isFiltered) filteredMoodEntries else moodEntries

            if (entriesToShow.isEmpty()) {
                tvEmptyState.visibility = TextView.VISIBLE
                rvMoodEntries.visibility = RecyclerView.GONE
                if (isFiltered) {
                    tvEmptyState.text = "No mood entries found for the selected date range"
                } else {
                    tvEmptyState.text = "No mood entries yet. Add your first mood!"
                }
            } else {
                tvEmptyState.visibility = TextView.GONE
                rvMoodEntries.visibility = RecyclerView.VISIBLE
            }
            Log.d(TAG, "Empty state updated: ${entriesToShow.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating empty state: ${e.message}")
        }
    }

    private fun navigateBackToHome() {
        try {
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        navigateBackToHome()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Mood Journal Activity Resumed")
        // Refresh data when returning to mood journal
        updateEmptyState()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Mood Journal Activity Paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Mood Journal Activity Destroyed")
    }

    // Data class for Mood Entry
    data class MoodEntry(
        val id: Int,
        val mood: String,
        val description: String,
        val date: Long,
        val emoji: String
    ) : java.io.Serializable {
        // Helper method to check if two mood entries are the same based on ID
        fun isSameAs(other: MoodEntry): Boolean {
            return this.id == other.id
        }
    }

}