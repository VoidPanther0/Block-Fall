package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight synthesized audio engine for tactile game SFX and ambient music.
 * All AudioTrack operations run off the main thread and safely no-op in headless/JVM test environments.
 */
class SoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var musicJob: Job? = null
    private var musicShouldPlay: Boolean = false

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopMusicLoop()
            } else if (musicShouldPlay) {
                startMusicLoop()
            }
        }

    fun setAppForeground(inForeground: Boolean) {
        musicShouldPlay = inForeground
        if (inForeground && musicEnabled) {
            startMusicLoop()
        } else {
            stopMusicLoop()
        }
    }

    fun playPlace() {
        if (!soundEnabled) return
        playTone(startFreq = 240.0, endFreq = 150.0, durationMs = 55, volume = 0.25)
    }

    fun playLineClear(waveIndex: Int) {
        if (!soundEnabled) return
        val base = 440.0 * Math.pow(1.12, (waveIndex - 1).coerceAtLeast(0).toDouble())
        scope.launch {
            playToneSync(startFreq = base, endFreq = base * 1.25, durationMs = 95, volume = 0.24)
            delay(45L)
            playToneSync(startFreq = base * 1.5, endFreq = base * 1.5, durationMs = 110, volume = 0.20)
        }
    }

    fun playLevelComplete() {
        if (!soundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.5)
            for (freq in notes) {
                playToneSync(startFreq = freq, endFreq = freq, durationMs = 105, volume = 0.22)
                delay(70L)
            }
        }
    }

    fun playGameOver() {
        if (!soundEnabled) return
        scope.launch {
            playToneSync(startFreq = 349.23, endFreq = 311.13, durationMs = 160, volume = 0.22)
            delay(130L)
            playToneSync(startFreq = 261.63, endFreq = 207.65, durationMs = 240, volume = 0.20)
        }
    }

    fun playUiClick() {
        if (!soundEnabled) return
        playTone(startFreq = 580.0, endFreq = 640.0, durationMs = 30, volume = 0.14)
    }

    private fun startMusicLoop() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            val chordRoots = listOf(220.0, 261.63, 196.0, 174.61)
            var idx = 0
            while (isActive && musicEnabled && musicShouldPlay) {
                val root = chordRoots[idx % chordRoots.size]
                playAmbientPadSync(root = root, durationMs = 1400, volume = 0.055)
                idx++
                delay(180L)
            }
        }
    }

    private fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopMusicLoop()
    }

    private fun playTone(startFreq: Double, endFreq: Double, durationMs: Int, volume: Double) {
        scope.launch {
            playToneSync(startFreq, endFreq, durationMs, volume)
        }
    }

    private fun playToneSync(startFreq: Double, endFreq: Double, durationMs: Int, volume: Double) {
        runCatching {
            val sampleRate = 22050
            val count = ((sampleRate * durationMs) / 1000).coerceAtLeast(1)
            val samples = ShortArray(count)
            var phase = 0.0
            for (i in 0 until count) {
                val t = i.toDouble() / count.toDouble()
                val freq = startFreq + (endFreq - startFreq) * t
                val envelope = sin(PI * t) * exp(-2.2 * t)
                phase += 2.0 * PI * freq / sampleRate
                val value = (sin(phase) * envelope * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                samples[i] = value.toShort()
            }
            writeStaticTrack(sampleRate, samples, durationMs)
        }
    }

    private fun playAmbientPadSync(root: Double, durationMs: Int, volume: Double) {
        runCatching {
            val sampleRate = 16000
            val count = ((sampleRate * durationMs) / 1000).coerceAtLeast(1)
            val samples = ShortArray(count)
            val fifth = root * 1.498
            for (i in 0 until count) {
                val t = i.toDouble() / count.toDouble()
                val env = sin(PI * t)
                val sec = i.toDouble() / sampleRate.toDouble()
                val wave = 0.65 * sin(2.0 * PI * root * sec) + 0.35 * sin(2.0 * PI * fifth * sec)
                val value = (wave * env * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                samples[i] = value.toShort()
            }
            writeStaticTrack(sampleRate, samples, durationMs)
        }
    }

    private fun writeStaticTrack(sampleRate: Int, samples: ShortArray, durationMs: Int) {
        val byteLen = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteLen)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        if (track.state == AudioTrack.STATE_INITIALIZED) {
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 25L)
            track.stop()
        }
        track.release()
    }
}
