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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import java.io.File
import java.io.FileOutputStream

class Dialog_edit_profile : AppCompatActivity() {

    // UI Components
    private lateinit var ivEditProfilePhoto: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etBio: EditText
    private lateinit var etWellnessGoals: EditText
    private lateinit var btnCancelEdit: Button
    private lateinit var btnSaveProfile: Button

    private lateinit var sharedPreferences: SharedPreferences
    private var selectedImageUri: Uri? = null

    companion object {
        private const val TAG = "EditProfileActivity"
        private const val PREFS_NAME = "user_prefs"
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
        setContentView(R.layout.activity_dialog_edit_profile)

        Log.d(TAG, "Edit Profile Activity Created")

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
        loadCurrentUserData()
    }

    private fun initializeViews() {
        try {
            // Profile photo
            ivEditProfilePhoto = findViewById(R.id.ivEditProfilePhoto)
            btnChangePhoto = findViewById(R.id.btnChangePhoto)

            // EditText fields
            etFullName = findViewById(R.id.etFullName)
            etEmail = findViewById(R.id.etEmail)
            etBio = findViewById(R.id.etBio)
            etWellnessGoals = findViewById(R.id.etWellnessGoals)

            // Buttons
            btnCancelEdit = findViewById(R.id.btnCancelEdit)
            btnSaveProfile = findViewById(R.id.btnSaveProfile)

            Log.d(TAG, "All edit profile views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing edit profile views: ${e.message}")
            showToast("Error initializing edit profile")
        }
    }

    private fun setupBackPressedHandler() {
        // Modern way to handle back button/gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelEditAndReturn()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        try {
            // Change photo button
            btnChangePhoto.setOnClickListener {
                Log.d(TAG, "Change photo clicked")
                showImageSourceDialog()
            }

            // Cancel button
            btnCancelEdit.setOnClickListener {
                Log.d(TAG, "Cancel edit clicked")
                cancelEditAndReturn()
            }

            // Save profile button
            btnSaveProfile.setOnClickListener {
                Log.d(TAG, "Save profile clicked")
                saveProfileChanges()
            }

            Log.d(TAG, "All edit profile click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up edit profile click listeners: ${e.message}")
            showToast("Error setting up click listeners")
        }
    }

    private fun loadCurrentUserData() {
        try {
            // Get current data from intent extras or SharedPreferences
            val currentName = intent.getStringExtra("current_name") ?:
            sharedPreferences.getString("user_name", "") ?: ""
            val currentEmail = intent.getStringExtra("current_email") ?:
            sharedPreferences.getString("user_email", "") ?: ""
            val currentBio = intent.getStringExtra("current_bio") ?:
            sharedPreferences.getString("user_bio", "") ?: ""
            val currentGoals = intent.getStringExtra("current_goals") ?:
            sharedPreferences.getString("wellness_goals", "") ?: ""

            // Set current data in EditText fields
            etFullName.setText(currentName)
            etEmail.setText(currentEmail)
            etBio.setText(currentBio)
            etWellnessGoals.setText(currentGoals)

            Log.d(TAG, "Current user data loaded into edit fields: $currentName, $currentEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading current user data: ${e.message}")
        }
    }

    private fun saveProfileChanges() {
        try {
            val newName = etFullName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val newBio = etBio.text.toString().trim()
            val newGoals = etWellnessGoals.text.toString().trim()

            // Validate inputs
            if (newName.isEmpty()) {
                etFullName.error = "Please enter your name"
                etFullName.requestFocus()
                return
            }

            if (newEmail.isEmpty()) {
                etEmail.error = "Please enter your email"
                etEmail.requestFocus()
                return
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return
            }

            if (newBio.isEmpty()) {
                etBio.error = "Please enter your bio"
                etBio.requestFocus()
                return
            }

            if (newGoals.isEmpty()) {
                etWellnessGoals.error = "Please enter your wellness goals"
                etWellnessGoals.requestFocus()
                return
            }

            // Save profile image if selected
            if (selectedImageUri != null) {
                saveProfileImage()
            }

            // Save to SharedPreferences using KTX extension
            sharedPreferences.edit {
                putString("user_name", newName)
                putString("user_email", newEmail)
                putString("user_bio", newBio)
                putString("wellness_goals", newGoals)
            }

            Log.d(TAG, "Profile changes saved: Name=$newName, Email=$newEmail")

            // Return to profile with success result
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile changes: ${e.message}")
            showToast("Error saving profile changes")
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
                ivEditProfilePhoto.setImageBitmap(bitmap)
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

    private fun saveProfileImage() {
        try {
            selectedImageUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
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
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile image: ${e.message}")
        }
    }

    private fun cancelEditAndReturn() {
        setResult(RESULT_CANCELED)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    @Deprecated(
        "Deprecated in Android",
        ReplaceWith("onBackPressedDispatcher", "androidx.activity.OnBackPressedDispatcher")
    )
    override fun onBackPressed() {
        // This method is deprecated but we're marking it as deprecated too
        // The modern approach is handled by onBackPressedDispatcher above
        cancelEditAndReturn()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Edit Profile Activity Resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Edit Profile Activity Destroyed")
    }
}