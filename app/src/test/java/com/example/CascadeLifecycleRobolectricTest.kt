package com.example

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.example.game.BlockColor
import com.example.game.Board
import com.example.game.GamePiece
import com.example.game.GameStatus
import com.example.game.PieceShape
import com.example.game.TraySlot
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ViewModel lifecycle guards: cascade cancellation, queued pause, stale selection.
 * Uses virtual time so the cascade animation never depends on wall-clock delays.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CascadeLifecycleRobolectricTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var vm: GameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = GameViewModel(ApplicationProvider.getApplicationContext<Application>())
        vm.startNewClassicGame(countAsNewGame = false)
        dispatcher.scheduler.runCurrent()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Suppress("UNCHECKED_CAST")
    private fun mutateState(transform: (GameUiState) -> GameUiState) {
        val f = GameViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        (f.get(vm) as MutableStateFlow<GameUiState>).value = transform(vm.uiState.value)
    }

    private fun dot(id: Long) = GamePiece(id, PieceShape.DOT, BlockColor.CYAN)

    /** Board where placing one DOT at (7,7) completes the bottom row and starts a cascade. */
    private fun seedClearingMove() {
        val rows = MutableList(8) { "........" }
        rows[7] = "#######."
        rows[5] = "#......."
        mutateState {
            it.copy(
                board = Board.fromPattern(rows),
                traySlots = listOf(TraySlot(0, dot(900)), TraySlot(1, dot(901)), TraySlot(2, dot(902))),
                status = GameStatus.PLAYING
            )
        }
    }

    @Test
    fun `new_game_mid_cascade_is_not_overwritten_by_old_cascade`() {
        seedClearingMove()
        vm.placePiece(0, 7, 7)
        dispatcher.scheduler.runCurrent()
        assertEquals(GameStatus.RESOLVING_CASCADE, vm.uiState.value.status)

        vm.startNewClassicGame(countAsNewGame = false)
        val freshBoard = vm.uiState.value.board
        val freshScore = vm.uiState.value.score
        dispatcher.scheduler.advanceTimeBy(5_000)
        dispatcher.scheduler.runCurrent()

        assertEquals(freshBoard, vm.uiState.value.board)
        assertEquals(freshScore, vm.uiState.value.score)
        assertEquals(GameStatus.PLAYING, vm.uiState.value.status)
        assertNull(vm.uiState.value.floatingBanner)
    }

    @Test
    fun `exit_to_home_mid_cascade_finalizes_turn_instead_of_snapshotting_partial_board`() {
        seedClearingMove()
        vm.placePiece(0, 7, 7)
        dispatcher.scheduler.runCurrent()
        assertEquals(GameStatus.RESOLVING_CASCADE, vm.uiState.value.status)

        vm.exitToHome()
        dispatcher.scheduler.runCurrent()

        val s = vm.uiState.value
        assertTrue("turn must be finalized", s.status != GameStatus.RESOLVING_CASCADE)
        // Final board has the cleared row gone and the stray block settled on the bottom row.
        assertNull(s.board.get(7, 7))
        assertTrue(s.board.get(7, 0) != null)
    }

    @Test
    fun `pause_during_cascade_is_queued_and_applied_after_finalization`() {
        seedClearingMove()
        vm.placePiece(0, 7, 7)
        dispatcher.scheduler.runCurrent()
        assertEquals(GameStatus.RESOLVING_CASCADE, vm.uiState.value.status)

        vm.pauseGame()
        assertEquals(GameStatus.RESOLVING_CASCADE, vm.uiState.value.status)

        dispatcher.scheduler.advanceTimeBy(5_000)
        dispatcher.scheduler.runCurrent()
        assertEquals(GameStatus.PAUSED, vm.uiState.value.status)
    }

    @Test
    fun `failed_or_cancelled_drag_clears_selected_slot`() {
        vm.selectTraySlot(0)
        assertEquals(0, vm.uiState.value.selectedSlotIndex)

        vm.startDrag(0, Offset(10f, 10f))
        vm.endDrag() // no board bounds -> no hover anchor -> failed drop
        assertNull(vm.uiState.value.selectedSlotIndex)
        assertNull(vm.uiState.value.dragState)

        vm.startDrag(1, Offset(10f, 10f))
        vm.cancelDrag()
        assertNull(vm.uiState.value.selectedSlotIndex)
    }

    @Test
    fun `shuffle_is_refused_during_cascade_and_drag_and_wallet_untouched`() {
        seedClearingMove()
        mutateState { it.copy(wallet = it.wallet.copy(shuffleCount = 2)) }
        val before = vm.uiState.value.wallet

        vm.startDrag(0, Offset(10f, 10f))
        vm.useShuffleBooster()
        dispatcher.scheduler.runCurrent()
        assertEquals(before, vm.uiState.value.wallet)
        vm.cancelDrag()

        vm.placePiece(0, 7, 7)
        dispatcher.scheduler.runCurrent()
        vm.useShuffleBooster()
        dispatcher.scheduler.runCurrent()
        assertEquals(before, vm.uiState.value.wallet)
    }
}
