package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette

// Crisp Daylight Puzzle — Classic Bright Canvas & High-Contrast Board Palette
val StudioBgTop = Color(0xFFF6F8FC)
val StudioBgMid = Color(0xFFF1F5FB)
val StudioBgBottom = Color(0xFFE9EFF8)

val RoyalBlueTop = StudioBgTop
val RoyalBlueMid = StudioBgMid
val RoyalBlueBottom = StudioBgBottom

val ObsidianBg = Color(0xFF1E2B48)
val ObsidianGlow = Color(0xFF28395E)
val BoardWellBg = Color(0xFFEBF0F7)
val BoardRim = Color(0xFF2B3E66)
val EmptySlotFill = Color(0xFF28395E)
val EmptySlotBorder = Color(0xFF364B78)

val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceCardElevated = Color(0xFFF7F9FD)
val SurfaceElevatedHigh = Color(0xFFEEF3FA)
val GlassBorder = Color(0xFFD5DFED)
val DividerSubtle = Color(0xFFE4ECF5)
val GlassTopSheen = Color(0x20FFFFFF)

val TextPrimary = Color(0xFF17233B)
val TextSecondary = Color(0xFF566580)
val TextMuted = Color(0xFF7D8CA6)

val AccentCyan = Color(0xFF1D7AFC)
val AccentBlue = Color(0xFF2563EB)
val AccentGold = Color(0xFFE59500)
val AccentDiamond = Color(0xFF0EA5E9)
val AccentDiamondViolet = Color(0xFF7C3AED)
val AccentCoral = Color(0xFFE5484D)
val AccentWarning = Color(0xFFD97706)
val AccentEmerald = Color(0xFF16A34A)
val AccentPurple = Color(0xFF7C3AED)
val InvalidTint = Color(0xFFEF4444)

// Tactile Classic Puzzle Buttons & Subtle Bottom Depth Lips
val GoldBtnTop = Color(0xFFFFB71B)
val GoldBtnBottom = Color(0xFFF59E0B)
val GoldBtnLip = Color(0xFFC27200)
val GoldBtnText = Color(0xFF1A2234)

val BlueBtnTop = Color(0xFF3B82F6)
val BlueBtnBottom = Color(0xFF2563EB)
val BlueBtnLip = Color(0xFF1D4ED8)

val GreenBtnTop = Color(0xFF22C55E)
val GreenBtnBottom = Color(0xFF16A34A)
val GreenBtnLip = Color(0xFF15803D)

val RedBtnTop = Color(0xFFF43F5E)
val RedBtnBottom = Color(0xFFE11D48)
val RedBtnLip = Color(0xFFBE123C)

val SlateBtnTop = Color(0xFFFFFFFF)
val SlateBtnBottom = Color(0xFFF1F5F9)
val SlateBtnLip = Color(0xFFCBD5E1)

val SwitchTrackOn = Color(0xFF16A34A)
val SwitchTrackOff = Color(0xFFCBD5E1)

val LocalBlockStyle = compositionLocalOf { BlockStyle.MODERN }
val LocalBoardPalette = compositionLocalOf { BoardColorPalette.ROYAL }

data class BlockPalette(
    val fill: Color,
    val highlight: Color,
    val shadow: Color,
    val glow: Color
)

data class BoardThemeColors(
    val studioTop: Color,
    val studioMid: Color,
    val studioBottom: Color,
    val studioGlowPrimary: Color,
    val studioGlowSecondary: Color,
    val wellTop: Color,
    val wellBottom: Color,
    val rim: Color,
    val slotFill: Color,
    val slotBorder: Color,
    val trayTop: Color,
    val trayBottom: Color,
    val trayBorder: Color,
    val selectedSlotBg: Color,
    val slotBg: Color,
    val swatchPrimary: Color,
    val swatchSecondary: Color
)

