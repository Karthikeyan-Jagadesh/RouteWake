package com.routewake.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmAudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun startAlarm(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        if (isPlaying) return
        isPlaying = true

        if (soundEnabled) {
            playAlarmSound()
        }

        if (vibrationEnabled) {
            startVibration()
        }
    }

    private fun playAlarmSound() {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            // Audio policy or restriction fallback
        }
    }

    private fun startVibration() {
        try {
            vibrator?.let { vib ->
                if (vib.hasVibrator()) {
                    val pattern = longArrayOf(0, 500, 300, 500, 300, 700)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(pattern, 0)
                    }
                }
            }
        } catch (_: Exception) {
            // Vibration permission or policy restriction
        }
    }

    fun stopAlarm() {
        isPlaying = false
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }

        try {
            vibrator?.cancel()
        } catch (_: Exception) {
        }
    }

    fun isAlarmActive(): Boolean = isPlaying
}
