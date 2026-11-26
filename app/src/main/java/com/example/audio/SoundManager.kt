package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import com.example.R
import com.example.game.MusicTrack
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
 * plus multi-track streaming procedural soundtrack (Lo-Fi Chill, Arcade Pulse,
 * Zen Garden, Synthwave) designed with warm harmonic synthesis and gentle envelopes
 * to prevent audio fatigue.
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

    @Volatile
    var currentTrack: MusicTrack = MusicTrack.LOFI_CHILL
        private set

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

    fun setMusicTrack(track: MusicTrack) {
        if (currentTrack == track) return
        currentTrack = track
        if (musicEnabled && musicShouldPlay) {
            stopMusicLoop()
            startMusicLoop()
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
            streamHarmonicSoundtrack()
        }
    }

    private fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
    }

    /**
     * Generates rich, soothing polyphonic procedural music with soft envelopes
     * and warm harmonic overtones based on the user's selected [currentTrack].
     */
    private fun kotlinx.coroutines.CoroutineScope.streamHarmonicSoundtrack() {
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
            var patternStep = 0
            var sampleInStep = 0
            var phaseRoot = 0f
            var phaseThird = 0f
            var phaseFifth = 0f
            var phaseMelody = 0f

            while (isActive && musicEnabled && musicShouldPlay) {
                val activeTrack = currentTrack
                val chordProgression = when (activeTrack) {
                    MusicTrack.LOFI_CHILL -> LOFI_PROGRESSION
                    MusicTrack.ARCADE_PULSE -> ARCADE_PROGRESSION
                    MusicTrack.ZEN_GARDEN -> ZEN_PROGRESSION
                    MusicTrack.SYNTHWAVE_NEON -> SYNTHWAVE_PROGRESSION
                }

                val currentChord = chordProgression[patternStep % chordProgression.size]
                val stepDurationSamples = when (activeTrack) {
                    MusicTrack.ARCADE_PULSE -> (AMBIENT_SAMPLE_RATE * 850) / 1000
                    MusicTrack.ZEN_GARDEN -> (AMBIENT_SAMPLE_RATE * 2400) / 1000
                    else -> (AMBIENT_SAMPLE_RATE * 1600) / 1000
                }
                val releaseSamples = (stepDurationSamples * 0.18f).toInt()
                val sustainSamples = stepDurationSamples - releaseSamples

                val rootFreq = currentChord.root
                val thirdFreq = currentChord.third
                val fifthFreq = currentChord.fifth
                val melodyFreq = currentChord.melodyNote

                val stepRoot = rootFreq / AMBIENT_SAMPLE_RATE
                val stepThird = thirdFreq / AMBIENT_SAMPLE_RATE
                val stepFifth = fifthFreq / AMBIENT_SAMPLE_RATE
                val stepMelody = melodyFreq / AMBIENT_SAMPLE_RATE

                for (i in 0 until AMBIENT_CHUNK_SAMPLES) {
                    val env = if (sampleInStep < sustainSamples) {
                        val attackNorm = (sampleInStep.toFloat() / (AMBIENT_SAMPLE_RATE * 0.09f)).coerceIn(0f, 1f)
                        val decayFactor = 1.0f - (sampleInStep.toFloat() / stepDurationSamples.toFloat()) * 0.35f
                        attackNorm * decayFactor
                    } else {
                        val sustainEndFactor = 1.0f - (sustainSamples.toFloat() / stepDurationSamples.toFloat()) * 0.35f
                        val relProgress = (sampleInStep - sustainSamples).toFloat() / releaseSamples.coerceAtLeast(1).toFloat()
                        sustainEndFactor * (1f - relProgress).coerceIn(0f, 1f)
                    }

                    val rootS = sampleOscillator(phaseRoot, activeTrack)
                    val thirdS = sampleOscillator(phaseThird, activeTrack)
                    val fifthS = sampleOscillator(phaseFifth, activeTrack)
                    val melodyS = if (melodyFreq > 0f) sampleOscillator(phaseMelody, activeTrack) else 0f

                    val mixed = when (activeTrack) {
                        MusicTrack.LOFI_CHILL -> (0.36f * rootS + 0.26f * thirdS + 0.22f * fifthS + 0.16f * melodyS) * env * 0.048f
                        MusicTrack.ARCADE_PULSE -> (0.38f * rootS + 0.24f * thirdS + 0.18f * fifthS + 0.28f * melodyS) * env * 0.038f
                        MusicTrack.ZEN_GARDEN -> (0.46f * rootS + 0.24f * fifthS + 0.30f * melodyS) * env * 0.038f
                        MusicTrack.SYNTHWAVE_NEON -> (0.42f * rootS + 0.30f * thirdS + 0.18f * fifthS + 0.14f * melodyS) * env * 0.044f
                    }

                    buffer[i] = (mixed * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()

                    phaseRoot = (phaseRoot + stepRoot) % 1f
                    phaseThird = (phaseThird + stepThird) % 1f
                    phaseFifth = (phaseFifth + stepFifth) % 1f
                    if (stepMelody > 0f) phaseMelody = (phaseMelody + stepMelody) % 1f

                    sampleInStep++
                    if (sampleInStep >= stepDurationSamples) {
                        sampleInStep = 0
                        patternStep = (patternStep + 1) % chordProgression.size
                    }
                }

                val written = created.write(buffer, 0, AMBIENT_CHUNK_SAMPLES)
                if (written < 0) break
            }
        } catch (_: Throwable) {
            // Headless JVM/Robolectric fallback
        } finally {
            runCatching {
                track?.pause()
                track?.flush()
                track?.stop()
                track?.release()
            }
        }
    }

    private fun sampleOscillator(phase: Float, track: MusicTrack): Float {
        val idx = (phase * LUT_SIZE).toInt() and LUT_MASK
        val fundamental = SINE_LUT[idx]
        return when (track) {
            MusicTrack.LOFI_CHILL -> {
                // Warm Rhodes electric piano tone: fundamental + soft 2nd harmonic
                val secondHarm = SINE_LUT[(phase * 2f * LUT_SIZE).toInt() and LUT_MASK]
                0.80f * fundamental + 0.20f * secondHarm
            }
            MusicTrack.ARCADE_PULSE -> {
                // Warm retro triangle-ish wave (odd harmonics 1 + 3 + 5, zero DC offset)
                val thirdHarm = SINE_LUT[(phase * 3f * LUT_SIZE).toInt() and LUT_MASK]
                val fifthHarm = SINE_LUT[(phase * 5f * LUT_SIZE).toInt() and LUT_MASK]
                0.72f * fundamental + 0.20f * thirdHarm + 0.08f * fifthHarm
            }
            MusicTrack.ZEN_GARDEN -> {
                // Pure meditative singing bowl tone with gentle 2nd overtone
                val secondHarm = SINE_LUT[(phase * 2f * LUT_SIZE).toInt() and LUT_MASK]
                0.88f * fundamental + 0.12f * secondHarm
            }
            MusicTrack.SYNTHWAVE_NEON -> {
                // Warm analog pad with sub-octave + soft 2nd & 3rd harmonics
                val secondHarm = SINE_LUT[(phase * 2f * LUT_SIZE).toInt() and LUT_MASK]
                val thirdHarm = SINE_LUT[(phase * 3f * LUT_SIZE).toInt() and LUT_MASK]
                0.65f * fundamental + 0.25f * secondHarm + 0.10f * thirdHarm
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

    private data class HarmonicChord(
        val root: Float,
        val third: Float,
        val fifth: Float,
        val melodyNote: Float = 0f
    )

    companion object {
        private const val MAX_STREAMS = 6
        private const val AMBIENT_SAMPLE_RATE = 16000
        private const val AMBIENT_CHUNK_SAMPLES = 1024
        private const val LUT_SIZE = 512
        private const val LUT_MASK = LUT_SIZE - 1

        private val SINE_LUT = FloatArray(LUT_SIZE) { i ->
            sin(2.0 * PI * i.toDouble() / LUT_SIZE.toDouble()).toFloat()
        }

        // Lo-Fi 8-Bar Progression: Cmaj7 -> Am7 -> Fmaj7 -> G6 -> Em7 -> Am7 -> Dm7 -> G13
        private val LOFI_PROGRESSION = listOf(
            HarmonicChord(130.81f, 164.81f, 196.00f, 329.63f),
            HarmonicChord(110.00f, 130.81f, 164.81f, 261.63f),
            HarmonicChord(87.31f, 110.00f, 130.81f, 220.00f),
            HarmonicChord(98.00f, 123.47f, 146.83f, 246.94f),
            HarmonicChord(164.81f, 196.00f, 246.94f, 392.00f),
            HarmonicChord(110.00f, 130.81f, 164.81f, 329.63f),
            HarmonicChord(146.83f, 174.61f, 220.00f, 293.66f),
            HarmonicChord(98.00f, 123.47f, 146.83f, 261.63f)
        )

        // Arcade Pulse 8-Step Melodic Arpeggio
        private val ARCADE_PROGRESSION = listOf(
            HarmonicChord(130.81f, 164.81f, 196.00f, 261.63f),
            HarmonicChord(130.81f, 164.81f, 196.00f, 329.63f),
            HarmonicChord(146.83f, 174.61f, 220.00f, 349.23f),
            HarmonicChord(146.83f, 174.61f, 220.00f, 440.00f),
            HarmonicChord(164.81f, 196.00f, 246.94f, 392.00f),
            HarmonicChord(110.00f, 130.81f, 164.81f, 329.63f),
            HarmonicChord(98.00f, 123.47f, 146.83f, 293.66f),
            HarmonicChord(130.81f, 164.81f, 196.00f, 392.00f)
        )

        // Zen Garden Pentatonic Bowls (C - D - E - G - A)
        private val ZEN_PROGRESSION = listOf(
            HarmonicChord(130.81f, 164.81f, 196.00f, 261.63f),
            HarmonicChord(146.83f, 196.00f, 220.00f, 293.66f),
            HarmonicChord(164.81f, 196.00f, 261.63f, 329.63f),
            HarmonicChord(110.00f, 164.81f, 196.00f, 392.00f),
            HarmonicChord(130.81f, 196.00f, 261.63f, 329.63f),
            HarmonicChord(98.00f, 146.83f, 196.00f, 293.66f)
        )

        // Synthwave Analog Pads (Dm -> Bb -> F -> C -> Gm -> Dm -> Bb -> A7sus)
        private val SYNTHWAVE_PROGRESSION = listOf(
            HarmonicChord(73.42f, 110.00f, 146.83f, 220.00f),
            HarmonicChord(58.27f, 87.31f, 116.54f, 174.61f),
            HarmonicChord(87.31f, 130.81f, 174.61f, 261.63f),
            HarmonicChord(65.41f, 98.00f, 130.81f, 196.00f),
            HarmonicChord(98.00f, 116.54f, 146.83f, 293.66f),
            HarmonicChord(73.42f, 110.00f, 146.83f, 261.63f),
            HarmonicChord(58.27f, 87.31f, 116.54f, 220.00f),
            HarmonicChord(110.00f, 146.83f, 164.81f, 246.94f)
        )
    }
}