fun BoardColorPalette.toBoardColors(): BoardThemeColors = when (this) {
    BoardColorPalette.ROYAL -> BoardThemeColors(
        studioTop = Color(0xFFF6F8FC),
        studioMid = Color(0xFFF0F4FB),
        studioBottom = Color(0xFFE7EEF8),
        studioGlowPrimary = Color(0xFF3B82F6),
        studioGlowSecondary = Color(0xFF0EA5E9),
        wellTop = Color(0xFF1B2B4B),
        wellBottom = Color(0xFF14213B),
        rim = Color(0xFF31497A),
        slotFill = Color(0xFF283E6B),
        slotBorder = Color(0xFF3B578F),
        trayTop = Color(0xFFFFFFFF),
        trayBottom = Color(0xFFF8FAFE),
        trayBorder = Color(0xFFD5DFED),
        selectedSlotBg = Color(0xFFEBF3FF),
        slotBg = Color(0xFFF1F5FB),
        swatchPrimary = Color(0xFF2563EB),
        swatchSecondary = Color(0xFF1B2B4B)
    )
    BoardColorPalette.MIDNIGHT -> BoardThemeColors(
        studioTop = Color(0xFFF3F5F9),
        studioMid = Color(0xFFEDEFF5),
        studioBottom = Color(0xFFE4E8F0),
        studioGlowPrimary = Color(0xFF475569),
        studioGlowSecondary = Color(0xFF64748B),
        wellTop = Color(0xFF1C2333),
        wellBottom = Color(0xFF141925),
        rim = Color(0xFF344059),
        slotFill = Color(0xFF273147),
        slotBorder = Color(0xFF3B4966),
        trayTop = Color(0xFFFFFFFF),
        trayBottom = Color(0xFFF7F8FB),
        trayBorder = Color(0xFFD3D9E5),
        selectedSlotBg = Color(0xFFE9EEF7),
        slotBg = Color(0xFFF1F3F8),
        swatchPrimary = Color(0xFF475569),
        swatchSecondary = Color(0xFF141925)
    )
    BoardColorPalette.EMERALD -> BoardThemeColors(
        studioTop = Color(0xFFF3F8F5),
        studioMid = Color(0xFFEDF4F0),
        studioBottom = Color(0xFFE3EEE8),
        studioGlowPrimary = Color(0xFF16A34A),
        studioGlowSecondary = Color(0xFF10B981),
        wellTop = Color(0xFF15382F),
        wellBottom = Color(0xFF0F2B24),
        rim = Color(0xFF275C4E),
        slotFill = Color(0xFF205144),
        slotBorder = Color(0xFF307362),
        trayTop = Color(0xFFFFFFFF),
        trayBottom = Color(0xFFF6FAF8),
        trayBorder = Color(0xFFCFE2D8),
        selectedSlotBg = Color(0xFFE7F6EE),
        slotBg = Color(0xFFF0F6F3),
        swatchPrimary = Color(0xFF16A34A),
        swatchSecondary = Color(0xFF0F2B24)
    )
    BoardColorPalette.SUNSET -> BoardThemeColors(
        studioTop = Color(0xFFFAF6F2),
        studioMid = Color(0xFFF5EEE8),
        studioBottom = Color(0xFFEEE3DA),
        studioGlowPrimary = Color(0xFFF97316),
        studioGlowSecondary = Color(0xFFF59E0B),
        wellTop = Color(0xFF28324E),
        wellBottom = Color(0xFF1E263D),
        rim = Color(0xFF43537D),
        slotFill = Color(0xFF344163),
        slotBorder = Color(0xFF4D5F8F),
        trayTop = Color(0xFFFFFFFF),
        trayBottom = Color(0xFFFCF9F6),
        trayBorder = Color(0xFFE6D9CF),
        selectedSlotBg = Color(0xFFFFF3E8),
        slotBg = Color(0xFFF8F2EC),
        swatchPrimary = Color(0xFFF97316),
        swatchSecondary = Color(0xFF1E263D)
    )
}

