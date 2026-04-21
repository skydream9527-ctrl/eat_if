package com.eatif.app.ui.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SoundManager {
    private var soundPool: SoundPool? = null
    private var soundClick: Int = 0
    private var soundWin: Int = 0
    private var soundLose: Int = 0
    private var soundSuccess: Int = 0
    private var soundFail: Int = 0

    private var isInitialized = false
    private var isEnabled = true
    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        if (isInitialized) return

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let { pool ->
            // Sound resources would be loaded from app's raw resources
            // For now, we just initialize the pool without sounds (vibration only)
        }

        isInitialized = true
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        SoundManager.isEnabled = enabled
    }

    fun playClick() {
        if (isEnabled) {
            vibrate(20)
        }
    }

    fun playWin() {
        if (isEnabled) {
            vibratePattern(longArrayOf(0, 50, 50, 100))
        }
    }

    fun playLose() {
        if (isEnabled) {
            vibrate(100)
        }
    }

    fun playSuccess() {
        if (isEnabled) {
            vibratePattern(longArrayOf(0, 30, 30, 60, 30, 90))
        }
    }

    fun playFail() {
        if (isEnabled) {
            vibratePattern(longArrayOf(0, 100, 50, 100))
        }
    }

    private fun vibrate(duration: Long) {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(duration)
            }
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}