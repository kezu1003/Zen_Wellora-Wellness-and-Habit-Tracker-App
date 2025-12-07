package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Home : AppCompatActivity() {

    // UI Components
    private lateinit var ivProfile: ImageView
    private lateinit var ivSettings: ImageView
    private lateinit var ivNotifications: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvCompletionPercentage: TextView
    private lateinit var tvHabitsCount: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var tvCompletionDate: TextView
    private lateinit var cvHabits: CardView
    private lateinit var cvMood: CardView
    private lateinit var cvHydration: CardView
    private lateinit var cvAnalytics: CardView
    private lateinit var cvWidget: CardView
    private lateinit var cvSensor: CardView
    private lateinit var tvViewAll: TextView
    private lateinit var rvRecentActivities: RecyclerView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var recentActivityManager: RecentActivityManager
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var habitManager: HabitManager
    private lateinit var gson: Gson

    companion object {
        private const val TAG = "HomeActivity"
        private const val PREFS_NAME = "user_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved theme before setting content view
        applySavedTheme()
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        Log.d(TAG, "Home Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // Initialize HabitManager and Gson
        habitManager = HabitManager(this)
        gson = Gson()
        
        // Initialize notification permission helper
        notificationPermissionHelper = NotificationPermissionHelper(this)
        
            // Initialize notification helper
            notificationHelper = NotificationHelper(this)
            
            // Initialize recent activity manager
            recentActivityManager = RecentActivityManager(this)
            
            // Check if opened from notification and stop alarm sound if needed
            handleNotificationClick()

            initializeViews()
            setupClickListeners()
            loadUserDataFromIntent()
            updateProgressData()
            setupRecentActivities()
        
        // Request notification permissions
        requestNotificationPermissions()
    }

    private fun initializeViews() {
        try {
            // Profile and header views
            ivProfile = findViewById(R.id.iv_profile)
            ivSettings = findViewById(R.id.iv_settings)
            ivNotifications = findViewById(R.id.iv_notifications)
            tvUserName = findViewById(R.id.tv_user_name)

            // Progress views
            tvCompletionPercentage = findViewById(R.id.tv_completion_percentage)
            tvHabitsCount = findViewById(R.id.tv_habits_count)
            tvStreakCount = findViewById(R.id.tv_streak_count)
            tvCompletionDate = findViewById(R.id.tv_completion_date)

            // Quick action cards
            cvHabits = findViewById(R.id.cv_habits)
            cvMood = findViewById(R.id.cv_mood)
            cvHydration = findViewById(R.id.cv_hydration)
            cvAnalytics = findViewById(R.id.cv_analytics)
            cvWidget = findViewById(R.id.cv_widget)
            cvSensor = findViewById(R.id.cv_sensor)

            // Other interactive elements
            tvViewAll = findViewById(R.id.tv_view_all)
            
            // Recent Activities RecyclerView
            rvRecentActivities = findViewById(R.id.rv_recent_activities)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
        }
    }

    private fun setupClickListeners() {
        try {
            // Profile image click - Navigate to Profile
            ivProfile.setOnClickListener {
                Log.d(TAG, "Profile image clicked")
                navigateToProfile()
            }

            // Settings icon click - Navigate to Settings
            ivSettings.setOnClickListener {
                Log.d(TAG, "Settings clicked")
                navigateToSettings()
            }

            // Notifications icon click - Navigate to Notification Management
            ivNotifications.setOnClickListener {
                Log.d(TAG, "Notifications clicked")
                navigateToNotificationManagement()
            }

            // Quick Action Cards
            cvHabits.setOnClickListener {
                Log.d(TAG, "Habits card clicked")
                navigateToHabitTracker()
            }

            cvMood.setOnClickListener {
                Log.d(TAG, "Mood journal card clicked")
                navigateToMoodJournal()
            }

            cvHydration.setOnClickListener {
                Log.d(TAG, "Hydration card clicked")
                navigateToHydrationReminder()
            }

            cvAnalytics.setOnClickListener {
                Log.d(TAG, "Analytics card clicked")
                navigateToAnalytics()
            }

            // Widget card navigates to Widget page
            cvWidget.setOnClickListener {
                Log.d(TAG, "Widget card clicked")
                navigateToWidget()
            }

            // UPDATED: Sensor card now navigates to Sensor page
            cvSensor.setOnClickListener {
                Log.d(TAG, "Sensor card clicked")
                navigateToSensor()
            }

            // View All recent activities
            tvViewAll.setOnClickListener {
                Log.d(TAG, "View All clicked")
                refreshRecentActivities()
                showToast("Recent activities refreshed!")
            }

            Log.d(TAG, "All click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
        }
    }
    
    private fun setupRecentActivities() {
        try {
            // Add sample activities if none exist
            if (recentActivityManager.getActivities().isEmpty()) {
                recentActivityManager.addSampleActivities()
            }
            
            // Setup RecyclerView
            recentActivityAdapter = RecentActivityAdapter(
                recentActivityManager.getActivities()
            ) { activity ->
                // Handle activity click
                Log.d(TAG, "Activity clicked: ${activity.title}")
                showToast("Activity: ${activity.title}")
            }
            
            rvRecentActivities.apply {
                layoutManager = LinearLayoutManager(this@Home)
                adapter = recentActivityAdapter
            }
            
            Log.d(TAG, "Recent activities setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up recent activities: ${e.message}")
        }
    }
    
    private fun refreshRecentActivities() {
        try {
            // Update adapter with latest activities
            recentActivityAdapter = RecentActivityAdapter(
                recentActivityManager.getActivities()
            ) { activity ->
                Log.d(TAG, "Activity clicked: ${activity.title}")
                showToast("Activity: ${activity.title}")
            }
            
            rvRecentActivities.adapter = recentActivityAdapter
            Log.d(TAG, "Recent activities refreshed")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing recent activities: ${e.message}")
        }
    }
    
    private fun handleNotificationClick() {
        try {
            // Check if activity was opened from a notification
            val source = intent.getStringExtra("source")
            Log.d(TAG, "Activity opened with source: $source")
            
            when (source) {
                "hydration_notification" -> {
                    Log.d(TAG, "Opened from hydration notification - stopping alarm sound via singleton")
                    AlarmSoundManager.stopAlarmSound()
                    showToast("💧 Time to hydrate! Drink some water.")
                }
                "habit_notification" -> {
                    Log.d(TAG, "Opened from habit notification - stopping alarm sound via singleton")
                    AlarmSoundManager.stopAlarmSound()
                    showToast("✅ Check your habit progress!")
                }
                "test_notification" -> {
                    Log.d(TAG, "Opened from test notification - stopping alarm sound via singleton")
                    AlarmSoundManager.stopAlarmSound()
                    showToast("🔔 Test notification clicked!")
                }
                else -> {
                    // App opened normally, still stop any playing alarms just in case
                    if (source != null) {
                        Log.d(TAG, "Unknown notification source: $source - stopping alarm as precaution")
                        AlarmSoundManager.stopAlarmSound()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling notification click: ${e.message}")
        }
    }

    // NEW METHOD: Navigate to Sensor page
    private fun navigateToSensor() {
        try {
            val intent = Intent(this, Fragment_sensor_integration::class.java)

            // Pass current user data to sensor
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            // Pass progress data for sensor integration
            intent.putExtra("completion_percentage", tvCompletionPercentage.text.toString())
            intent.putExtra("habits_completed", sharedPreferences.getInt("habits_completed_today", 6))
            intent.putExtra("total_habits", sharedPreferences.getInt("total_habits", 8))
            intent.putExtra("streak_count", sharedPreferences.getInt("streak_count", 12))

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Sensor page successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to sensor: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            showToast("Sensor feature is currently unavailable")
        }
    }

    private fun navigateToWidget() {
        try {
            val intent = Intent(this, Widget_configure::class.java)

            // Pass current user data to widget
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            // Pass progress data for widget configuration
            intent.putExtra("completion_percentage", tvCompletionPercentage.text.toString())
            intent.putExtra("habits_completed", sharedPreferences.getInt("habits_completed_today", 6))
            intent.putExtra("total_habits", sharedPreferences.getInt("total_habits", 8))
            intent.putExtra("streak_count", sharedPreferences.getInt("streak_count", 12))

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Widget page successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to widget: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            showToast("Widget feature is currently unavailable")
        }
    }

    private fun navigateToAnalytics() {
        try {
            val intent = Intent(this, Fragment_charts_statistics::class.java)

            // Pass current user data to analytics
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            // Pass progress data for analytics
            intent.putExtra("completion_percentage", tvCompletionPercentage.text.toString())
            intent.putExtra("habits_completed", sharedPreferences.getInt("habits_completed_today", 6))
            intent.putExtra("total_habits", sharedPreferences.getInt("total_habits", 8))
            intent.putExtra("streak_count", sharedPreferences.getInt("streak_count", 12))

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Analytics page successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to analytics: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            showToast("Analytics feature is currently unavailable")
        }
    }

    private fun navigateToHydrationReminder() {
        try {
            val intent = Intent(this, Fragment_hydration_reminder::class.java)

            // Pass current user data to hydration reminder
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Hydration Reminder page successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to hydration reminder: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            showToast("Hydration feature is currently unavailable")
        }
    }

    private fun navigateToMoodJournal() {
        try {
            val intent = Intent(this, Fragment_mood_journal::class.java)

            // Pass current user data to mood journal
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Mood Journal page")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to mood journal: ${e.message}")
            showToast("Error opening mood journal")
        }
    }

    private fun navigateToHabitTracker() {
        try {
            val intent = Intent(this, DailyHabitTracker::class.java)

            // Pass current progress data to habit tracker
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Habit Tracker page")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to habit tracker: ${e.message}")
            showToast("Error opening habit tracker")
        }
    }

    private fun navigateToProfile() {
        try {
            val intent = Intent(this, Fragment_user_profile::class.java)

            // Pass current user data to profile activity
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Profile page")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to profile: ${e.message}")
            showToast("Error opening profile")
        }
    }

    private fun navigateToSettings() {
        try {
            val intent = Intent(this, Fragment_settings::class.java)

            // Pass current settings if needed
            val userName = tvUserName.text.toString()
            if (userName.isNotEmpty() && userName != "Sarah") {
                intent.putExtra("user_name", userName)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Settings page")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to settings: ${e.message}")
            showToast("Error opening settings")
        }
    }

    private fun navigateToNotificationManagement() {
        try {
            val intent = Intent(this, Fragment_notification_management::class.java)

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Notification Management page")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to notification management: ${e.message}")
            showToast("Error opening notification settings")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserDataFromIntent() {
        try {
            // Get user data from intent extras first
            val intentUserName = intent.getStringExtra("user_name")
            val intentUserEmail = intent.getStringExtra("user_email")
            
            if (!intentUserName.isNullOrEmpty()) {
                // Use data from intent (fresh login)
                tvUserName.text = intentUserName
                
                // Store in SharedPreferences for persistence
                val editor = sharedPreferences.edit()
                editor.putString("user_name", intentUserName)
                if (!intentUserEmail.isNullOrEmpty()) {
                    editor.putString("user_email", intentUserEmail)
                }
                editor.apply()
                
                Log.d(TAG, "User data loaded from intent: $intentUserName")
            } else {
                // Fallback to SharedPreferences
                loadUserData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data from intent: ${e.message}")
            loadUserData()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserData() {
        try {
            // Load user data from SharedPreferences
            val userName = sharedPreferences.getString("user_name", "Sarah") ?: "Sarah"
            tvUserName.text = userName

            Log.d(TAG, "User data loaded successfully: $userName")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressData() {
        try {
            // Load habits from storage
            habitManager.loadHabitsFromStorage()
            
            // Get real progress data from HabitManager
            val (habitsCompleted, totalHabits) = habitManager.getTodayProgress()
            val completionPercentage = habitManager.getTodayCompletionPercentage()
            val bestStreak = habitManager.getBestStreak()
            
            // Calculate current streak from habit data
            val currentStreak = calculateCurrentStreak()

            // Update progress indicators with real data
            tvCompletionPercentage.text = "$completionPercentage%"
            tvHabitsCount.text = "$habitsCompleted/$totalHabits"
            tvStreakCount.text = "$currentStreak days"
            tvCompletionDate.text = "Today"

            Log.d(TAG, "Real progress data updated: $habitsCompleted/$totalHabits ($completionPercentage%), Streak: $currentStreak days")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress data: ${e.message}")
            // Fallback to default values if there's an error
            tvCompletionPercentage.text = "0%"
            tvHabitsCount.text = "0/0"
            tvStreakCount.text = "0 days"
            tvCompletionDate.text = "Today"
        }
    }

    private fun calculateCurrentStreak(): Int {
        try {
            val habits = habitManager.getAllHabits()
            if (habits.isEmpty()) return 0
            
            val today = getStartOfDay()
            var streak = 0
            var currentDate = today
            
            while (true) {
                val dayBefore = currentDate - (24 * 60 * 60 * 1000L)
                val completedToday = habits.all { habit ->
                    habit.completedDates.contains(currentDate)
                }
                
                if (completedToday) {
                    streak++
                    currentDate = dayBefore
                } else {
                    break
                }
            }
            
            return streak
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating current streak: ${e.message}")
            return 0
        }
    }
    
    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    @Suppress("UNUSED")
    private fun saveSampleProgressData() {
        // Save sample progress data to SharedPreferences
        try {
            sharedPreferences.edit {
                putInt("streak_count", 12)
                putInt("habits_completed_today", 6)
                putInt("total_habits", 8)
                putInt("moods_count", 45)
                putBoolean("notifications_enabled", true)
            }

            Log.d(TAG, "Sample progress data saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sample progress data: ${e.message}")
        }
    }

    private fun requestNotificationPermissions() {
        try {
            Log.d(TAG, "Requesting notification permissions")
            val hasPermission = notificationPermissionHelper.checkAndRequestNotificationPermission(this)
            
            if (hasPermission) {
                Log.d(TAG, "Notification permissions already granted")
            } else {
                Log.d(TAG, "Notification permission request sent")
            }
            
            // Check battery optimization
            if (notificationPermissionHelper.checkBatteryOptimization()) {
                Log.w(TAG, "Battery optimization is enabled - notifications may be delayed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting notification permissions: ${e.message}")
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Notification permission granted")
                    showToast("Notifications enabled! You'll receive hydration reminders.")
                } else {
                    Log.w(TAG, "Notification permission denied")
                    showToast("Notification permission denied. You can enable it in settings.")
                }
            }
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

    override fun onResume() {
        super.onResume()
        // Refresh progress data when returning to home page
        updateProgressData()
        // Refresh recent activities when returning to home
        refreshRecentActivities()
        updateWidget()
        Log.d(TAG, "Home Activity Resumed - All data refreshed")
    }

    private fun applySavedTheme() {
        try {
            val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val savedTheme = sharedPreferences.getString("app_theme", "system") ?: "system"
            
            val nightMode = when (savedTheme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            AppCompatDelegate.setDefaultNightMode(nightMode)
            
            Log.d(TAG, "Applied saved theme: $savedTheme (night mode: $nightMode)")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying saved theme: ${e.message}")
            // Fallback to system theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Home Activity Started")

        // Check if we have progress data, if not save sample data
        if (!sharedPreferences.contains("streak_count")) {
            saveSampleProgressData()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Home Activity Paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Home Activity Destroyed")
    }
}