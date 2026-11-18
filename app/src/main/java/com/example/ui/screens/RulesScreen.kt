package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun RulesScreen(
    onBack: () -> Unit
) {
    CascadeScreenContainer(
        maxContentWidth = 620.dp,
        modifier = Modifier.testTag("rules_screen")
    ) {
        ScreenTopBar(
            title = "Rules & Scoring",
            onBack = onBack,
            backTestTag = "rules_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RuleSectionCard(
                title = "Core Gameplay",
                accent = AccentCyan,
                bullets = listOf(
                    "The board is an 8×8 grid. You receive 3 polyomino pieces in the tray at a time.",
                    "Place all 3 pieces in any order to receive a fresh set of 3 pieces.",
                    "Placed pieces stay fixed in position—blocks do NOT fall while placing."
                )
            )

            RuleSectionCard(
                title = "Line Clearing & Gravity",
                accent = AccentEmerald,
                bullets = listOf(
                    "Filling all 8 cells of any row or column clears that line immediately.",
                    "Intersecting rows and columns clear simultaneously in the same wave.",
                    "After lines clear, every remaining block above empty squares falls straight DOWN within its column without moving sideways.",
                    "If falling blocks complete new rows or columns, another Cascade Wave triggers automatically until the board settles."
                )
            )

            FrostedGlassCard(
                cornerRadius = 20.dp,
                borderColor = AccentGold.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "SCORING FORMULA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = AccentGold
                    )
                    ScoreRow("Place a Piece", "+1 pt per cell placed")
                    ScoreRow("Clear a Line", "+10 pts per line")
                    ScoreRow("Multi-Line Clear", "+15 pts per extra line in wave")
                    ScoreRow("Gravity Cascade Multiplier", "×Wave Index (×2, ×3, ×4...)")
                    ScoreRow("Cascade Chain Bonus", "+25 pts per chain step above wave 1")
                    ScoreRow("Full Board Clear", "+150 pts bonus")
                }
            }

            RuleSectionCard(
                title = "Game Modes & Win Conditions",
                accent = AccentCoral,
                bullets = listOf(
                    "Classic Mode: Play endlessly to beat your high score. The game ends when none of your remaining tray pieces fit anywhere on the board. Progress is auto-saved after every turn.",
                    "Challenge Mode: 8 handcrafted puzzles with target scores and move limits. Reach the target score within the move limit to clear the level and unlock the next one.",
                    "Stars: Earn 1★ for reaching the target score, 2★ for +25% score or 3+ moves left, and 3★ for +50% score or 5+ moves left."
                )
            )
        }
    }
}

@Composable
private fun RuleSectionCard(
    title: String,
    accent: Color,
    bullets: List<String>
) {
    FrostedGlassCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = accent
            )
            bullets.forEach { bullet ->
                Text(
                    text = "• $bullet",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCardElevated.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color.White
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = AccentGold
        )
    }
}
