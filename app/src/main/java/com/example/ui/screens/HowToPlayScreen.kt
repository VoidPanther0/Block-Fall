package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Board
import com.example.game.GridCoord
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.OutfitFontFamily

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    // 0 = Gravity fall diagram (matching mockup), 1 = Placed piece completing Row 7, 2 = Cascade dropped & cleared
    var demoStep by remember { mutableIntStateOf(0) }

    val demoBoard = remember(demoStep) {
        when (demoStep) {
            0 -> Board.fromPattern(
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
            )
            1 -> Board.fromPattern(
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
            )
            else -> Board.fromPattern(
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
        }
    }

    val highlightCoords = when (demoStep) {
        1 -> setOf(GridCoord(7, 5))
        else -> emptySet()
    }
    val highlightRows = when (demoStep) {
        1, 2 -> listOf(7)
        else -> emptyList()
    }

    CascadeScreenContainer(
        maxContentWidth = 540.dp,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FrostedGlassCard(
                cornerRadius = 22.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { demoStep = (demoStep + 1) % 3 }
                        .padding(14.dp)
                        .testTag("how_to_play_demo_board")
                ) {
                    BoardPreview(
                        board = demoBoard,
                        highlightedCoords = highlightCoords,
                        highlightRows = highlightRows,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 230.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "Blocks fall straight down",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                NumberedGuideRow(
                    number = 1,
                    title = "Place the blocks."
                )
                NumberedGuideRow(
                    number = 2,
                    title = "Clear rows or columns."
                )
                NumberedGuideRow(
                    number = 3,
                    title = "Remaining blocks\nfall down!"
                )
            }

            Spacer(Modifier.weight(1f, fill = false))

            GlossyArcadeButton(
                text = "Got it!",
                style = ArcadeButtonStyle.BLUE,
                onClick = onBack,
                height = 54.dp,
                cornerRadius = 27.dp,
                fontSize = 18.sp,
                testTag = "how_to_play_got_it_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NumberedGuideRow(
    number: Int,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom)))
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                ),
                color = Color.White
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ),
            color = Color.White
        )
    }
}
