package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Board
import com.example.game.GridCoord
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class TutorialStep(
    val number: Int,
    val title: String,
    val description: String,
    val tip: String,
    val board: Board,
    val highlightCoords: Set<GridCoord>,
    val highlightRows: List<Int>,
    val showGravityArrow: Boolean
)

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    var demoStep by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            TutorialStep(
                number = 1,
                title = "Place Any Piece",
                description = "Drag a piece from your tray or tap a slot then tap the board. Pieces appear lifted above your finger so you never lose sight of the grid.",
                tip = "Blocks stay locked in place until you complete a full row or column.",
                board = Board.fromPattern(
                    listOf(
                        "........",
                        "........",
                        "........",
                        "....1111",
                        "2222....",
                        "........",
                        "........",
                        "00000.00"
                    )
                ),
                highlightCoords = setOf(GridCoord(7, 5)),
                highlightRows = emptyList(),
                showGravityArrow = false
            ),
            TutorialStep(
                number = 2,
                title = "Complete Full Lines",
                description = "Fill all 8 cells in any row or column—or intersect both at once—to clear those lines immediately.",
                tip = "Multi-line clears in a single turn earn +15 extra points per additional line.",
                board = Board.fromPattern(
                    listOf(
                        "........",
                        "........",
                        "........",
                        "....1111",
                        "2222....",
                        "........",
                        "........",
                        "00000300"
                    )
                ),
                highlightCoords = setOf(GridCoord(7, 5)),
                highlightRows = listOf(7),
                showGravityArrow = false
            ),
            TutorialStep(
                number = 3,
                title = "Gravity Drops Straight Down",
                description = "Whenever lines clear, every floating block above empty squares falls vertically in its own column—never sideways.",
                tip = "Suspended shelves collapse down to pack the lower rows.",
                board = Board.fromPattern(
                    listOf(
                        "........",
                        "........",
                        "..00.11.",
                        "..00.11.",
                        "33...22.",
                        "334..22.",
                        "444.....",
                        "440....."
                    )
                ),
                highlightCoords = emptySet(),
                highlightRows = emptyList(),
                showGravityArrow = true
            ),
            TutorialStep(
                number = 4,
                title = "Ignite Cascade Chains",
                description = "When falling blocks complete a brand-new line automatically, Wave 2 triggers at ×2 points! Set up staggered ledges for ×3 and ×4 chains.",
                tip = "Clearing every block on the board awards a +150 point sweep bonus.",
                board = Board.fromPattern(
                    listOf(
                        "........",
                        "........",
                        "........",
                        "........",
                        "........",
                        "........",
                        "........",
                        "22221111"
                    )
                ),
                highlightCoords = emptySet(),
                highlightRows = listOf(7),
                showGravityArrow = true
            )
        )
    }

    val current = steps[demoStep.coerceIn(steps.indices)]

    CascadeScreenContainer(
        maxContentWidth = 860.dp,
        modifier = Modifier.testTag("how_to_play_screen")
    ) {
        ScreenTopBar(
            title = "How to Play",
            onBack = onBack,
            backTestTag = "how_to_play_back_button"
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCardElevated)
                    .border(1.dp, AccentGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${demoStep + 1} / ${steps.size}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp
                    ),
                    color = AccentGold
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 660.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        TutorialBoardCard(
                            current = current,
                            onAdvance = { demoStep = (demoStep + 1) % steps.size },
                            modifier = Modifier.weight(0.95f)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1.05f)
                        ) {
                            TutorialExplanationCard(
                                current = current,
                                totalSteps = steps.size,
                                selectedStep = demoStep,
                                onSelectStep = { demoStep = it },
                                onPrev = { demoStep = (demoStep - 1 + steps.size) % steps.size },
                                onNext = { demoStep = (demoStep + 1) % steps.size }
                            )
                            PremiumPrimaryButton(
                                text = "Got it!",
                                style = ArcadeButtonStyle.GOLD,
                                onClick = onBack,
                                height = 54.dp,
                                cornerRadius = 16.dp,
                                fontSize = 18.sp,
                                testTag = "how_to_play_got_it_button",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    TutorialBoardCard(
                        current = current,
                        onAdvance = { demoStep = (demoStep + 1) % steps.size }
                    )
                    TutorialExplanationCard(
                        current = current,
                        totalSteps = steps.size,
                        selectedStep = demoStep,
                        onSelectStep = { demoStep = it },
                        onPrev = { demoStep = (demoStep - 1 + steps.size) % steps.size },
                        onNext = { demoStep = (demoStep + 1) % steps.size }
                    )
                    PremiumPrimaryButton(
                        text = "Got it!",
                        style = ArcadeButtonStyle.GOLD,
                        onClick = onBack,
                        height = 54.dp,
                        cornerRadius = 16.dp,
                        fontSize = 18.sp,
                        testTag = "how_to_play_got_it_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialBoardCard(
    current: TutorialStep,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        cornerRadius = 22.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAdvance)
                .padding(14.dp)
                .testTag("how_to_play_demo_board")
        ) {
            Box(contentAlignment = Alignment.Center) {
                BoardPreview(
                    board = current.board,
                    highlightedCoords = current.highlightCoords,
                    highlightRows = current.highlightRows,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 220.dp)
                )
                if (current.showGravityArrow) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BoardWellBg.copy(alpha = 0.88f))
                            .border(1.5.dp, AccentGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Blocks fall straight down",
                            tint = AccentGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Text(
                text = "Tap board to preview next step",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun TutorialExplanationCard(
    current: TutorialStep,
    totalSteps: Int,
    selectedStep: Int,
    onSelectStep: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = AccentCyan.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentGold.copy(alpha = 0.16f))
                        .border(1.dp, AccentGold.copy(alpha = 0.45f), CircleShape)
                ) {
                    Text(
                        text = "${current.number}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AccentGold
                    )
                }
                Text(
                    text = current.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = TextPrimary
                )
            }

            Text(
                text = current.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BoardWellBg)
                    .border(1.dp, GlassBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tip: ${current.tip}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = AccentCyan
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onPrev)
                        .padding(horizontal = 10.dp)
                        .testTag("tutorial_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous step",
                        tint = TextPrimary
                    )
                    Text(
                        text = "Prev",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp
                        ),
                        color = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(totalSteps) { idx ->
                        val active = idx == selectedStep
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 22.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (active) AccentGold else TextSecondary.copy(alpha = 0.35f))
                                .clickable { onSelectStep(idx) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNext)
                        .padding(horizontal = 10.dp)
                        .testTag("tutorial_next_button")
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp
                        ),
                        color = AccentCyan
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next step",
                        tint = AccentCyan
                    )
                }
            }
        }
    }
}
