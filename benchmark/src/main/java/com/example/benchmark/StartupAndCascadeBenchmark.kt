package com.example.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Hardware Macrobenchmark suite for Block Falls startup & gameplay frame timing.
 * Run on a physical device or emulator with:
 * gradle :benchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class StartupAndCascadeBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupColdCompilationNone() = startup(CompilationMode.None())

    @Test
    fun startupColdBaselineProfile() = startup(CompilationMode.Partial())

    @Test
    fun gameplayFrameTiming() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Partial(),
            setupBlock = {
                pressHome()
                startActivityAndWait()
                device.wait(Until.hasObject(By.res("home_play_button")), 5_000)
                device.findObject(By.res("home_play_button"))?.click()
                device.waitForIdle()
            }
        ) {
            val board = device.findObject(By.res("game_board"))
            val slot0 = device.findObject(By.res("tray_slot_0"))
            if (slot0 != null && board != null) {
                val dest = board.visibleCenter
                slot0.drag(dest, 600)
                device.waitForIdle()
            }
        }
    }

    private fun startup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = compilationMode
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.aistudio.cascadegrid.vqmxrt"
    }
}
