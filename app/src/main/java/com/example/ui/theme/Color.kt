package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.game.BlockColor

// Royal Arcade Sky & Frosted Glass palette
val RoyalBlueTop = Color(0xFF142E75)
val RoyalBlueMid = Color(0xFF1D439E)
val RoyalBlueBottom = Color(0xFF0E2054)

val ObsidianBg = RoyalBlueBottom
val ObsidianGlow = RoyalBlueMid
val BoardWellBg = Color(0xFF14275D)
val BoardRim = Color(0xFF3961BD)
val EmptySlotFill = Color(0xFF1B3373)
val EmptySlotBorder = Color(0xFF284898)

val SurfaceCard = Color(0xFF183276)
val SurfaceCardElevated = Color(0xFF214091)
val GlassBorder = Color(0xFF4B75D6)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFBBD0FF)

val AccentCyan = Color(0xFF34C5FF)
val AccentGold = Color(0xFFFFCA28)
val AccentCoral = Color(0xFFF14B59)
val AccentEmerald = Color(0xFF38D430)
val AccentPurple = Color(0xFF7D4AE6)
val InvalidTint = Color(0xFFFF455B)

// Glossy 3D Button Gradients & Bottom Lips
val GoldBtnTop = Color(0xFFFFDC3D)
val GoldBtnBottom = Color(0xFFF59E0B)
val GoldBtnLip = Color(0xFFC06E00)
val GoldBtnText = Color(0xFF1E263D)

val BlueBtnTop = Color(0xFF43BCFF)
val BlueBtnBottom = Color(0xFF1978EC)
val BlueBtnLip = Color(0xFF0B4DB3)

val GreenBtnTop = Color(0xFF56E33F)
val GreenBtnBottom = Color(0xFF1BA824)
val GreenBtnLip = Color(0xFF0E6C14)

val RedBtnTop = Color(0xFFF55862)
val RedBtnBottom = Color(0xFFC92935)
val RedBtnLip = Color(0xFF8C141E)

val PurpleTileTop = Color(0xFF9155F0)
val PurpleTileBottom = Color(0xFF5B35B5)

val GreenTileTop = Color(0xFF45D83A)
val GreenTileBottom = Color(0xFF1A9627)

val SwitchTrackOn = Color(0xFF2ED53D)
val SwitchTrackOff = Color(0xFF2A458E)

data class BlockPalette(
    val fill: Color,
    val highlight: Color,
    val shadow: Color,
    val glow: Color
)

fun BlockColor.toPalette(): BlockPalette = when (this) {
    BlockColor.CYAN -> BlockPalette(
        fill = Color(0xFF23C4FF),
        highlight = Color(0xFFA8EBFF),
        shadow = Color(0xFF0874C2),
        glow = Color(0xFF4CD6FF)
    )
    BlockColor.AMBER -> BlockPalette(
        fill = Color(0xFFFFC619),
        highlight = Color(0xFFFFF099),
        shadow = Color(0xFFC97B00),
        glow = Color(0xFFFFD84D)
    )
    BlockColor.CORAL -> BlockPalette(
        fill = Color(0xFFFF424E),
        highlight = Color(0xFFFFA3AA),
        shadow = Color(0xFFB31726),
        glow = Color(0xFFFF5E6B)
    )
    BlockColor.EMERALD -> BlockPalette(
        fill = Color(0xFF39D82E),
        highlight = Color(0xFFA9FC9F),
        shadow = Color(0xFF158A14),
        glow = Color(0xFF5BF050)
    )
    BlockColor.VIOLET -> BlockPalette(
        fill = Color(0xFF9C4DF4),
        highlight = Color(0xFFD6B4FF),
        shadow = Color(0xFF5E1EB5),
        glow = Color(0xFFB778FF)
    )
    BlockColor.TANGERINE -> BlockPalette(
        fill = Color(0xFFFF7B1C),
        highlight = Color(0xFFFFC599),
        shadow = Color(0xFFC24300),
        glow = Color(0xFFFF9547)
    )
    BlockColor.COBALT -> BlockPalette(
        fill = Color(0xFF2B7FFF),
        highlight = Color(0xFF9AC5FF),
        shadow = Color(0xFF1146B8),
        glow = Color(0xFF529BFF)
    )
}
