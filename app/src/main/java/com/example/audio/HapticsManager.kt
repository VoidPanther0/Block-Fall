package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticEvent(val durationMs: Long, val amplitude: Int) {
    TAP(12, 60),
    PLACE(20, 110),
    CLEAR(35, 160),
    COMBO(55, 200),
    PERFECT(90, 255),
    GAME_OVER(160, 200)
}

/**
 * Real vibration through the system Vibrator. Does nothing when the device has no
 * vibrator, [enabled] is false (the in-app Haptics setting), or the platform denies it.
 * Amplitude control is used only where the hardware reports support.
 */
class HapticsManager(context: Context) {
    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    @Volatile
    var enabled: Boolean = true

    val isSupported: Boolean get() = runCatching { vibrator?.hasVibrator() == true }.getOrDefault(false)

    fun perform(event: HapticEvent) {
        if (!enabled) return
        val v = vibrator ?: return
        runCatching {
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = if (v.hasAmplitudeControl()) event.amplitude else VibrationEffect.DEFAULT_AMPLITUDE
                v.vibrate(VibrationEffect.createOneShot(event.durationMs, amp))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(event.durationMs)
            }
        }
    }

    fun cancel() {
        runCatching { vibrator?.cancel() }
    }
}
