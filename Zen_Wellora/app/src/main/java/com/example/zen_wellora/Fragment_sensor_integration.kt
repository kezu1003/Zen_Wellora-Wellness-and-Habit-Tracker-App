package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Fragment_sensor_integration : AppCompatActivity(), SensorEventListener {

    // UI Components
    private lateinit var switchShakeDetection: Switch
    private lateinit var tvShakeStatus: TextView
    private lateinit var tvShakeCount: TextView
    private lateinit var btnMoodHappy: Button
    private lateinit var btnMoodNeutral: Button
    private lateinit var btnMoodSad: Button
    private lateinit var btnCalibrate: Button
    private lateinit var tvSensorData: TextView
    private lateinit var tvTotalShakes: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sensorManager: SensorManager

    // Permission handling
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "All sensor permissions granted")
            initializeSensors()
        } else {
            Log.w(TAG, "Some sensor permissions denied")
            showToast("Sensor permissions are required for step counting and shake detection")
            updatePermissionStatus()
        }
    }

    // Sensor variables
    private var accelerometerSensor: Sensor? = null
    private var shakeCount = 0
    private var lastShakeTime: Long = 0
    private var isShakeDetectionEnabled = false

    // Shake detection variables
    private val SHAKE_THRESHOLD = 8f // Reduced for better sensitivity
    private val SHAKE_SLOP_TIME_MS = 300 // Reduced for more responsive detection
    private val SHAKE_COUNT_RESET_TIME_MS = 3000
    private var shakeCalibration = 0f
    private var calibrationSamples = 0
    private val MAX_CALIBRATION_SAMPLES = 50

    companion object {
        private const val TAG = "SensorActivity"
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_SHAKE_COUNT = "shake_count"
        private const val KEY_TOTAL_SHAKES = "total_shakes"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_sensor_integration)

        Log.d(TAG, "Sensor Integration Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Initialize Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        initializeViews()
        setupClickListeners()
        loadSensorData()
        checkPermissionsAndInitializeSensors()
        resetDailyDataIfNeeded()
    }

    private fun initializeViews() {
        try {

            // Shake Detection Views
            switchShakeDetection = findViewById(R.id.switchShakeDetection)
            tvShakeStatus = findViewById(R.id.tvShakeStatus)
            tvShakeCount = findViewById(R.id.tvShakeCount)
            btnMoodHappy = findViewById(R.id.btnMoodHappy)
            btnMoodNeutral = findViewById(R.id.btnMoodNeutral)
            btnMoodSad = findViewById(R.id.btnMoodSad)

            // Sensor Control Views
            btnCalibrate = findViewById(R.id.btnCalibrate)
            tvSensorData = findViewById(R.id.tvSensorData)

            // Statistics Views
            tvTotalShakes = findViewById(R.id.tvTotalShakes)

            Log.d(TAG, "All sensor views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing sensor views: ${e.message}")
            showToast("Error initializing sensor page")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        try {

            // Shake Detection Toggle
            switchShakeDetection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && accelerometerSensor != null) {
                    isShakeDetectionEnabled = true
                    enableShakeDetection()
                    showToast("Shake detection enabled")
                } else {
                    isShakeDetectionEnabled = false
                    disableShakeDetection()
                    showToast("Shake detection disabled")
                }
                saveSensorSettings()
            }

            // Mood Buttons
            btnMoodHappy.setOnClickListener {
                setQuickMood("happy")
                showToast("Quick mood set to Happy 😊")
            }

            btnMoodNeutral.setOnClickListener {
                setQuickMood("neutral")
                showToast("Quick mood set to Neutral 😐")
            }

            btnMoodSad.setOnClickListener {
                setQuickMood("sad")
                showToast("Quick mood set to Sad 😢")
            }

            // Sensor Control Buttons
            btnCalibrate.setOnClickListener {
                calibrateShakeDetection()
                showToast("Calibrating shake detection...")
            }


            Log.d(TAG, "All sensor click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up sensor click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSensorData() {
        try {
            // Load shake detection data
            shakeCount = sharedPreferences.getInt(KEY_SHAKE_COUNT, 0)
            val totalShakes = sharedPreferences.getInt(KEY_TOTAL_SHAKES, 0)
            
            // Load settings
            val shakeDetectionEnabled = sharedPreferences.getBoolean("shake_detection_enabled", false)
            val quickMood = sharedPreferences.getString("quick_mood", "happy") ?: "happy"

            // Apply settings to UI
            switchShakeDetection.isChecked = shakeDetectionEnabled

            // Update shake detection data
            tvShakeCount.text = "Shakes today: $shakeCount"
            tvTotalShakes.text = formatNumber(totalShakes)

            // Set quick mood button states
            setQuickMoodButtonStates(quickMood)

            Log.d(TAG, "Sensor data loaded - Shakes: $shakeCount")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading sensor data: ${e.message}")
            showToast("Error loading sensor data")
        }
    }

    @SuppressLint("SetTextI18n")


    private fun calibrateShakeDetection() {
        try {
            // Reset calibration
            shakeCalibration = 0f
            calibrationSamples = 0
            
            runOnUiThread {
                tvShakeStatus.text = "🔄 Calibrating... Hold device still"
            }
            
            Log.d(TAG, "Shake detection calibration started")
        } catch (e: Exception) {
            Log.e(TAG, "Error calibrating shake detection: ${e.message}")
        }
    }



    private fun enableShakeDetection() {
        try {
            if (accelerometerSensor == null) {
                showToast("Accelerometer sensor not available on this device")
                switchShakeDetection.isChecked = false
                return
            }
            
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
            tvShakeStatus.text = "🎯 Ready to shake!"
            Log.d(TAG, "Accelerometer sensor registered for shake detection")
            showToast("Shake detection started - shake your device to test!")
            
            // Start calibration
            calibrateShakeDetection()
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling shake detection: ${e.message}")
            showToast("Error enabling shake detection")
            switchShakeDetection.isChecked = false
        }
    }

    private fun disableShakeDetection() {
        try {
            accelerometerSensor?.let {
                sensorManager.unregisterListener(this, it)
            }
            tvShakeStatus.text = "👋 Shake me!"
            Log.d(TAG, "Accelerometer sensor unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling shake detection: ${e.message}")
        }
    }

    private fun setQuickMood(mood: String) {
        try {
            sharedPreferences.edit().putString("quick_mood", mood).apply()
            setQuickMoodButtonStates(mood)
            Log.d(TAG, "Quick mood set to: $mood")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting quick mood: ${e.message}")
        }
    }

    private fun setQuickMoodButtonStates(selectedMood: String) {
        try {
            // Reset all buttons
            btnMoodHappy.alpha = 0.6f
            btnMoodNeutral.alpha = 0.6f
            btnMoodSad.alpha = 0.6f

            // Highlight selected mood
            when (selectedMood) {
                "happy" -> btnMoodHappy.alpha = 1.0f
                "neutral" -> btnMoodNeutral.alpha = 1.0f
                "sad" -> btnMoodSad.alpha = 1.0f
            }
            Log.d(TAG, "Mood button states updated for: $selectedMood")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting mood button states: ${e.message}")
        }
    }

    private fun calibrateSensors() {
        try {
            // TODO: Implement actual sensor calibration
            showToast("Sensors calibrated successfully!")
            sharedPreferences.edit().putBoolean("sensors_calibrated", true).apply()
            Log.d(TAG, "Sensors calibrated")
        } catch (e: Exception) {
            Log.e(TAG, "Error calibrating sensors: ${e.message}")
            showToast("Error calibrating sensors")
        }
    }


    private fun checkAvailableSensors() {
        try {
            // Check available sensors and update UI accordingly
            val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
            Log.d(TAG, "Available sensors: ${sensorList.size}")

            // Log available sensor types
            sensorList.forEach { sensor ->
                Log.d(TAG, "Sensor: ${sensor.name}, Type: ${sensor.type}")
            }

            // Check permissions
            val hasActivityRecognition = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            val hasBodySensors = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED

            runOnUiThread {
                // Update shake detection status
                if (accelerometerSensor == null) {
                    switchShakeDetection.isEnabled = false
                    switchShakeDetection.text = "Shake Detection (Not Available)"
                } else {
                    switchShakeDetection.isEnabled = true
                    switchShakeDetection.text = "Shake Detection (Available)"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking available sensors: ${e.message}")
        }
    }

    private fun saveSensorSettings() {
        try {
            sharedPreferences.edit()
                .putBoolean("shake_detection_enabled", switchShakeDetection.isChecked)
                .apply()

            Log.d(TAG, "Sensor settings saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sensor settings: ${e.message}")
        }
    }


    private fun saveShakeData(totalShakes: Int) {
        try {
            sharedPreferences.edit()
                .putInt(KEY_SHAKE_COUNT, shakeCount)
                .putInt(KEY_TOTAL_SHAKES, totalShakes)
                .apply()

            Log.d(TAG, "Shake data saved: $shakeCount shakes today")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving shake data: ${e.message}")
        }
    }

    private fun checkPermissionsAndInitializeSensors() {
        try {
            val permissions = mutableListOf<String>()
            
            // Check for required permissions
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
            
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.BODY_SENSORS)
            }

            if (permissions.isNotEmpty()) {
                Log.d(TAG, "Requesting sensor permissions: $permissions")
                permissionLauncher.launch(permissions.toTypedArray())
            } else {
                Log.d(TAG, "All sensor permissions already granted")
                initializeSensors()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions: ${e.message}")
            showToast("Error checking sensor permissions")
        }
    }

    private fun initializeSensors() {
        try {
            // Get accelerometer sensor
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            // Log sensor information
            accelerometerSensor?.let { sensor ->
                Log.d(TAG, "Accelerometer Sensor: ${sensor.name}, Type: ${sensor.type}")
            }

            // Update UI based on sensor availability
            updateSensorStatus()

            Log.d(TAG, "Sensors initialized - Accelerometer: ${accelerometerSensor != null}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing sensors: ${e.message}")
            showToast("Error initializing sensors")
        }
    }

    private fun updateSensorStatus() {
        try {
            runOnUiThread {
                // Update shake detection status
                if (accelerometerSensor != null) {
                    switchShakeDetection.isEnabled = true
                    switchShakeDetection.text = "Shake Detection (Available)"
                } else {
                    switchShakeDetection.isEnabled = false
                    switchShakeDetection.text = "Shake Detection (Not Available)"
                    showToast("Accelerometer sensor not available on this device")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating sensor status: ${e.message}")
        }
    }

    private fun updatePermissionStatus() {
        try {
            runOnUiThread {
                val hasActivityRecognition = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
                val hasBodySensors = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
                
                if (!hasActivityRecognition || !hasBodySensors) {
                    showToast("Please grant sensor permissions in Settings to use step counter and shake detection")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating permission status: ${e.message}")
        }
    }

    private fun resetDailyDataIfNeeded() {
        try {
            val lastResetDate = sharedPreferences.getLong(KEY_LAST_RESET_DATE, 0)
            val currentDate = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()

            // Reset if it's a new day
            calendar.timeInMillis = lastResetDate
            val lastResetDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            calendar.timeInMillis = currentDate
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)

            if (lastResetDay != currentDay) {
                shakeCount = 0

                sharedPreferences.edit()
                    .putInt(KEY_SHAKE_COUNT, 0)
                    .putLong(KEY_LAST_RESET_DATE, currentDate)
                    .apply()

                runOnUiThread {
                    tvShakeCount.text = "Shakes today: 0"
                }

                Log.d(TAG, "Daily sensor data reset")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting daily data: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    handleAccelerometerEvent(sensorEvent)
                }
            }
        }
    }


    private fun handleAccelerometerEvent(event: SensorEvent) {
        try {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Update sensor data display
            runOnUiThread {
                tvSensorData.text = "X: ${"%.2f".format(x)}, Y: ${"%.2f".format(y)}, Z: ${"%.2f".format(z)}"
                tvSensorData.visibility = TextView.VISIBLE
            }

            // Calculate acceleration magnitude
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()


            // Shake detection
            if (isShakeDetectionEnabled) {
                // Calibration phase - collect baseline readings
                if (calibrationSamples < MAX_CALIBRATION_SAMPLES) {
                    shakeCalibration += acceleration
                    calibrationSamples++
                    if (calibrationSamples == MAX_CALIBRATION_SAMPLES) {
                        shakeCalibration /= MAX_CALIBRATION_SAMPLES
                        Log.d(TAG, "Shake calibration completed: $shakeCalibration")
                    }
                    return
                }

                // Improved shake detection with dynamic calibration
                val netAcceleration = Math.abs(acceleration - shakeCalibration)
                
                // Shake detection with improved threshold
                if (netAcceleration > SHAKE_THRESHOLD) {
                val currentTime = System.currentTimeMillis()

                // Ignore shakes too close together
                if (currentTime - lastShakeTime > SHAKE_SLOP_TIME_MS) {
                    lastShakeTime = currentTime
                    shakeCount++

                    runOnUiThread {
                        handleShakeDetected()
                    }

                        Log.d(TAG, "Shake detected! Net acceleration: $netAcceleration, Total shakes: $shakeCount")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling accelerometer event: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleShakeDetected() {
        try {
            // Update shake count display
            tvShakeCount.text = "Shakes today: $shakeCount"

            // Update total shakes
            val totalShakes = sharedPreferences.getInt(KEY_TOTAL_SHAKES, 0) + 1
            tvTotalShakes.text = formatNumber(totalShakes)

            // Save shake data
            saveShakeData(totalShakes)

            // Visual feedback
            tvShakeStatus.text = "📱 Shake detected!"

            // Get selected mood and log it
            val quickMood = sharedPreferences.getString("quick_mood", "happy") ?: "happy"
            logMoodFromShake(quickMood)

            // Reset status after delay
            tvShakeStatus.postDelayed({
                tvShakeStatus.text = "🎯 Ready to shake!"
            }, 1000)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling shake: ${e.message}")
        }
    }

    private fun logMoodFromShake(mood: String) {
        try {
            val moodMessage = when (mood) {
                "happy" -> "Mood logged: Happy 😊"
                "neutral" -> "Mood logged: Neutral 😐"
                "sad" -> "Mood logged: Sad 😢"
                else -> "Mood logged"
            }
            showToast(moodMessage)
            Log.d(TAG, "Mood logged via shake: $mood")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging mood from shake: ${e.message}")
        }
    }


    // Helper methods
    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Sensor Integration Activity Resumed")

        // Check permissions and re-register sensors if they were enabled
        checkPermissionsAndInitializeSensors()

        // Re-register sensors based on current settings
        if (switchShakeDetection.isChecked && isShakeDetectionEnabled) {
            enableShakeDetection()
        }

        loadSensorData()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Sensor Integration Activity Paused")

        // Unregister all sensors to save battery
        sensorManager.unregisterListener(this)

        // Save current data
        saveShakeData(sharedPreferences.getInt(KEY_TOTAL_SHAKES, 0))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Sensor Integration Activity Destroyed")
    }
}