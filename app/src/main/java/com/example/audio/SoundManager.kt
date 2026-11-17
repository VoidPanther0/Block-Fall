package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

/**
 * SoundPool-backed sound manager for low-latency triggerable game SFX
 * (block drops, line clears, combo completions, level clear, game over, UI clicks)
 * plus optional ambient background music when enabled in Settings.
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var musicJob: Job? = null
    private var musicShouldPlay: Boolean = false

    private val loadedSampleIds = ConcurrentHashMap.newKeySet<Int>()
    private var soundPool: SoundPool? = null

    private var blockDropSoundId: Int = 0
    private var lineClearSoundId: Int = 0
    private var comboCompleteSoundId: Int = 0
    private var levelCompleteSoundId: Int = 0
    private var gameOverSoundId: Int = 0
    private var uiClickSoundId: Int = 0

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

    val isReady: Boolean
        get() = soundPool != null

    init {
        runCatching {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSampleIds.add(sampleId)
                }
            }

            blockDropSoundId = pool.load(appContext, R.raw.sfx_block_drop, 1)
            lineClearSoundId = pool.load(appContext, R.raw.sfx_line_clear, 1)
            comboCompleteSoundId = pool.load(appContext, R.raw.sfx_combo_cascade, 1)
            levelCompleteSoundId = pool.load(appContext, R.raw.sfx_level_complete, 1)
            gameOverSoundId = pool.load(appContext, R.raw.sfx_game_over, 1)
            uiClickSoundId = pool.load(appContext, R.raw.sfx_ui_click, 1)

            soundPool = pool
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

    /**
     * Triggered when a piece is placed on the board or when falling blocks land.
     */
    fun playBlockDrop(volume: Float = 0.85f, rate: Float = 1.0f) {
        playSample(blockDropSoundId, volume = volume, rate = rate)
    }

    /**
     * Triggered when one or more lines clear on the initial wave.
     */
    fun playLineClear(linesCleared: Int = 1) {
        val pitchBoost = (1.0f + (linesCleared - 1).coerceAtLeast(0) * 0.06f).coerceIn(0.9f, 1.3f)
        playSample(lineClearSoundId, volume = 0.90f, rate = pitchBoost)
    }

    /**
     * Triggered when a gravity cascade combo or multi-wave chain completes/triggers.
     */
    fun playComboComplete(comboDepth: Int = 2) {
        val pitchBoost = (1.0f + (comboDepth - 2).coerceAtLeast(0) * 0.08f).coerceIn(1.0f, 1.45f)
        playSample(comboCompleteSoundId, volume = 0.95f, rate = pitchBoost)
    }

    fun playLevelComplete() {
        playSample(levelCompleteSoundId, volume = 0.95f, rate = 1.0f)
    }

    fun playGameOver() {
        playSample(gameOverSoundId, volume = 0.85f, rate = 1.0f)
    }

    fun playUiClick() {
        playSample(uiClickSoundId, volume = 0.55f, rate = 1.0f)
    }

    private fun playSample(soundId: Int, volume: Float, rate: Float) {
        if (!soundEnabled || soundId == 0) return
        val pool = soundPool ?: return
        val clampedVol = volume.coerceIn(0f, 1f)
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        runCatching {
            if (loadedSampleIds.isEmpty() || loadedSampleIds.contains(soundId)) {
                pool.play(soundId, clampedVol, clampedVol, 1, 0, clampedRate)
            }
        }
    }

    private fun startMusicLoop() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            val chordRoots = listOf(220.0, 261.63, 196.0, 174.61)
            var idx = 0
            while (isActive && musicEnabled && musicShouldPlay) {
                val root = chordRoots[idx % chordRoots.size]
                playAmbientPadSync(root = root, durationMs = 1400, volume = 0.05)
                idx++
                delay(180L)
            }
        }
    }

    private fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
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
            val byteLen = samples.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

    fun release() {
        stopMusicLoop()
        scope.cancel()
        loadedSampleIds.clear()
        runCatching { soundPool?.release() }
        soundPool = null
    }

    companion object {
        private const val MAX_STREAMS = 6
    }
}
