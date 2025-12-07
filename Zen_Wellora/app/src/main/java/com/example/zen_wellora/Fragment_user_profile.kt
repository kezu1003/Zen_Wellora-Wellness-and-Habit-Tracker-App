package com.example.zen_wellora

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import java.io.File
import java.io.FileOutputStream

class Fragment_user_profile : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var ivCoverPhoto: ImageView
    private lateinit var btnEditPhoto: ImageButton
    private lateinit var btnEditCover: ImageButton
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button

    private lateinit var tvUserName: TextView
    private lateinit var tvUserBio: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var tvHabitsCount: TextView
    private lateinit var tvMoodsCount: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvWellnessGoals: TextView
    private lateinit var tvWaterGoal: TextView
    private lateinit var tvNotificationPrefs: TextView
    private lateinit var tvJoinDate: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private var selectedImageUri: Uri? = null

    companion object {
        private const val TAG = "UserProfileActivity"
        private const val PREFS_NAME = "user_prefs"
        const val REQUEST_EDIT_PROFILE = 1001
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val imageUri = data.data
                if (imageUri != null) {
                    selectedImageUri = imageUri
                    loadImageFromUri(imageUri)
                }
            }
        }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoFile = File(getExternalFilesDir(null), "profile_photo.jpg")
            if (photoFile.exists()) {
                val imageUri = Uri.fromFile(photoFile)
                selectedImageUri = imageUri
                loadImageFromUri(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_user_profile)

        Log.d(TAG, "User Profile Activity Created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        initializeViews()
        setupClickListeners()
        setupBackPressedHandler()
        loadUserProfileData()
    }

    private fun initializeViews() {
        try {
            // Back arrow
            ivBack = findViewById(R.id.iv_back)
            
            // Profile and cover photos
            ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
            ivCoverPhoto = findViewById(R.id.ivCoverPhoto)
            btnEditPhoto = findViewById(R.id.btnEditPhoto)
            btnEditCover = findViewById(R.id.btnEditCover)

            // Buttons
            btnEditProfile = findViewById(R.id.btnEditProfile)
            btnLogout = findViewById(R.id.btnLogout)

            // Text views
            tvUserName = findViewById(R.id.tvUserName)
            tvUserBio = findViewById(R.id.tvUserBio)
            tvStreakCount = findViewById(R.id.tvStreakCount)
            tvHabitsCount = findViewById(R.id.tvHabitsCount)
            tvMoodsCount = findViewById(R.id.tvMoodsCount)
            tvEmail = findViewById(R.id.tvEmail)
            tvWellnessGoals = findViewById(R.id.tvWellnessGoals)
            tvWaterGoal = findViewById(R.id.tvWaterGoal)
            tvNotificationPrefs = findViewById(R.id.tvNotificationPrefs)
            tvJoinDate = findViewById(R.id.tvJoinDate)

            Log.d(TAG, "All profile views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing profile views: ${e.message}")
            showToast("Error initializing profile")
        }
    }

    private fun setupBackPressedHandler() {
        // Modern way to handle back button/gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToHome()
            }
        })
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
            
            // Edit profile button - Navigate to Edit Profile
            btnEditProfile.setOnClickListener {
                Log.d(TAG, "Edit profile clicked")
                navigateToEditProfile()
            }

            // Edit photo buttons
            btnEditPhoto.setOnClickListener {
                Log.d(TAG, "Edit profile photo clicked")
                showImageSourceDialog()
            }

            btnEditCover.setOnClickListener {
                Log.d(TAG, "Edit cover photo clicked")
                showToast("Change cover photo feature coming soon!")
            }


            // Logout button
            btnLogout.setOnClickListener {
                Log.d(TAG, "Logout clicked")
                showLogoutConfirmationDialog()
            }

            // Back navigation on profile photo click (optional)
            ivProfilePhoto.setOnClickListener {
                navigateBackToHome()
            }

            Log.d(TAG, "All profile click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up profile click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun navigateToEditProfile() {
        try {
            val intent = Intent(this, Dialog_edit_profile::class.java)

            // Pass current user data to edit profile activity
            intent.putExtra("current_name", tvUserName.text.toString())
            intent.putExtra("current_bio", tvUserBio.text.toString())
            intent.putExtra("current_goals", tvWellnessGoals.text.toString())

            startActivityForResult(intent, REQUEST_EDIT_PROFILE)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "Navigated to Edit Profile page")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to edit profile: ${e.message}")
            showToast("Error opening edit profile")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserProfileData() {
        try {
            // Get user data from intent extras first, then SharedPreferences
            val intentUserName = intent.getStringExtra("user_name")
            val intentUserEmail = intent.getStringExtra("user_email")
            
            val userName = intentUserName ?: sharedPreferences.getString("user_name", "Sarah") ?: "Sarah"
            val userEmail = intentUserEmail ?: sharedPreferences.getString("user_email", "sarah@example.com") ?: "sarah@example.com"
            val userBio = sharedPreferences.getString("user_bio",
                "Wellness enthusiast on a journey to better health and mindfulness. Love yoga, meditation, and staying hydrated!")
                ?: "Wellness enthusiast on a journey to better health and mindfulness. Love yoga, meditation, and staying hydrated!"

            val wellnessGoals = sharedPreferences.getString("wellness_goals", "Meditation, Hydration, Exercise")
                ?: "Meditation, Hydration, Exercise"
            val waterGoal = sharedPreferences.getString("water_goal", "8 glasses per day") ?: "8 glasses per day"

            // Load stats with default values
            val streakCount = sharedPreferences.getInt("streak_count", 12)
            val habitsCount = sharedPreferences.getInt("habits_count", 6)
            val moodsCount = sharedPreferences.getInt("moods_count", 45)

            val joinDate = sharedPreferences.getString("join_date", "January 2024") ?: "January 2024"
            val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)

            // Update UI with loaded data
            tvUserName.text = userName
            tvUserBio.text = userBio
            tvEmail.text = userEmail
            tvWellnessGoals.text = wellnessGoals
            tvWaterGoal.text = waterGoal
            tvStreakCount.text = streakCount.toString()
            tvHabitsCount.text = habitsCount.toString()
            tvMoodsCount.text = moodsCount.toString()
            tvJoinDate.text = "Member since $joinDate"

            // Set notification preferences text
            tvNotificationPrefs.text = if (notificationsEnabled) {
                "Enabled for all reminders"
            } else {
                "Disabled"
            }

            // Set notification text color based on preference
            val notificationColor = if (notificationsEnabled) {
                ContextCompat.getColor(this, R.color.success)
            } else {
                ContextCompat.getColor(this, R.color.error)
            }
            tvNotificationPrefs.setTextColor(notificationColor)

            // Load profile image
            loadProfileImage()

            Log.d(TAG, "User profile data loaded successfully: $userName, Streak: $streakCount")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user profile data: ${e.message}")
            showToast("Error loading profile data")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EDIT_PROFILE) {
            if (resultCode == RESULT_OK) {
                // Profile was updated, refresh the data
                loadUserProfileData()
                showToast("Profile updated successfully!")
                Log.d(TAG, "Profile updated and refreshed")
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Edit profile was canceled")
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        try {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to logout?")

            builder.setPositiveButton("Yes, Logout") { _, _ ->
                performLogout()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()

            // Customize button colors
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.error)
            )
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.primary)
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout dialog: ${e.message}")
            performLogout() // Fallback to direct logout
        }
    }

    private fun performLogout() {
        try {
            // Clear user data from shared preferences using KTX extension
            sharedPreferences.edit {
                clear()
            }

            // Navigate to login screen
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()

            // Use proper animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            Log.d(TAG, "User logged out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
            showToast("Error during logout. Please try again.")
        }
    }

    private fun saveSampleUserData() {
        // This function can be used to save sample data for testing using KTX extension
        try {
            sharedPreferences.edit {
                putString("user_name", "Sarah")
                putString("user_email", "sarah@example.com")
                putString("user_bio", "Wellness enthusiast on a journey to better health and mindfulness. Love yoga, meditation, and staying hydrated!")
                putString("wellness_goals", "Meditation, Hydration, Exercise")
                putString("water_goal", "8 glasses per day")
                putInt("streak_count", 12)
                putInt("habits_count", 6)
                putInt("moods_count", 45)
                putString("join_date", "January 2024")
                putBoolean("notifications_enabled", true)
            }

            Log.d(TAG, "Sample user data saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sample data: ${e.message}")
        }
    }

    private fun showImageSourceDialog() {
        try {
            val options = arrayOf("Camera", "Gallery")
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Select Image Source")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            builder.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing image source dialog: ${e.message}")
            showToast("Error opening image picker")
        }
    }

    private fun openCamera() {
        try {
            val photoFile = File(getExternalFilesDir(null), "profile_photo.jpg")
            val photoUri = Uri.fromFile(photoFile)
            
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(cameraIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
            showToast("Error opening camera")
        }
    }

    private fun openGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(galleryIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}")
            showToast("Error opening gallery")
        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                ivProfilePhoto.setImageBitmap(bitmap)
                saveProfileImage(bitmap)
                showToast("Profile photo updated!")
                Log.d(TAG, "Profile photo loaded successfully")
            } else {
                showToast("Error loading image")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URI: ${e.message}")
            showToast("Error loading image")
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        try {
            // Save image to internal storage
            val imageFile = File(getExternalFilesDir(null), "profile_photo.jpg")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Save image path to SharedPreferences
            sharedPreferences.edit {
                putString("profile_photo_path", imageFile.absolutePath)
            }
            
            Log.d(TAG, "Profile photo saved: ${imageFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile image: ${e.message}")
        }
    }

    private fun loadProfileImage() {
        try {
            val imagePath = sharedPreferences.getString("profile_photo_path", null)
            if (imagePath != null) {
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    if (bitmap != null) {
                        ivProfilePhoto.setImageBitmap(bitmap)
                        Log.d(TAG, "Profile photo loaded from storage")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}")
        }
    }

    private fun navigateBackToHome() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "User Profile Activity Resumed")
        // Refresh data when returning to profile
        loadUserProfileData()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    @Deprecated(
        "Deprecated in Android",
        ReplaceWith("onBackPressedDispatcher", "androidx.activity.OnBackPressedDispatcher")
    )
    override fun onBackPressed() {
        // This method is deprecated but we're marking it as deprecated too
        // The modern approach is handled by onBackPressedDispatcher above
        navigateBackToHome()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "User Profile Activity Started")

        // Check if we have user data, if not save sample data
        if (!sharedPreferences.contains("user_name")) {
            saveSampleUserData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "User Profile Activity Destroyed")
    }
}