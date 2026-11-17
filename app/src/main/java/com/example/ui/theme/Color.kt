package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.game.BlockColor

// Kinetic Obsidian Arcade palette
val ObsidianBg = Color(0xFF0D111A)
val ObsidianGlow = Color(0xFF162035)
val BoardWellBg = Color(0xFF121724)
val BoardRim = Color(0xFF28344E)
val EmptySlotFill = Color(0xFF192132)
val EmptySlotBorder = Color(0xFF232E45)
val SurfaceCard = Color(0xFF171E2E)
val SurfaceCardElevated = Color(0xFF1F283D)
val TextPrimary = Color(0xFFF3F6FC)
val TextSecondary = Color(0xFF93A1BF)
val AccentCyan = Color(0xFF00D8F6)
val AccentGold = Color(0xFFFFC13B)
val AccentCoral = Color(0xFFFF4D6D)
val AccentEmerald = Color(0xFF10E594)
val InvalidTint = Color(0xFFFF3B5C)

data class BlockPalette(
    val fill: Color,
    val highlight: Color,
    val shadow: Color,
    val glow: Color
)

fun BlockColor.toPalette(): BlockPalette = when (this) {
    BlockColor.CYAN -> BlockPalette(
        fill = Color(0xFF00D4F2),
        highlight = Color(0xFF8FF5FF),
        shadow = Color(0xFF007B99),
        glow = Color(0xFF00E5FF)
    )
    BlockColor.AMBER -> BlockPalette(
        fill = Color(0xFFFFB703),
        highlight = Color(0xFFFFE680),
        shadow = Color(0xFFB37400),
        glow = Color(0xFFFFCC33)
    )
    BlockColor.CORAL -> BlockPalette(
        fill = Color(0xFFFF476F),
        highlight = Color(0xFFFF9EB3),
        shadow = Color(0xFFA8193B),
        glow = Color(0xFFFF5C80)
    )
    BlockColor.EMERALD -> BlockPalette(
        fill = Color(0xFF12E08E),
        highlight = Color(0xFF8CFBD0),
        shadow = Color(0xFF078A55),
        glow = Color(0xFF24F5A2)
    )
    BlockColor.VIOLET -> BlockPalette(
        fill = Color(0xFF9B5DE5),
        highlight = Color(0xFFD2B0FF),
        shadow = Color(0xFF5A25A1),
        glow = Color(0xFFB47EFF)
    )
    BlockColor.TANGERINE -> BlockPalette(
        fill = Color(0xFFFF7A2F),
        highlight = Color(0xFFFFBD94),
        shadow = Color(0xFFB54106),
        glow = Color(0xFFFF8F4D)
    )
    BlockColor.COBALT -> BlockPalette(
        fill = Color(0xFF3A86FF),
        highlight = Color(0xFF9BC2FF),
        shadow = Color(0xFF134EB5),
        glow = Color(0xFF5C9CFF)
    )
}
