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
import com.example.ui.components.PremiumCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RulesScreen(
    onBack: () -> Unit
) {
    CascadeScreenContainer(
        maxContentWidth = 680.dp,
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
                kicker = "Basics",
                title = "Core Gameplay",
                accent = AccentCyan,
                bullets = listOf(
                    "The playfield is an 8×8 grid. You receive 3 polyomino pieces in the tray at a time.",
                    "Place all 3 pieces in any order to draw a fresh tray of 3 pieces.",
                    "Placed pieces stay fixed in position—blocks do NOT drop while placing."
                )
            )

            RuleSectionCard(
                kicker = "Cascades",
                title = "Line Clearing & Vertical Gravity",
                accent = AccentEmerald,
                bullets = listOf(
                    "Completing all 8 cells of any row or column clears that line immediately.",
                    "Intersecting rows and columns clear simultaneously within the same wave.",
                    "After lines clear, remaining blocks above empty squares fall straight DOWN within their column without moving sideways.",
                    "If falling blocks form new full lines, another Cascade Wave resolves automatically until the board settles."
                )
            )

            PremiumCard(
                cornerRadius = 20.dp,
                borderColor = AccentGold.copy(alpha = 0.42f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    SectionHeader(
                        kicker = "Points & Multipliers",
                        title = "Scoring Formula",
                        accent = AccentGold
                    )
                    ScoreRow("Place a Piece", "+1 pt / cell")
                    ScoreRow("Clear a Line", "+10 pts / line")
                    ScoreRow("Multi-Line Clear", "+15 pts / extra line")
                    ScoreRow("Gravity Cascade", "×Wave (×2, ×3, ×4...)")
                    ScoreRow("Cascade Chain Bonus", "+25 pts / step > 1")
                    ScoreRow("Full Board Clear", "+150 pts bonus")
                }
            }

            RuleSectionCard(
                kicker = "Progression",
                title = "Modes & Star Thresholds",
                accent = AccentCoral,
                bullets = listOf(
                    "Classic Endless: Play endlessly to beat your personal high score. Ends when no remaining tray piece fits on the board.",
                    "Challenge Campaign: 8 handcrafted tactical boards with target scores and move budgets. Earn 1★ at target, 2★ at +25% score or 3+ moves left, and 3★ at +50% score or 5+ moves left."
                )
            )

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun RuleSectionCard(
    kicker: String,
    title: String,
    accent: Color,
    bullets: List<String>
) {
    PremiumCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            SectionHeader(
                kicker = kicker,
                title = title,
                accent = accent
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
            .background(BoardWellBg)
            .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = TextPrimary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            ),
            color = AccentCyan
        )
    }
}
