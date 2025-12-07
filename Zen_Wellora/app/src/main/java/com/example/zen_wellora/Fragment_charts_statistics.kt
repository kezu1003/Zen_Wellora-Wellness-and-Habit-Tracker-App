package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class Fragment_charts_statistics : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var btnWeek: Button
    private lateinit var btnMonth: Button
    private lateinit var btnQuarter: Button
    private lateinit var spinnerMoodChartType: Spinner
    private lateinit var tvAvgMood: TextView
    private lateinit var tvBestMood: TextView
    private lateinit var tvMoodConsistency: TextView
    private lateinit var tvCompletionRate: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvTotalCompleted: TextView
    private lateinit var btnExportData: Button
    private lateinit var btnShareInsights: Button

    // Mood Progress Bars
    private lateinit var progressMon: ProgressBar
    private lateinit var progressTue: ProgressBar
    private lateinit var progressWed: ProgressBar
    private lateinit var progressThu: ProgressBar
    private lateinit var progressFri: ProgressBar

    // Mood Score TextViews
    private lateinit var tvMonScore: TextView
    private lateinit var tvTueScore: TextView
    private lateinit var tvWedScore: TextView
    private lateinit var tvThuScore: TextView
    private lateinit var tvFriScore: TextView

    // Habit Progress Bars
    private lateinit var progressMeditation: ProgressBar
    private lateinit var progressExercise: ProgressBar
    private lateinit var progressReading: ProgressBar

    // Habit Percentage TextViews
    private lateinit var tvMeditationPercent: TextView
    private lateinit var tvExercisePercent: TextView
    private lateinit var tvReadingPercent: TextView

    // Weekly Progress
    private lateinit var progressWeeklyOverall: ProgressBar
    private lateinit var tvWeeklyCompletion: TextView
    private lateinit var tvWeeklyStreak: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson

    // Data classes for analytics
    data class MoodEntry(
        val id: Int,
        val mood: String,
        val description: String,
        val date: Long,
        val emoji: String
    ) : java.io.Serializable

    data class Habit(
        val id: Long,
        val name: String,
        val description: String,
        val icon: String,
        val category: String,
        var completed: Boolean,
        val createdAt: Long,
        var completedDates: MutableList<Long>
    )

    data class AnalyticsData(
        val moodEntries: List<MoodEntry>,
        val habits: List<Habit>,
        val timeRange: String
    )

    companion object {
        private const val TAG = "AnalyticsActivity"
        private const val PREFS_NAME = "user_prefs"
        private const val MOOD_ENTRIES_KEY = "mood_entries"
        private const val HABITS_KEY = "user_habits"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_charts_statistics)

        Log.d(TAG, "Analytics Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences and Gson
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        gson = Gson()

        initializeViews()
        setupSpinner()
        setupClickListeners()
        loadAnalyticsData()
    }

    private fun initializeViews() {
        try {
            // Back arrow
            ivBack = findViewById(R.id.iv_back)
            
            // Time range buttons
            btnWeek = findViewById(R.id.btnWeek)
            btnMonth = findViewById(R.id.btnMonth)
            btnQuarter = findViewById(R.id.btnQuarter)

            // Mood chart controls
            spinnerMoodChartType = findViewById(R.id.spinnerMoodChartType)

            // Mood statistics
            tvAvgMood = findViewById(R.id.tvAvgMood)
            tvBestMood = findViewById(R.id.tvBestMood)
            tvMoodConsistency = findViewById(R.id.tvMoodConsistency)

            // Habit statistics
            tvCompletionRate = findViewById(R.id.tvCompletionRate)
            tvCurrentStreak = findViewById(R.id.tvCurrentStreak)
            tvTotalCompleted = findViewById(R.id.tvTotalCompleted)

            // Action buttons
            btnExportData = findViewById(R.id.btnExportData)
            btnShareInsights = findViewById(R.id.btnShareInsights)

            // Mood Progress Bars
            progressMon = findViewById(R.id.progressMon)
            progressTue = findViewById(R.id.progressTue)
            progressWed = findViewById(R.id.progressWed)
            progressThu = findViewById(R.id.progressThu)
            progressFri = findViewById(R.id.progressFri)

            // Mood Score TextViews
            tvMonScore = findViewById(R.id.tvMonScore)
            tvTueScore = findViewById(R.id.tvTueScore)
            tvWedScore = findViewById(R.id.tvWedScore)
            tvThuScore = findViewById(R.id.tvThuScore)
            tvFriScore = findViewById(R.id.tvFriScore)

            // Habit Progress Bars
            progressMeditation = findViewById(R.id.progressMeditation)
            progressExercise = findViewById(R.id.progressExercise)
            progressReading = findViewById(R.id.progressReading)

            // Habit Percentage TextViews
            tvMeditationPercent = findViewById(R.id.tvMeditationPercent)
            tvExercisePercent = findViewById(R.id.tvExercisePercent)
            tvReadingPercent = findViewById(R.id.tvReadingPercent)

            // Weekly Progress
            progressWeeklyOverall = findViewById(R.id.progressWeeklyOverall)
            tvWeeklyCompletion = findViewById(R.id.tvWeeklyCompletion)
            tvWeeklyStreak = findViewById(R.id.tvWeeklyStreak)

            // REMOVED: Placeholder text view references since they no longer exist in XML
            // The placeholders were replaced with actual progress bars

            Log.d(TAG, "All analytics views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing analytics views: ${e.message}")
            showToast("Error initializing analytics page")
        }
    }

    private fun setupSpinner() {
        val chartTypes = arrayOf("Weekly View", "Monthly View", "Progress Summary")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chartTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMoodChartType.adapter = adapter

        spinnerMoodChartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> showWeeklyView()
                    1 -> showMonthlyView()
                    2 -> showProgressSummary()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showWeeklyView() {
        // Update mood progress bars with weekly data (scale 1-10)
        updateMoodProgressBarsWithScores(listOf(7, 8, 6, 9, 7))
        updateMoodScoreTexts(listOf(7, 8, 6, 9, 7))

        // Update habit progress for weekly view
        updateHabitProgress(listOf(85, 70, 90))
        updateHabitPercentTexts(listOf(85, 70, 90))

        // Update weekly progress
        updateWeeklyProgress(78, 12)

        showToast("Showing Weekly View")
    }

    private fun showMonthlyView() {
        // Update mood progress bars with monthly data (averages)
        updateMoodProgressBarsWithScores(listOf(7, 7, 7, 8, 7))
        updateMoodScoreTexts(listOf(7, 7, 7, 8, 7))

        // Update habit progress for monthly view
        updateHabitProgress(listOf(82, 75, 88))
        updateHabitPercentTexts(listOf(82, 75, 88))

        // Update weekly progress
        updateWeeklyProgress(82, 15)

        showToast("Showing Monthly View")
    }

    private fun showProgressSummary() {
        // Update mood progress bars with progress summary data
        updateMoodProgressBarsWithScores(listOf(8, 8, 7, 9, 8))
        updateMoodScoreTexts(listOf(8, 8, 7, 9, 8))

        // Update habit progress for progress summary
        updateHabitProgress(listOf(90, 85, 95))
        updateHabitPercentTexts(listOf(90, 85, 95))

        // Update weekly progress
        updateWeeklyProgress(90, 18)

        showToast("Showing Progress Summary")
    }

    private fun updateMoodProgressBarsWithScores(moodScores: List<Int>) {
        val progressBars = listOf(progressMon, progressTue, progressWed, progressThu, progressFri)

        moodScores.forEachIndexed { index, score ->
            if (index < progressBars.size) {
                progressBars[index].progress = score

                // Update progress bar color based on mood score
                val color = when {
                    score >= 8 -> R.color.success
                    score >= 6 -> R.color.accent
                    else -> R.color.warning
                }
                progressBars[index].progressTintList = ContextCompat.getColorStateList(this, color)
            }
        }
    }

    private fun updateMoodScoreTexts(moodScores: List<Int>) {
        val scoreTexts = listOf(tvMonScore, tvTueScore, tvWedScore, tvThuScore, tvFriScore)

        moodScores.forEachIndexed { index, score ->
            if (index < scoreTexts.size) {
                scoreTexts[index].text = "$score/10"

                // Update text color based on mood score
                val color = when {
                    score >= 8 -> R.color.success
                    score >= 6 -> R.color.accent
                    else -> R.color.warning
                }
                scoreTexts[index].setTextColor(ContextCompat.getColor(this, color))
            }
        }
    }

    private fun updateHabitProgress(percentages: List<Int>) {
        val habitBars = listOf(progressMeditation, progressExercise, progressReading)

        percentages.forEachIndexed { index, percent ->
            if (index < habitBars.size) {
                habitBars[index].progress = percent

                // Update progress bar color based on completion percentage
                val color = when {
                    percent >= 80 -> R.color.success
                    percent >= 60 -> R.color.accent
                    else -> R.color.warning
                }
                habitBars[index].progressTintList = ContextCompat.getColorStateList(this, color)
            }
        }
    }

    private fun updateHabitPercentTexts(percentages: List<Int>) {
        val percentTexts = listOf(tvMeditationPercent, tvExercisePercent, tvReadingPercent)

        percentages.forEachIndexed { index, percent ->
            if (index < percentTexts.size) {
                percentTexts[index].text = "$percent%"

                // Update text color based on completion percentage
                val color = when {
                    percent >= 80 -> R.color.success
                    percent >= 60 -> R.color.accent
                    else -> R.color.warning
                }
                percentTexts[index].setTextColor(ContextCompat.getColor(this, color))
            }
        }
    }

    private fun updateWeeklyProgress(completion: Int, streak: Int) {
        progressWeeklyOverall.progress = completion
        tvWeeklyCompletion.text = "$completion%"
        tvWeeklyStreak.text = "🔥 Current Streak: $streak days"

        // Update color based on completion
        val color = when {
            completion >= 80 -> R.color.success
            completion >= 60 -> R.color.accent
            else -> R.color.warning
        }
        progressWeeklyOverall.progressTintList = ContextCompat.getColorStateList(this, color)
        tvWeeklyCompletion.setTextColor(ContextCompat.getColor(this, color))
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        try {
            // Back arrow
            ivBack.setOnClickListener {
                Log.d(TAG, "Back arrow clicked")
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Time range buttons
            btnWeek.setOnClickListener {
                setActiveTimeRangeButton(btnWeek)
                showToast("Showing weekly data")
                loadTimeRangeData("week")
            }

            btnMonth.setOnClickListener {
                setActiveTimeRangeButton(btnMonth)
                showToast("Showing monthly data")
                loadTimeRangeData("month")
            }

            btnQuarter.setOnClickListener {
                setActiveTimeRangeButton(btnQuarter)
                showToast("Showing quarterly data")
                loadTimeRangeData("quarter")
            }

            // Export data button
            btnExportData.setOnClickListener {
                showToast("Export feature coming soon!")
            }

            // Share insights button
            btnShareInsights.setOnClickListener {
                showToast("Share insights feature coming soon!")
            }

            Log.d(TAG, "All analytics click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up analytics click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun setActiveTimeRangeButton(activeButton: Button) {
        try {
            // Reset all buttons to inactive state
            val buttons = listOf(btnWeek, btnMonth, btnQuarter)
            buttons.forEach { button ->
                button.setBackgroundResource(R.drawable.button_rounded_small)
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_light)
                button.setTextColor(ContextCompat.getColor(this, R.color.primary))
            }

            // Set active button state
            activeButton.setBackgroundResource(R.drawable.button_rounded_small)
            activeButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.accent)
            activeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        } catch (e: Exception) {
            Log.e(TAG, "Error setting active time range button: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAnalyticsData() {
        try {
            Log.d(TAG, "Loading real analytics data from mood and habit pages...")
            
            // Load real data from mood journal and habit tracker
            val moodEntries = loadMoodEntries()
            val habits = loadHabits()
            
            // Calculate real analytics
            val analyticsData = calculateAnalytics(moodEntries, habits, "week")
            
            // Update UI with real data
            updateAnalyticsUI(analyticsData)

            // Load initial chart data
            showWeeklyView()

            // Set initial active time range
            setActiveTimeRangeButton(btnWeek)

            Log.d(TAG, "Real analytics data loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading analytics data: ${e.message}")
            showToast("Error loading analytics data")
        }
    }

    private fun loadTimeRangeData(timeRange: String) {
        try {
            Log.d(TAG, "Loading analytics data for time range: $timeRange")
            
            // Load real data from mood journal and habit tracker
            val moodEntries = loadMoodEntries()
            val habits = loadHabits()
            
            // Calculate real analytics for the selected time range
            val analyticsData = calculateAnalytics(moodEntries, habits, timeRange)
            
            // Update UI with real data
            updateAnalyticsUI(analyticsData)
            
            // Show appropriate view based on time range
            when (timeRange) {
                "week" -> showWeeklyView()
                "month" -> showMonthlyView()
                "quarter" -> showProgressSummary()
            }
            
            Log.d(TAG, "Time range data loaded successfully: $timeRange")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading time range data: ${e.message}")
            showToast("Error loading analytics data")
        }
    }

    // ==================== REAL ANALYTICS METHODS ====================
    
    private fun loadMoodEntries(): List<MoodEntry> {
        return try {
            val moodEntriesJson = sharedPreferences.getString(MOOD_ENTRIES_KEY, "[]")
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(moodEntriesJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading mood entries: ${e.message}")
            emptyList()
        }
    }
    
    private fun loadHabits(): List<Habit> {
        return try {
            val habitsJson = sharedPreferences.getString(HABITS_KEY, "[]")
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(habitsJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading habits: ${e.message}")
            emptyList()
        }
    }
    
    private fun calculateAnalytics(moodEntries: List<MoodEntry>, habits: List<Habit>, timeRange: String): AnalyticsData {
        val currentTime = System.currentTimeMillis()
        val daysBack = when (timeRange) {
            "week" -> 7
            "month" -> 30
            "quarter" -> 90
            else -> 7
        }
        val startTime = currentTime - (daysBack * 24 * 60 * 60 * 1000L)
        
        // Filter data by time range
        val filteredMoods = moodEntries.filter { it.date >= startTime }
        val filteredHabits = habits.filter { it.createdAt >= startTime }
        
        return AnalyticsData(filteredMoods, filteredHabits, timeRange)
    }
    
    private fun updateAnalyticsUI(analyticsData: AnalyticsData) {
        try {
            // Update mood statistics
            updateMoodStatistics(analyticsData.moodEntries)
            
            // Update habit statistics
            updateHabitStatistics(analyticsData.habits)
            
            // Update progress bars and charts
            updateProgressBars(analyticsData)
            
            // Update weekly progress
            updateWeeklyProgress(analyticsData)
            
            Log.d(TAG, "Analytics UI updated with real data")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating analytics UI: ${e.message}")
        }
    }
    
    private fun updateMoodStatistics(moodEntries: List<MoodEntry>) {
        try {
            if (moodEntries.isEmpty()) {
                tvAvgMood.text = "😊"
                tvBestMood.text = "😄"
                tvMoodConsistency.text = "0%"
                return
            }
            
            // Calculate average mood
            val moodScores = moodEntries.map { getMoodScore(it.mood) }
            val avgScore = moodScores.average()
            tvAvgMood.text = getMoodEmoji(avgScore)
            
            // Find best mood
            val bestMood = moodEntries.maxByOrNull { getMoodScore(it.mood) }?.mood ?: "😊"
            tvBestMood.text = getMoodEmoji(getMoodScore(bestMood))
            
            // Calculate mood consistency (how consistent the mood scores are)
            val consistency = calculateMoodConsistency(moodScores)
            tvMoodConsistency.text = "${consistency}%"
            
            Log.d(TAG, "Mood statistics updated: avg=${avgScore}, best=$bestMood, consistency=$consistency%")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating mood statistics: ${e.message}")
        }
    }
    
    private fun updateHabitStatistics(habits: List<Habit>) {
        try {
            if (habits.isEmpty()) {
                tvCompletionRate.text = "0%"
                tvCurrentStreak.text = "0"
                tvTotalCompleted.text = "0"
                return
            }
            
            // Calculate completion rate
            val totalHabits = habits.size
            val completedToday = habits.count { it.completed }
            val completionRate = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0
            tvCompletionRate.text = "$completionRate%"
            
            // Calculate current streak
            val currentStreak = calculateCurrentStreak(habits)
            tvCurrentStreak.text = currentStreak.toString()
            
            // Calculate total completed this week
            val weekStart = getWeekStart()
            val totalCompleted = habits.sumOf { habit ->
                habit.completedDates.count { it >= weekStart }
            }
            tvTotalCompleted.text = totalCompleted.toString()
            
            Log.d(TAG, "Habit statistics updated: completion=$completionRate%, streak=$currentStreak, total=$totalCompleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating habit statistics: ${e.message}")
        }
    }
    
    private fun updateProgressBars(analyticsData: AnalyticsData) {
        try {
            // Update mood progress bars for each day of the week
            updateMoodProgressBars(analyticsData.moodEntries)
            
            // Update habit progress bars
            updateHabitProgressBars(analyticsData.habits)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress bars: ${e.message}")
        }
    }
    
    private fun updateMoodProgressBars(moodEntries: List<MoodEntry>) {
        try {
            val weekStart = getWeekStart()
            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
            val progressBars = listOf(progressMon, progressTue, progressWed, progressThu, progressFri)
            val scoreTexts = listOf(tvMonScore, tvTueScore, tvWedScore, tvThuScore, tvFriScore)
            
            for (i in 0..4) {
                val dayStart = weekStart + (i * 24 * 60 * 60 * 1000L)
                val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
                
                val dayMoods = moodEntries.filter { it.date >= dayStart && it.date < dayEnd }
                val avgMoodScore = if (dayMoods.isNotEmpty()) {
                    dayMoods.map { getMoodScore(it.mood) }.average()
                } else {
                    3.0 // Neutral mood if no data
                }
                
                val progress = (avgMoodScore * 20).roundToInt() // Convert to 0-100 scale
                progressBars[i].progress = progress
                scoreTexts[i].text = String.format("%.1f", avgMoodScore)
                
                // Set color based on mood score
                val color = getMoodColor(avgMoodScore)
                progressBars[i].progressTintList = ContextCompat.getColorStateList(this, color)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating mood progress bars: ${e.message}")
        }
    }
    
    private fun updateHabitProgressBars(habits: List<Habit>) {
        try {
            val weekStart = getWeekStart()
            val habitCategories = habits.groupBy { it.category }
            
            // Update meditation progress
            val meditationHabits = habitCategories["Meditation"] ?: emptyList()
            val meditationProgress = calculateHabitProgress(meditationHabits, weekStart)
            progressMeditation.progress = meditationProgress
            tvMeditationPercent.text = "$meditationProgress%"
            
            // Update exercise progress
            val exerciseHabits = habitCategories["Exercise"] ?: emptyList()
            val exerciseProgress = calculateHabitProgress(exerciseHabits, weekStart)
            progressExercise.progress = exerciseProgress
            tvExercisePercent.text = "$exerciseProgress%"
            
            // Update reading progress
            val readingHabits = habitCategories["Reading"] ?: emptyList()
            val readingProgress = calculateHabitProgress(readingHabits, weekStart)
            progressReading.progress = readingProgress
            tvReadingPercent.text = "$readingProgress%"
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating habit progress bars: ${e.message}")
        }
    }
    
    private fun updateWeeklyProgress(analyticsData: AnalyticsData) {
        try {
            val weekStart = getWeekStart()
            val totalHabits = analyticsData.habits.size
            val completedThisWeek = analyticsData.habits.sumOf { habit ->
                habit.completedDates.count { it >= weekStart }
            }
            
            val weeklyCompletion = if (totalHabits > 0) {
                (completedThisWeek * 100) / (totalHabits * 7) // 7 days in a week
            } else {
                0
            }
            
            progressWeeklyOverall.progress = weeklyCompletion
            tvWeeklyCompletion.text = "$weeklyCompletion%"
            
            // Calculate weekly streak
            val weeklyStreak = calculateWeeklyStreak(analyticsData.habits)
            tvWeeklyStreak.text = weeklyStreak.toString()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating weekly progress: ${e.message}")
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private fun getMoodScore(mood: String): Double {
        return when (mood.lowercase()) {
            "excellent", "amazing", "fantastic" -> 5.0
            "good", "great", "happy" -> 4.0
            "okay", "fine", "neutral" -> 3.0
            "bad", "sad", "down" -> 2.0
            "terrible", "awful", "depressed" -> 1.0
            else -> 3.0
        }
    }
    
    private fun getMoodEmoji(score: Double): String {
        return when {
            score >= 4.5 -> "😄"
            score >= 3.5 -> "😊"
            score >= 2.5 -> "😐"
            score >= 1.5 -> "😔"
            else -> "😢"
        }
    }
    
    private fun getMoodColor(score: Double): Int {
        return when {
            score >= 4.0 -> R.color.success
            score >= 3.0 -> R.color.warning
            else -> R.color.error
        }
    }
    
    private fun calculateMoodConsistency(scores: List<Double>): Int {
        if (scores.isEmpty()) return 0
        val avg = scores.average()
        val variance = scores.map { (it - avg) * (it - avg) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        return (100 - (standardDeviation * 20)).roundToInt().coerceIn(0, 100)
    }
    
    private fun calculateCurrentStreak(habits: List<Habit>): Int {
        if (habits.isEmpty()) return 0
        
        val today = getStartOfDay()
        var streak = 0
        var currentDay = today
        
        while (true) {
            val dayStart = currentDay
            val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
            
            val completedToday = habits.any { habit ->
                habit.completedDates.any { it >= dayStart && it < dayEnd }
            }
            
            if (completedToday) {
                streak++
                currentDay -= (24 * 60 * 60 * 1000L)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateWeeklyStreak(habits: List<Habit>): Int {
        if (habits.isEmpty()) return 0
        
        var streak = 0
        var currentWeek = getWeekStart()
        
        while (true) {
            val weekEnd = currentWeek + (7 * 24 * 60 * 60 * 1000L)
            val completedThisWeek = habits.any { habit ->
                habit.completedDates.any { it >= currentWeek && it < weekEnd }
            }
            
            if (completedThisWeek) {
                streak++
                currentWeek -= (7 * 24 * 60 * 60 * 1000L)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateHabitProgress(habits: List<Habit>, weekStart: Long): Int {
        if (habits.isEmpty()) return 0
        
        val totalPossible = habits.size * 7 // 7 days in a week
        val completed = habits.sumOf { habit ->
            habit.completedDates.count { it >= weekStart }
        }
        
        return if (totalPossible > 0) (completed * 100) / totalPossible else 0
    }
    
    private fun getWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Analytics Activity Resumed")
        loadAnalyticsData()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Analytics Activity Started")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Analytics Activity Paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Analytics Activity Destroyed")
    }
}