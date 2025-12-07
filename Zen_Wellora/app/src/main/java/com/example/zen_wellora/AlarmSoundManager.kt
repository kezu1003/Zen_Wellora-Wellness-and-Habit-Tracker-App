package com.example.zen_wellora

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Singleton class to manage alarm sounds globally across the app
 * This ensures we can stop sounds from any activity or receiver
 */
object AlarmSoundManager {
    private const val TAG = "AlarmSoundManager"
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Track all MediaPlayer instances for emergency shutdown
    private val allMediaPlayers = mutableSetOf<MediaPlayer>()
    
    fun playAlarmSound(context: Context) {
        try {
            Log.d(TAG, "=== Starting Alarm Sound ===")
            
            // Stop any existing sound first
            stopAlarmSound()
            
            // Check audio settings
            logAudioSettings(context)
            
            // Initialize vibrator if not already done
            if (vibrator == null) {
                initializeVibrator(context)
            }
            
            // Play sound
            startMediaPlayerAlarm(context)
            
            // Play vibration
            startVibration()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound: ${e.message}", e)
        }
    }
    
    fun stopAlarmSound() {
        // Ensure we're on the main thread for MediaPlayer operations
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.d(TAG, "stopAlarmSound called from background thread - posting to main thread")
            mainHandler.post { stopAlarmSound() }
            return
        }
        
        try {
            val currentTime = System.currentTimeMillis()
            Log.d(TAG, "=== EMERGENCY STOP ALARM SOUND CALLED AT $currentTime ===")
            Log.d(TAG, "Thread: ${Thread.currentThread().name}")
            Log.d(TAG, "MediaPlayer exists: ${mediaPlayer != null}")
            Log.d(TAG, "isPlaying flag: $isPlaying")
            
            // EMERGENCY: Stop ALL MediaPlayer instances
            Log.d(TAG, "Emergency stopping ALL MediaPlayer instances (${allMediaPlayers.size} tracked)")
            allMediaPlayers.toList().forEach { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                        Log.d(TAG, "✓ Emergency stopped MediaPlayer")
                    }
                    mp.release()
                    Log.d(TAG, "✓ Emergency released MediaPlayer")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in emergency MediaPlayer stop: ${e.message}")
                }
            }
            allMediaPlayers.clear()
            
            // Stop current MediaPlayer
            mediaPlayer?.let { mp ->
                try {
                    Log.d(TAG, "MediaPlayer.isPlaying(): ${mp.isPlaying}")
                    if (mp.isPlaying) {
                        mp.stop()
                        Log.d(TAG, "✓ MediaPlayer.stop() SUCCESSFUL")
                    } else {
                        Log.d(TAG, "MediaPlayer was not playing")
                    }
                    mp.release()
                    Log.d(TAG, "✓ MediaPlayer.release() SUCCESSFUL")
                    allMediaPlayers.remove(mp)
                } catch (e: Exception) {
                    Log.e(TAG, "CRITICAL ERROR stopping MediaPlayer: ${e.message}", e)
                    try {
                        mp.release()
                        allMediaPlayers.remove(mp)
                        Log.d(TAG, "Emergency MediaPlayer.release() completed")
                    } catch (e2: Exception) {
                        Log.e(TAG, "FATAL ERROR releasing MediaPlayer: ${e2.message}", e2)
                    }
                }
                mediaPlayer = null
                Log.d(TAG, "MediaPlayer reference set to null")
            } ?: Log.d(TAG, "MediaPlayer was already null")
            
            // Stop vibration
            try {
                vibrator?.cancel()
                Log.d(TAG, "✓ Vibrator cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling vibrator: ${e.message}", e)
            }
            
            isPlaying = false
            Log.d(TAG, "✓ isPlaying flag set to false")
            Log.d(TAG, "=== ALARM SOUND STOP PROCEDURE COMPLETE ===")
            
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL ERROR in stopAlarmSound: ${e.message}", e)
        }
    }
    
    fun isCurrentlyPlaying(): Boolean {
        return isPlaying && mediaPlayer != null
    }
    
    private fun startMediaPlayerAlarm(context: Context) {
        try {
            val soundUri = getBestSoundUri(context)
            if (soundUri == null) {
                Log.w(TAG, "No sound URI available")
                return
            }
            
            Log.d(TAG, "Creating MediaPlayer with URI: $soundUri")
            mediaPlayer = MediaPlayer().apply {
                // Track this instance for emergency shutdown
                allMediaPlayers.add(this)
                setDataSource(context, soundUri)
                
                // Use ALARM stream for maximum reliability
                setAudioStreamType(AudioManager.STREAM_ALARM)
                
                // Set audio attributes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                
                // Set to loop for persistent alarm
                isLooping = true
                
                // Set maximum volume
                setVolume(1.0f, 1.0f)
                
                setOnPreparedListener { mp ->
                    Log.d(TAG, "MediaPlayer prepared - starting alarm")
                    mp.start()
                    this@AlarmSoundManager.isPlaying = true
                    
                    // Auto-stop after 30 seconds as safety measure
                    mainHandler.postDelayed({
                        if (this@AlarmSoundManager.isPlaying) {
                            Log.d(TAG, "Auto-stopping alarm after 30 seconds")
                            stopAlarmSound()
                        }
                    }, 30000)
                }
                
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    this@AlarmSoundManager.isPlaying = false
                    allMediaPlayers.remove(mp)
                    mp.release()
                    mediaPlayer = null
                    true
                }
                
                setOnCompletionListener { mp ->
                    Log.d(TAG, "MediaPlayer completed")
                    // Don't clean up here since we're looping
                }
                
                prepareAsync()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating MediaPlayer: ${e.message}", e)
            this@AlarmSoundManager.isPlaying = false
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    
    private fun getBestSoundUri(context: Context): Uri? {
        val soundOptions = listOf(
            { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) },
            { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) },
            { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) }
        )
        
        for (getSoundUri in soundOptions) {
            try {
                val uri = getSoundUri()
                if (uri != null) {
                    Log.d(TAG, "Using sound URI: $uri")
                    return uri
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get sound URI: ${e.message}")
            }
        }
        
        Log.w(TAG, "No sound URI found")
        return null
    }
    
    private fun initializeVibrator(context: Context) {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            Log.d(TAG, "Vibrator initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing vibrator: ${e.message}")
        }
    }
    
    private fun startVibration() {
        try {
            vibrator?.let { vib ->
                val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0) // Repeat indefinitely
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(pattern, 0) // Repeat indefinitely
                }
                
                Log.d(TAG, "Vibration started")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration: ${e.message}")
        }
    }
    
    private fun logAudioSettings(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            Log.d(TAG, "Ringer mode: ${audioManager.ringerMode}")
            Log.d(TAG, "Alarm volume: ${audioManager.getStreamVolume(AudioManager.STREAM_ALARM)}/${audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)}")
            Log.d(TAG, "Notification volume: ${audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)}/${audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging audio settings: ${e.message}")
        }
    }
    
    fun cleanup() {
        try {
            stopAlarmSound()
            vibrator = null
            Log.d(TAG, "AlarmSoundManager cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up AlarmSoundManager: ${e.message}")
        }
    }
}