fun BlockColor.toPalette(
    boardPalette: BoardColorPalette = BoardColorPalette.ROYAL
): BlockPalette = when (boardPalette) {
    BoardColorPalette.ROYAL -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF22B8CF),
            highlight = Color(0xFF99E9F2),
            shadow = Color(0xFF0C8599),
            glow = Color(0xFF66D9E8)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFAB005),
            highlight = Color(0xFFFFEC99),
            shadow = Color(0xFFD97706),
            glow = Color(0xFFFFD43B)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFA5252),
            highlight = Color(0xFFFFB8B8),
            shadow = Color(0xFFC92A2A),
            glow = Color(0xFFFF8787)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF40C057),
            highlight = Color(0xFFB2F2BB),
            shadow = Color(0xFF2B8A3E),
            glow = Color(0xFF69DB7C)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFF7950F2),
            highlight = Color(0xFFD0BFFF),
            shadow = Color(0xFF5F3DC4),
            glow = Color(0xFF9775FA)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFD7E14),
            highlight = Color(0xFFFFD8A8),
            shadow = Color(0xFFD9480F),
            glow = Color(0xFFFFA94D)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF339AF0),
            highlight = Color(0xFFA5D8FF),
            shadow = Color(0xFF1864AB),
            glow = Color(0xFF74C0FC)
        )
    }

    BoardColorPalette.MIDNIGHT -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF06B6D4),
            highlight = Color(0xFFA5F3FC),
            shadow = Color(0xFF0E7490),
            glow = Color(0xFF67E8F9)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFEAB308),
            highlight = Color(0xFFFEF08A),
            shadow = Color(0xFFA16207),
            glow = Color(0xFFFDE047)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFF43F5E),
            highlight = Color(0xFFFECDD3),
            shadow = Color(0xFFBE123C),
            glow = Color(0xFFFB7185)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF10B981),
            highlight = Color(0xFFA7F3D0),
            shadow = Color(0xFF047857),
            glow = Color(0xFF34D399)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFF8B5CF6),
            highlight = Color(0xFFDDD6FE),
            shadow = Color(0xFF5B21B6),
            glow = Color(0xFFA78BFA)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFF97316),
            highlight = Color(0xFFFED7AA),
            shadow = Color(0xFFC2410C),
            glow = Color(0xFFFB923C)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF3B82F6),
            highlight = Color(0xFFBFDBFE),
            shadow = Color(0xFF1D4ED8),
            glow = Color(0xFF60A5FA)
        )
    }

    BoardColorPalette.EMERALD -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF14B8A6),
            highlight = Color(0xFF99F6E4),
            shadow = Color(0xFF0F766E),
            glow = Color(0xFF5EEAD4)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFF59E0B),
            highlight = Color(0xFFFDE68A),
            shadow = Color(0xFFB45309),
            glow = Color(0xFFFCD34D)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFEF4444),
            highlight = Color(0xFFFECACA),
            shadow = Color(0xFFB91C1C),
            glow = Color(0xFFF87171)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF22C55E),
            highlight = Color(0xFFBBF7D0),
            shadow = Color(0xFF15803D),
            glow = Color(0xFF4ADE80)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFF8B5CF6),
            highlight = Color(0xFFDDD6FE),
            shadow = Color(0xFF6D28D9),
            glow = Color(0xFFA78BFA)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFB923C),
            highlight = Color(0xFFFFEDD5),
            shadow = Color(0xFFC2410C),
            glow = Color(0xFFFDBA74)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF0EA5E9),
            highlight = Color(0xFFBAE6FD),
            shadow = Color(0xFF0369A1),
            glow = Color(0xFF38BDF8)
        )
    }

    BoardColorPalette.SUNSET -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF38BDF8),
            highlight = Color(0xFFE0F2FE),
            shadow = Color(0xFF0284C7),
            glow = Color(0xFF7DD3FC)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFBBF24),
            highlight = Color(0xFFFEF3C7),
            shadow = Color(0xFFD97706),
            glow = Color(0xFFFDE68A)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFB7185),
            highlight = Color(0xFFFFE4E6),
            shadow = Color(0xFFE11D48),
            glow = Color(0xFFFDA4AF)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF34D399),
            highlight = Color(0xFFD1FAE5),
            shadow = Color(0xFF059669),
            glow = Color(0xFF6EE7B7)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFFA855F7),
            highlight = Color(0xFFF3E8FF),
            shadow = Color(0xFF7E22CE),
            glow = Color(0xFFC084FC)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFF97316),
            highlight = Color(0xFFFFEDD5),
            shadow = Color(0xFFEA580C),
            glow = Color(0xFFFB923C)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF6366F1),
            highlight = Color(0xFFE0E7FF),
            shadow = Color(0xFF4338CA),
            glow = Color(0xFF818CF8)
        )
    }
}
