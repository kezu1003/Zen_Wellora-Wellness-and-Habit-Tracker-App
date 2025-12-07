package com.example.zen_wellora

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Login : AppCompatActivity() {

    // UI Components
    private lateinit var ivBack: ImageView
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvForgotPassword: TextView
    private lateinit var cvLogin: CardView
    private lateinit var cvFacebook: CardView
    private lateinit var cvTwitter: CardView
    private lateinit var cvGoogle: CardView
    private lateinit var tvSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("LoginActivity", "onCreate started")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            setContentView(R.layout.activity_login)
            Log.d("LoginActivity", "Content view set successfully")

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Initialize views
            initViews()

            // Set up click listeners
            setupClickListeners()
            
            // Handle new user from signup
            handleNewUserFromSignup()

            Log.d("LoginActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in onCreate: ", e)
            Toast.makeText(this, "Error loading login page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        try {
            ivBack = findViewById(R.id.iv_back)
            tilUsername = findViewById(R.id.til_username)
            tilPassword = findViewById(R.id.til_password)
            etUsername = findViewById(R.id.et_username)
            etPassword = findViewById(R.id.et_password)
            tvForgotPassword = findViewById(R.id.tv_forgot_password)
            cvLogin = findViewById(R.id.cv_login)
            cvFacebook = findViewById(R.id.cv_facebook)
            cvTwitter = findViewById(R.id.cv_twitter)
            cvGoogle = findViewById(R.id.cv_google)
            tvSignUp = findViewById(R.id.tv_sign_up)

            Log.d("LoginActivity", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error initializing views: ", e)
        }
    }

    private fun setupClickListeners() {
        try {
            // Back button - Navigate to Selection page
            ivBack.setOnClickListener {
                Log.d("LoginActivity", "Back button clicked")
                val intent = Intent(this, Selection::class.java)
                startActivity(intent)
                finish() // Close login activity
                // Use modern transition method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
            }

            // Login button - Handle email/password login
            cvLogin.setOnClickListener {
                Log.d("LoginActivity", "Login button clicked")
                handleLogin()
            }

            // Forgot password - Navigate to Reset Password page
            tvForgotPassword.setOnClickListener {
                Log.d("LoginActivity", "Forgot password link clicked")
                val intent = Intent(this, Resetpassword::class.java)
                startActivity(intent)
                // Use modern transition method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
            }

            // Sign up link - Navigate to Sign Up page
            tvSignUp.setOnClickListener {
                Log.d("LoginActivity", "Sign up link clicked")
                val intent = Intent(this, Signup::class.java)
                startActivity(intent)
                // Use modern transition method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
            }

            // Social media login buttons
            setupSocialMediaButtons()

            Log.d("LoginActivity", "All click listeners set up successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error setting up click listeners: ", e)
        }
    }

    private fun handleLogin() {
        try {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Clear previous errors
            tilUsername.error = null
            tilPassword.error = null

            // Validate inputs
            if (username.isEmpty()) {
                tilUsername.error = "Username/Email is required"
                etUsername.requestFocus()
                Toast.makeText(this, "Please enter username or email", Toast.LENGTH_SHORT).show()
                return
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password is required"
                etPassword.requestFocus()
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return
            }

            if (password.length < 6) {
                tilPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }

            // Validate email format if it contains @
            if (username.contains("@") && !isValidEmail(username)) {
                tilUsername.error = "Please enter a valid email address"
                etUsername.requestFocus()
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return
            }

            // Demo login validation - Replace with your actual authentication logic
            if (isValidLogin(username, password)) {
                Log.d("LoginActivity", "Login successful, navigating to home")
                Toast.makeText(this, "Login successful! Welcome to Zen Wellora", Toast.LENGTH_SHORT).show()

                // Store user data in SharedPreferences for persistence
                storeUserData(username, password)

                // Navigate to Home/Dashboard with user data
                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("user_name", username)
                intent.putExtra("user_email", getEmailFromUsername(username))
                startActivity(intent)
                finish()
                // Use modern transition method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                Toast.makeText(this, "Invalid username/email or password", Toast.LENGTH_SHORT).show()
                tilPassword.error = "Invalid credentials"
            }

        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in handleLogin: ", e)
            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSocialMediaButtons() {
        try {
            // Facebook login
            cvFacebook.setOnClickListener {
                Log.d("LoginActivity", "Facebook login clicked")
                handleSocialLogin("Facebook")
            }

            // Twitter login
            cvTwitter.setOnClickListener {
                Log.d("LoginActivity", "Twitter login clicked")
                handleSocialLogin("Twitter")
            }

            // Google login
            cvGoogle.setOnClickListener {
                Log.d("LoginActivity", "Google login clicked")
                handleSocialLogin("Google")
            }

            Log.d("LoginActivity", "Social media buttons set up successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error setting up social media buttons: ", e)
        }
    }

    private fun handleSocialLogin(provider: String) {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("$provider Login")
            builder.setMessage("Choose how you want to proceed with $provider authentication:")

            // Option 1: Quick login (Demo)
            builder.setPositiveButton("Quick Login") { _, _ ->
                Log.d("LoginActivity", "$provider quick login selected")
                Toast.makeText(this, "Logged in with $provider! Welcome to Zen Wellora", Toast.LENGTH_SHORT).show()

                // Navigate directly to home
                val intent = Intent(this, Home::class.java)
                intent.putExtra("loginMethod", provider)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                // Use modern transition method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }

            // Option 2: Create account with social provider
            builder.setNeutralButton("Create Account") { _, _ ->
                Log.d("LoginActivity", "$provider create account selected")
                Toast.makeText(this, "Redirecting to signup with $provider...", Toast.LENGTH_SHORT).show()

                // Navigate to signup with social media pre-filled
                val intent = Intent(this, Signup::class.java)
                intent.putExtra("socialProvider", provider)
                startActivity(intent)
                // Use modern transition method
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
            }

            // Option 3: Cancel
            builder.setNegativeButton("Cancel") { dialog, _ ->
                Log.d("LoginActivity", "$provider login cancelled")
                dialog.dismiss()
            }

            builder.show()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in handleSocialLogin: ", e)
            Toast.makeText(this, "$provider login failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle new user coming from signup
    private fun handleNewUserFromSignup() {
        try {
            val isNewUser = intent.getBooleanExtra("newUser", false)
            val username = intent.getStringExtra("username")
            val email = intent.getStringExtra("email")
            
            if (isNewUser && !username.isNullOrEmpty()) {
                Log.d("LoginActivity", "New user from signup: $username")
                
                // Pre-fill the username/email field
                if (!email.isNullOrEmpty()) {
                    etUsername.setText(email)
                } else {
                    etUsername.setText(username)
                }
                
                // Show welcome message
                Toast.makeText(this, "Welcome $username! Please login to continue", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error handling new user from signup: ${e.message}")
        }
    }

    // Store user data in SharedPreferences
    private fun storeUserData(username: String, password: String) {
        try {
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("user_name", username)
            editor.putString("user_email", getEmailFromUsername(username))
            editor.putString("user_password", password)
            editor.putBoolean("user_logged_in", true)
            editor.putLong("login_timestamp", System.currentTimeMillis())
            
            // Clear mood journal data for new user login
            editor.remove("mood_entries")
            editor.putString("mood_entries", "[]") // Empty array for new user
            
            // Clear habit data for new user login
            editor.remove("user_habits")
            editor.putString("user_habits", "[]") // Empty array for new user
            
            // Clear any other user-specific data
            editor.remove("mood_filter_start_date")
            editor.remove("mood_filter_end_date")
            editor.remove("last_mood_entry_id")
            
            editor.apply()
            
            Log.d("LoginActivity", "User data stored and mood journal cleared for: $username")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error storing user data: ${e.message}")
        }
    }

    // Get email from username (for demo purposes)
    private fun getEmailFromUsername(username: String): String {
        return if (username.contains("@")) {
            username
        } else {
            // For demo purposes, create email from username
            "$username@zenwellora.com"
        }
    }

    // Helper method to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Demo login validation - Replace with your actual authentication logic
    private fun isValidLogin(username: String, password: String): Boolean {
        try {
            // Check stored user data from signup
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val registeredUsername = sharedPreferences.getString("registered_username", "")
            val registeredEmail = sharedPreferences.getString("registered_email", "")
            val registeredPassword = sharedPreferences.getString("registered_password", "")
            val userRegistered = sharedPreferences.getBoolean("user_registered", false)

            // If user has registered, validate against stored data
            if (userRegistered && !registeredUsername.isNullOrEmpty() && !registeredPassword.isNullOrEmpty()) {
                val isValidStoredUser = (username == registeredUsername || username == registeredEmail) && password == registeredPassword
                if (isValidStoredUser) {
                    Log.d("LoginActivity", "Login successful with stored user data")
                    return true
                }
            }

            // Fallback to demo validation for testing
            return when {
                username.contains("@") && password == "123456" -> true
                username.lowercase() == "admin" && password == "admin123" -> true
                username.lowercase() == "test" && password == "test123" -> true
                else -> false
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error validating login: ${e.message}")
            return false
        }
    }
}