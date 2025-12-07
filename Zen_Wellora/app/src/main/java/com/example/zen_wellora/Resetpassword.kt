package com.example.zen_wellora

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class Resetpassword : AppCompatActivity() {

    // UI Components
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnResetPassword: AppCompatButton
    private lateinit var tvBackToLogin: TextView
    private lateinit var tvSignUp: TextView

    private val TAG = "ResetPasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate started")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            setContentView(R.layout.activity_resetpassword)
            Log.d(TAG, "Content view set successfully")

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Initialize views
            initViews()

            // Set up click listeners
            setupClickListeners()

            // Setup modern back pressed handler
            setupBackPressedHandler()

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ", e)
            Toast.makeText(this, "Error loading reset password page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        try {
            // Find views by ID
            etEmail = findViewById(R.id.emailEditText)
            btnResetPassword = findViewById(R.id.resetPasswordButton)
            tvBackToLogin = findViewById(R.id.backToLoginText)
            tvSignUp = findViewById(R.id.signUpText)

            // Get parent TextInputLayout for email
            tilEmail = etEmail.parent?.parent as? TextInputLayout
                ?: throw IllegalStateException("TextInputLayout not found for email field")

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ", e)
            Toast.makeText(this, "Error initializing UI components", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        try {
            // Reset password button - Handle password reset
            btnResetPassword.setOnClickListener {
                Log.d(TAG, "Reset password button clicked")
                handlePasswordReset()
            }

            // Back to login link - Navigate to Login page
            tvBackToLogin.setOnClickListener {
                Log.d(TAG, "Back to login link clicked")
                navigateToLogin()
            }

            // Sign up link - Navigate to Sign Up page
            tvSignUp.setOnClickListener {
                Log.d(TAG, "Sign up link clicked")
                navigateToSignUp()
            }

            Log.d(TAG, "All click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ", e)
            Toast.makeText(this, "Error setting up click listeners", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBackPressedHandler() {
        // Modern way to handle back button/gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToLogin()
            }
        })
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, Login::class.java)
            // Add flags to clear the back stack and start fresh
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            // Use proper animation resources
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to login: ", e)
            Toast.makeText(this, "Error navigating to login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToSignUp() {
        try {
            val intent = Intent(this, Signup::class.java)
            // Add flags to clear the back stack and start fresh
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            // Use proper animation resources
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to sign up: ", e)
            Toast.makeText(this, "Error navigating to sign up", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handlePasswordReset() {
        try {
            val email = etEmail.text.toString().trim()

            // Clear previous errors
            tilEmail.error = null
            tilEmail.isErrorEnabled = false

            // Validate email input
            if (!validateEmail(email)) {
                return
            }

            // Show loading state
            btnResetPassword.text = "Sending Reset Link..."
            btnResetPassword.isEnabled = false
            btnResetPassword.alpha = 0.7f

            // Simulate password reset API call (for demo purposes)
            simulatePasswordResetApiCall(email)

        } catch (e: Exception) {
            Log.e(TAG, "Error in handlePasswordReset: ", e)
            Toast.makeText(this, "Password reset failed. Please try again.", Toast.LENGTH_SHORT).show()
            resetButtonState()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun simulatePasswordResetApiCall(email: String) {
        // Simulate network delay
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                // Check if email exists in our "database" (for demo purposes)
                if (isEmailRegistered(email)) {
                    // Success case - email is registered
                    showSuccessDialog(email)
                    Log.d(TAG, "Reset email sent to: $email")
                } else {
                    // Email not registered
                    showEmailNotRegisteredDialog(email)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in API simulation: ", e)
                Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show()
            } finally {
                resetButtonState()
            }
        }, 2000) // 2 second delay to simulate network call
    }

    private fun resetButtonState() {
        btnResetPassword.text = "RESET PASSWORD"
        btnResetPassword.isEnabled = true
        btnResetPassword.alpha = 1.0f
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                tilEmail.error = "Email address is required"
                etEmail.requestFocus()
                showToast("Please enter your email address")
                false
            }
            !isValidEmail(email) -> {
                tilEmail.error = "Please enter a valid email address"
                etEmail.requestFocus()
                showToast("Please enter a valid email address")
                false
            }
            else -> true
        }
    }

    private fun showSuccessDialog(email: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Reset Email Sent")
        builder.setMessage("We've sent password reset instructions to:\n\n$email\n\nPlease check your inbox and follow the instructions to reset your password.")

        builder.setPositiveButton("Go to Login") { _, _ ->
            navigateToLogin()
        }

        builder.setNeutralButton("Send Again") { _, _ ->
            // Resend the reset email
            handlePasswordReset()
        }

        builder.setNegativeButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Customize dialog appearance
        val dialog = builder.create()
        dialog.show()

        // Customize button colors using ContextCompat
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.accent)
        )
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.setTextColor(
            ContextCompat.getColor(this, R.color.primary)
        )
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.text_secondary)
        )
    }

    private fun showEmailNotRegisteredDialog(email: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Email Not Found")
        builder.setMessage("The email address '$email' is not registered with Zen Wellora.\n\nWould you like to create a new account?")

        builder.setPositiveButton("Sign Up") { _, _ ->
            navigateToSignUp()
        }

        builder.setNegativeButton("Try Different Email") { dialog, _ ->
            dialog.dismiss()
            etEmail.requestFocus()
            etEmail.selectAll()
        }

        val dialog = builder.create()
        dialog.show()

        // Customize button colors using ContextCompat
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.accent)
        )
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.primary)
        )
    }

    // Demo function to check if email is registered
    // In a real app, this would call your backend API
    private fun isEmailRegistered(email: String): Boolean {
        // For demo purposes, consider these emails as registered
        val registeredEmails = listOf(
            "user@example.com",
            "test@zenwellora.com",
            "demo@wellness.com"
        )
        // Use lowercase() with Locale instead of deprecated toLowerCase()
        return registeredEmails.contains(email.lowercase(Locale.getDefault()))
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    @Deprecated(
        "Deprecated in Android",
        ReplaceWith("onBackPressedDispatcher", "androidx.activity.OnBackPressedDispatcher")
    )
    override fun onBackPressed() {
        // This method is deprecated but we're marking it as deprecated too
        // The modern approach is handled by onBackPressedDispatcher above
        navigateToLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity destroyed")
    }
}