package com.example.zen_wellora

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import android.util.Log

class ZenWelloraApplication : Application() {
    
    companion object {
        private const val TAG = "ZenWelloraApplication"
        private const val PREFS_NAME = "user_prefs"
        private const val THEME_KEY = "app_theme"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Apply saved theme on app startup
        applySavedTheme()
        
        Log.d(TAG, "Zen Wellora Application initialized")
    }
    
    private fun applySavedTheme() {
        try {
            val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val savedTheme = sharedPreferences.getString(THEME_KEY, "system") ?: "system"
            
            val nightMode = when (savedTheme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            AppCompatDelegate.setDefaultNightMode(nightMode)
            
            Log.d(TAG, "Applied theme: $savedTheme (night mode: $nightMode)")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying saved theme: ${e.message}")
            // Fallback to system theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun applyTheme(theme: String) {
        try {
            val nightMode = when (theme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            AppCompatDelegate.setDefaultNightMode(nightMode)
            
            // Save the theme preference
            val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            sharedPreferences.edit().putString(THEME_KEY, theme).apply()
            
            Log.d(TAG, "Theme applied and saved: $theme (night mode: $nightMode)")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme: ${e.message}")
        }
    }
}



