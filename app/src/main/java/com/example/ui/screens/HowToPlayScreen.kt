package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Board
import com.example.game.GridCoord
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit,
    onOpenRules: () -> Unit
) {
    // 0 = Initial suspended blocks, 1 = Piece placed completing Row 7, 2 = Row 7 cleared & gravity drops Row 7 cascade, 3 = Cascade cleared!
    var demoStep by remember { mutableIntStateOf(0) }

    val demoBoard = remember(demoStep) {
        when (demoStep) {
            0 -> Board.fromPattern(
                listOf(
                    "........",
                    "........",
                    "........",
                    "........",
                    "....1111",
                    "2222....",
                    "........",
                    "00000.00"
                )
            )
            1 -> Board.fromPattern(
                listOf(
                    "........",
                    "........",
                    "........",
                    "........",
                    "....1111",
                    "2222....",
                    "........",
                    "00000300"
                )
            )
            2 -> Board.fromPattern(
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
            )
            else -> Board.empty()
        }
    }

    val demoCaption = when (demoStep) {
        0 -> "Step 1: Notice the gap at Row 7, Col 5, and two suspended shelves above it."
        1 -> "Step 2: Placing a block completes Row 7! It flashes and prepares to clear."
        2 -> "Step 3: Row 7 clears and gravity drops the suspended blocks straight DOWN, forming a brand-new full Row 7!"
        else -> "Step 4: Cascade Chain x2! The newly formed row clears automatically for bonus points."
    }

    val highlightCoords = when (demoStep) {
        0 -> setOf(GridCoord(7, 5))
        1 -> setOf(GridCoord(7, 5))
        else -> emptySet()
    }
    val highlightRows = when (demoStep) {
        1, 2 -> listOf(7)
        else -> emptyList()
    }

    CascadeScreenContainer(
        maxContentWidth = 640.dp,
        modifier = Modifier.testTag("how_to_play_screen")
    ) {
        ScreenTopBar(
            title = "How to Play",
            onBack = onBack,
            backTestTag = "how_to_play_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Interactive Gravity & Cascade Walkthrough
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, AccentCyan.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "INTERACTIVE CASCADE DEMO",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentCyan
                )

                BoardPreview(
                    board = demoBoard,
                    highlightedCoords = highlightCoords,
                    highlightRows = highlightRows,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 260.dp)
                )

                Text(
                    text = demoCaption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { demoStep = (demoStep + 1) % 4 },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("how_to_play_next_step_button")
                    ) {
                        Text(
                            text = if (demoStep == 3) "Restart Demo" else "Next Step (${demoStep + 1}/4)",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002026)
                        )
                    }
                }
            }

            // 4 Core Mechanics Explained
            GuideCard(
                step = "1. PLACEMENT",
                title = "Drag or Tap to Place",
                body = "Drag any of the 3 tray pieces onto the 8×8 grid—pieces float above your finger so you always see where they land. You can also tap a tray piece and tap a board cell.",
                accent = AccentCyan
            )
            GuideCard(
                step = "2. LINE CLEARS",
                title = "Fill Any Row or Column",
                body = "Complete all 8 squares across any horizontal row or vertical column to shatter those blocks. Clearing multiple lines at once awards extra multi-line bonuses.",
                accent = AccentEmerald
            )
            GuideCard(
                step = "3. DOWNWARD GRAVITY",
                title = "Columns Drop Straight Down",
                body = "Whenever a line clears, every remaining block sitting above empty space falls straight down inside its column until it lands on another block or the floor.",
                accent = AccentGold
            )
            GuideCard(
                step = "4. CASCADING CLEARS",
                title = "Chain Reactions Multiply Score",
                body = "If falling blocks form another complete row or column, the board clears again automatically! Each gravity wave multiplies your line clear score (x2, x3, x4...).",
                accent = AccentCoral
            )

            OutlinedButton(
                onClick = onOpenRules,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("how_to_play_rules_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Full Scoring & Rules Reference",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GuideCard(
    step: String,
    title: String,
    body: String,
    accent: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardElevated)
            .border(1.dp, BoardRim, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = step,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = accent
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = TextSecondary
        )
    }
}
