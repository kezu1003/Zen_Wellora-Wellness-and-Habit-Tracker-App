package com.example.zen_wellora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Signup : AppCompatActivity() {

    // UI Components
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignUp: AppCompatButton
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SignupActivity", "onCreate started")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            setContentView(R.layout.activity_signup)
            Log.d("SignupActivity", "Content view set successfully")

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Initialize views
            initViews()

            // Set up click listeners
            setupClickListeners()

            // Check if social provider was passed from login
            handleSocialProviderIntent()

            Log.d("SignupActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("SignupActivity", "Error in onCreate: ", e)
            Toast.makeText(this, "Error loading signup page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        try {
            // Find EditTexts directly
            etUsername = findViewById(R.id.usernameEditText)
            etEmail = findViewById(R.id.emailEditText)
            etPassword = findViewById(R.id.passwordEditText)
            etConfirmPassword = findViewById(R.id.confirmPasswordEditText)
            btnSignUp = findViewById(R.id.signUpButton)
            tvLogin = findViewById(R.id.loginTextView)

            // Get parent TextInputLayouts
            tilUsername = etUsername.parent.parent as TextInputLayout
            tilEmail = etEmail.parent.parent as TextInputLayout
            tilPassword = etPassword.parent.parent as TextInputLayout
            tilConfirmPassword = etConfirmPassword.parent.parent as TextInputLayout

            Log.d("SignupActivity", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("SignupActivity", "Error initializing views: ", e)
        }
    }

    private fun setupClickListeners() {
        try {
            // Sign up button - Handle registration
            btnSignUp.setOnClickListener {
                Log.d("SignupActivity", "Sign up button clicked")
                handleSignUp()
            }

            // Login link - Navigate back to Login page
            tvLogin.setOnClickListener {
                Log.d("SignupActivity", "Login link clicked")
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
                // Use standard navigation without animation
                startActivity(intent)
                finish()
            }

            Log.d("SignupActivity", "All click listeners set up successfully")
        } catch (e: Exception) {
            Log.e("SignupActivity", "Error setting up click listeners: ", e)
        }
    }

    private fun handleSignUp() {
        try {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Clear previous errors
            clearErrors()

            // Validate inputs
            if (!validateInputs(username, email, password, confirmPassword)) {
                return
            }

            // Demo registration - Replace with your actual registration logic
            if (performRegistration(username, email, password)) {
                Log.d("SignupActivity", "Registration successful, navigating to login")
                Toast.makeText(this, "Registration successful! Please login to continue", Toast.LENGTH_SHORT).show()

                // Clear form data
                clearFormData()

                // Navigate to Login page
                val intent = Intent(this, Login::class.java)
                intent.putExtra("newUser", true)
                intent.putExtra("username", username)
                intent.putExtra("email", email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_SHORT).show()
                tilEmail.error = "Email already exists"
            }

        } catch (e: Exception) {
            Log.e("SignupActivity", "Error in handleSignUp: ", e)
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Validate username
        if (username.isEmpty()) {
            tilUsername.error = "Username is required"
            etUsername.requestFocus()
            isValid = false
        } else if (username.length < 3) {
            tilUsername.error = "Username must be at least 3 characters"
            etUsername.requestFocus()
            isValid = false
        }

        // Validate email
        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            if (isValid) etEmail.requestFocus()
            isValid = false
        } else if (!isValidEmail(email)) {
            tilEmail.error = "Please enter a valid email address"
            if (isValid) etEmail.requestFocus()
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            if (isValid) etPassword.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            if (isValid) etPassword.requestFocus()
            isValid = false
        } else if (!isStrongPassword(password)) {
            tilPassword.error = "Password must contain at least one letter and one number"
            if (isValid) etPassword.requestFocus()
            isValid = false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Please confirm your password"
            if (isValid) etConfirmPassword.requestFocus()
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = "Passwords do not match"
            if (isValid) etConfirmPassword.requestFocus()
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun clearErrors() {
        tilUsername.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
    }

    private fun clearFormData() {
        etUsername.text?.clear()
        etEmail.text?.clear()
        etPassword.text?.clear()
        etConfirmPassword.text?.clear()
        clearErrors()
    }

    private fun handleSocialProviderIntent() {
        try {
            val socialProvider = intent.getStringExtra("socialProvider")
            if (!socialProvider.isNullOrEmpty()) {
                Log.d("SignupActivity", "Social provider received: $socialProvider")
                Toast.makeText(this, "Sign up with $socialProvider account", Toast.LENGTH_SHORT).show()

                // Pre-fill some fields or show social signup options
                // You can customize this based on your needs
            }
        } catch (e: Exception) {
            Log.e("SignupActivity", "Error handling social provider intent: ", e)
        }
    }

    // Helper method to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Helper method to check password strength
    private fun isStrongPassword(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    // Demo registration method - Replace with your actual registration logic
    private fun performRegistration(username: String, email: String, password: String): Boolean {
        try {
            // Simulate registration process
            // In a real app, you would make an API call to your backend

            // Demo validation - reject common test emails
            val blockedEmails = listOf("test@test.com", "admin@admin.com", "user@user.com")
            if (email.lowercase() in blockedEmails) {
                return false
            }

            // Store user data in SharedPreferences for demo purposes
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("registered_username", username)
            editor.putString("registered_email", email)
            editor.putString("registered_password", password)
            editor.putBoolean("user_registered", true)
            editor.putLong("registration_timestamp", System.currentTimeMillis())
            editor.apply()

            Log.d("SignupActivity", "User data stored: $username, $email")
            
            // Simulate network delay
            Thread.sleep(500)
            return true
        } catch (e: Exception) {
            Log.e("SignupActivity", "Error storing user data: ${e.message}")
            return false
        }
    }
}