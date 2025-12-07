package com.example.zen_wellora

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class AlarmSoundPlayer(private val context: Context) {
    
    companion object {
        private const val TAG = "AlarmSoundPlayer"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    init {
        initializeVibrator()
    }
    
    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    fun playNotificationAlarm() {
        try {
            Log.d(TAG, "=== Playing Notification Alarm ===")
            
            // Check audio settings first
            checkAudioSettings()
            
            // Play sound
            playAlarmSound()
            
            // Play vibration
            playVibration()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing notification alarm: ${e.message}", e)
        }
    }
    
    private fun checkAudioSettings() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            val ringerMode = audioManager.ringerMode
            Log.d(TAG, "Current ringer mode: $ringerMode")
            
            val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            Log.d(TAG, "Notification volume: $notificationVolume/$maxNotificationVolume")
            
            val alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            Log.d(TAG, "Alarm volume: $alarmVolume/$maxAlarmVolume")
            
            // If notification volume is 0 or device is silent, we might not hear anything
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                Log.w(TAG, "Device is in silent mode - sound may not play")
            } else if (notificationVolume == 0 && alarmVolume == 0) {
                Log.w(TAG, "Both notification and alarm volumes are 0")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking audio settings: ${e.message}")
        }
    }
    
    private fun playAlarmSound() {
        try {
            // Stop any existing playback
            stopAlarmSound()
            
            // Get notification sound URI - try multiple fallbacks
            val soundUri = getNotificationSoundUri()
            Log.d(TAG, "Using sound URI: $soundUri")
            
            if (soundUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, soundUri)
                    
                    // Use ALARM stream for more reliable playback
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                    
                    // Set audio attributes for better compatibility
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                    }
                    
                    // Set volume to max for alarm
                    setVolume(1.0f, 1.0f)
                    
                    // Prepare and play
                    setOnPreparedListener { mp ->
                        Log.d(TAG, "MediaPlayer prepared - starting playback")
                        mp.start()
                    }
                    
                    setOnCompletionListener { mp ->
                        Log.d(TAG, "Sound playback completed")
                        mp.release()
                        mediaPlayer = null
                    }
                    
                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        mp.release()
                        mediaPlayer = null
                        true
                    }
                    
                    // Start async preparation
                    prepareAsync()
                }
                
                Log.d(TAG, "MediaPlayer set up for sound playback")
            } else {
                Log.w(TAG, "No sound URI available - cannot play sound")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound: ${e.message}", e)
            // Clean up on error
            stopAlarmSound()
        }
    }
    
    private fun getNotificationSoundUri(): Uri? {
        // Try multiple sound sources as fallbacks
        val soundOptions = listOf(
            { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) },
            { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) },
            { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) }
        )
        
        for (getSoundUri in soundOptions) {
            try {
                val uri = getSoundUri()
                if (uri != null) {
                    Log.d(TAG, "Found sound URI: $uri")
                    return uri
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get sound URI: ${e.message}")
            }
        }
        
        Log.w(TAG, "No sound URI found - will be silent")
        return null
    }
    
    private fun playVibration() {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Modern vibration pattern
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    vib.vibrate(effect)
                    Log.d(TAG, "Playing modern vibration pattern")
                } else {
                    // Legacy vibration
                    @Suppress("DEPRECATION")
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    vib.vibrate(pattern, -1)
                    Log.d(TAG, "Playing legacy vibration pattern")
                }
            } ?: Log.w(TAG, "Vibrator not available")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing vibration: ${e.message}")
        }
    }
    
    fun stopAlarmSound() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                    Log.d(TAG, "Stopped alarm sound playback")
                }
                mp.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm sound: ${e.message}")
        }
    }
    
    fun cleanup() {
        stopAlarmSound()
        vibrator = null
    }
}