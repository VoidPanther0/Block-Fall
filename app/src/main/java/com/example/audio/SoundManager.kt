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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

/**
 * SoundPool-backed sound manager for low-latency triggerable game SFX
 * (block drops, line clears, combo completions, level clear, game over, UI clicks)
 * plus low-overhead streaming ambient background music using a precomputed sine LUT
 * and a single reused AudioTrack buffer (zero per-cycle allocations or blocking sleep).
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

    /** Sign-in success cue (reuses the level-complete sample, pitched up). */
    fun playLogin() {
        playSample(levelCompleteSoundId, volume = 0.7f, rate = 1.25f)
    }

    fun playGreat() {
        playSample(lineClearSoundId, volume = 1.0f, rate = 1.2f)
    }

    fun playPerfect() {
        playSample(levelCompleteSoundId, volume = 1.0f, rate = 1.1f)
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
            streamAmbientChordPad()
        }
    }

    private fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun kotlinx.coroutines.CoroutineScope.streamAmbientChordPad() {
        var track: AudioTrack? = null
        try {
            val minBufBytes = AudioTrack.getMinBufferSize(
                AMBIENT_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(AMBIENT_CHUNK_SAMPLES * 4)

            val created = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(AMBIENT_SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (created.state != AudioTrack.STATE_INITIALIZED) {
                created.release()
                return
            }

            track = created
            created.play()

            val buffer = ShortArray(AMBIENT_CHUNK_SAMPLES)
            var chordIndex = 0
            var sampleInChord = 0
            var phaseRoot = 0f
            var phaseFifth = 0f

            while (isActive && musicEnabled && musicShouldPlay) {
                val rootFreq = CHORD_ROOTS[chordIndex]
                val fifthFreq = rootFreq * 1.498f
                val stepRoot = rootFreq / AMBIENT_SAMPLE_RATE
                val stepFifth = fifthFreq / AMBIENT_SAMPLE_RATE

                for (i in 0 until AMBIENT_CHUNK_SAMPLES) {
                    if (sampleInChord < SAMPLES_PER_CHORD) {
                        val envNorm = sampleInChord.toFloat() / SAMPLES_PER_CHORD.toFloat()
                        val envIdx = (envNorm * HALF_LUT_LIMIT).toInt().coerceIn(0, HALF_LUT_LIMIT)
                        val env = SINE_LUT[envIdx]

                        val rootSample = SINE_LUT[(phaseRoot * LUT_SIZE).toInt() and LUT_MASK]
                        val fifthSample = SINE_LUT[(phaseFifth * LUT_SIZE).toInt() and LUT_MASK]
                        val mixed = (0.65f * rootSample + 0.35f * fifthSample) * env * AMBIENT_GAIN

                        buffer[i] = (mixed * Short.MAX_VALUE)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()

                        phaseRoot += stepRoot
                        if (phaseRoot >= 1f) phaseRoot -= 1f
                        phaseFifth += stepFifth
                        if (phaseFifth >= 1f) phaseFifth -= 1f
                    } else {
                        buffer[i] = 0
                    }

                    sampleInChord++
                    if (sampleInChord >= TOTAL_CYCLE_SAMPLES) {
                        sampleInChord = 0
                        chordIndex = (chordIndex + 1) % CHORD_ROOTS.size
                    }
                }

                val written = created.write(buffer, 0, AMBIENT_CHUNK_SAMPLES)
                if (written < 0) break
            }
        } catch (_: Throwable) {
            // AudioTrack unavailable on headless JVM/Robolectric
        } finally {
            runCatching {
                track?.pause()
                track?.flush()
                track?.stop()
                track?.release()
            }
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
        private const val AMBIENT_SAMPLE_RATE = 16000
        private const val AMBIENT_CHUNK_SAMPLES = 1024
        private const val SAMPLES_PER_CHORD = (AMBIENT_SAMPLE_RATE * 1400) / 1000
        private const val TOTAL_CYCLE_SAMPLES = SAMPLES_PER_CHORD + (AMBIENT_SAMPLE_RATE * 180) / 1000
        private const val AMBIENT_GAIN = 0.05f
        private const val LUT_SIZE = 512
        private const val LUT_MASK = LUT_SIZE - 1
        private const val HALF_LUT_LIMIT = (LUT_SIZE / 2) - 1
        private val CHORD_ROOTS = floatArrayOf(220f, 261.63f, 196f, 174.61f)
        private val SINE_LUT = FloatArray(LUT_SIZE) { i ->
            sin(2.0 * PI * i.toDouble() / LUT_SIZE.toDouble()).toFloat()
        }
    }
}
