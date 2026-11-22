package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette

// Prism Atelier — Deep Midnight Slate & Luminous Neo-Glass Studio palette
val StudioBgTop = Color(0xFF0A1024)
val StudioBgMid = Color(0xFF111C3D)
val StudioBgBottom = Color(0xFF070B18)

val RoyalBlueTop = StudioBgTop
val RoyalBlueMid = StudioBgMid
val RoyalBlueBottom = StudioBgBottom

val ObsidianBg = StudioBgBottom
val ObsidianGlow = StudioBgMid
val BoardWellBg = Color(0xFF0D152E)
val BoardRim = Color(0xFF2D4585)
val EmptySlotFill = Color(0xFF142044)
val EmptySlotBorder = Color(0xFF223468)

val SurfaceCard = Color(0xFF141E3E)
val SurfaceCardElevated = Color(0xFF1D2B57)
val SurfaceElevatedHigh = Color(0xFF24366B)
val GlassBorder = Color(0xFF3E5BA8)
val DividerSubtle = Color(0xFF283C75)
val GlassTopSheen = Color(0x28FFFFFF)

val TextPrimary = Color(0xFFF6F8FF)
val TextSecondary = Color(0xFFA4B5E0)
val TextMuted = Color(0xFF7D90C4)

val AccentCyan = Color(0xFF2EE6FF)
val AccentBlue = Color(0xFF3B82F6)
val AccentGold = Color(0xFFFFCA3A)
val AccentDiamond = Color(0xFF6CE5FF)
val AccentDiamondViolet = Color(0xFFA78BFA)
val AccentCoral = Color(0xFFFF4D6D)
val AccentWarning = Color(0xFFFF9F43)
val AccentEmerald = Color(0xFF2EE59D)
val AccentPurple = Color(0xFF9B6BFF)
val InvalidTint = Color(0xFFFF4D6D)

// Sculpted Tactile Button Gradients & Bottom Depth Lips
val GoldBtnTop = Color(0xFFFFDC5E)
val GoldBtnBottom = Color(0xFFF59E0B)
val GoldBtnLip = Color(0xFFB46300)
val GoldBtnText = Color(0xFF141B30)

val BlueBtnTop = Color(0xFF3CC7FF)
val BlueBtnBottom = Color(0xFF1E6BFF)
val BlueBtnLip = Color(0xFF1042A8)

val GreenBtnTop = Color(0xFF42EB9B)
val GreenBtnBottom = Color(0xFF12B76A)
val GreenBtnLip = Color(0xFF097643)

val RedBtnTop = Color(0xFFFF617D)
val RedBtnBottom = Color(0xFFD92649)
val RedBtnLip = Color(0xFF8F112B)

val SlateBtnTop = Color(0xFF2A3A6E)
val SlateBtnBottom = Color(0xFF1B274F)
val SlateBtnLip = Color(0xFF0E1633)

val SwitchTrackOn = Color(0xFF1FD183)
val SwitchTrackOff = Color(0xFF233261)

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
        studioTop = Color(0xFF0B1228),
        studioMid = Color(0xFF14214A),
        studioBottom = Color(0xFF080D1D),
        studioGlowPrimary = Color(0xFF2E6BFF),
        studioGlowSecondary = Color(0xFF2EE6FF),
        wellTop = Color(0xFF13204A),
        wellBottom = Color(0xFF0B132E),
        rim = Color(0xFF395AB8),
        slotFill = Color(0xFF172859),
        slotBorder = Color(0xFF263E80),
        trayTop = Color(0xFF172552).copy(alpha = 0.92f),
        trayBottom = Color(0xFF0D1635).copy(alpha = 0.96f),
        trayBorder = Color(0xFF4267C7).copy(alpha = 0.55f),
        selectedSlotBg = Color(0xFF223775),
        slotBg = Color(0xFF101B3D).copy(alpha = 0.85f),
        swatchPrimary = Color(0xFF3370FF),
        swatchSecondary = Color(0xFF0D1635)
    )
    BoardColorPalette.MIDNIGHT -> BoardThemeColors(
        studioTop = Color(0xFF080A14),
        studioMid = Color(0xFF111629),
        studioBottom = Color(0xFF05070E),
        studioGlowPrimary = Color(0xFF5C74B8),
        studioGlowSecondary = Color(0xFF00E5FF),
        wellTop = Color(0xFF151C30),
        wellBottom = Color(0xFF0A0E1B),
        rim = Color(0xFF4C6094),
        slotFill = Color(0xFF182036),
        slotBorder = Color(0xFF2A3659),
        trayTop = Color(0xFF182036).copy(alpha = 0.94f),
        trayBottom = Color(0xFF0B0F1C).copy(alpha = 0.97f),
        trayBorder = Color(0xFF5369A3).copy(alpha = 0.58f),
        selectedSlotBg = Color(0xFF253257),
        slotBg = Color(0xFF0F1424).copy(alpha = 0.88f),
        swatchPrimary = Color(0xFF5369A3),
        swatchSecondary = Color(0xFF0A0E1B)
    )
    BoardColorPalette.EMERALD -> BoardThemeColors(
        studioTop = Color(0xFF061B1A),
        studioMid = Color(0xFF0C2E2B),
        studioBottom = Color(0xFF041211),
        studioGlowPrimary = Color(0xFF1FD19B),
        studioGlowSecondary = Color(0xFF4CE8C6),
        wellTop = Color(0xFF0F3833),
        wellBottom = Color(0xFF08211E),
        rim = Color(0xFF2BA88F),
        slotFill = Color(0xFF13423C),
        slotBorder = Color(0xFF21695F),
        trayTop = Color(0xFF12403A).copy(alpha = 0.92f),
        trayBottom = Color(0xFF09221F).copy(alpha = 0.96f),
        trayBorder = Color(0xFF2BA88F).copy(alpha = 0.58f),
        selectedSlotBg = Color(0xFF1A5950),
        slotBg = Color(0xFF0A2925).copy(alpha = 0.86f),
        swatchPrimary = Color(0xFF2BA88F),
        swatchSecondary = Color(0xFF08211E)
    )
    BoardColorPalette.SUNSET -> BoardThemeColors(
        studioTop = Color(0xFF1D0A28),
        studioMid = Color(0xFF331347),
        studioBottom = Color(0xFF12061A),
        studioGlowPrimary = Color(0xFFFF4D8D),
        studioGlowSecondary = Color(0xFFFFB84D),
        wellTop = Color(0xFF3E1854),
        wellBottom = Color(0xFF220C30),
        rim = Color(0xFFD65A92),
        slotFill = Color(0xFF471C5E),
        slotBorder = Color(0xFF702E82),
        trayTop = Color(0xFF451B5C).copy(alpha = 0.92f),
        trayBottom = Color(0xFF230D31).copy(alpha = 0.96f),
        trayBorder = Color(0xFFD65A92).copy(alpha = 0.58f),
        selectedSlotBg = Color(0xFF5D267A),
        slotBg = Color(0xFF290F38).copy(alpha = 0.86f),
        swatchPrimary = Color(0xFFD65A92),
        swatchSecondary = Color(0xFF220C30)
    )
}

fun BlockColor.toPalette(
    boardPalette: BoardColorPalette = BoardColorPalette.ROYAL
): BlockPalette = when (boardPalette) {
    BoardColorPalette.ROYAL -> when (this) {
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
            fill = Color(0xFFFF4258),
            highlight = Color(0xFFFFA6B2),
            shadow = Color(0xFFB3172B),
            glow = Color(0xFFFF6174)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF2EE070),
            highlight = Color(0xFFA7FAD0),
            shadow = Color(0xFF0F8E43),
            glow = Color(0xFF52F291)
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

    BoardColorPalette.MIDNIGHT -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF00E5FF),
            highlight = Color(0xFFB8FFFF),
            shadow = Color(0xFF00839E),
            glow = Color(0xFF5CF2FF)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFFD426),
            highlight = Color(0xFFFFF4A8),
            shadow = Color(0xFFB88600),
            glow = Color(0xFFFFE159)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF2E63),
            highlight = Color(0xFFFF9DB7),
            shadow = Color(0xFFA80D37),
            glow = Color(0xFFFF5C87)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF08D98E),
            highlight = Color(0xFF9EFFDB),
            shadow = Color(0xFF048758),
            glow = Color(0xFF42F5B3)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFFB366FF),
            highlight = Color(0xFFE3C2FF),
            shadow = Color(0xFF6B24B8),
            glow = Color(0xFFC98FFF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF8833),
            highlight = Color(0xFFFFCC9E),
            shadow = Color(0xFFB54B00),
            glow = Color(0xFFFFA05C)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF4D94FF),
            highlight = Color(0xFFB3D2FF),
            shadow = Color(0xFF1952B3),
            glow = Color(0xFF75ADFF)
        )
    }

    BoardColorPalette.EMERALD -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF2CE6C4),
            highlight = Color(0xFFB0FFEE),
            shadow = Color(0xFF0E8F79),
            glow = Color(0xFF5CF2D6)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFF7C931),
            highlight = Color(0xFFFFF1A8),
            shadow = Color(0xFFB5860B),
            glow = Color(0xFFFFDA61)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF5C5C),
            highlight = Color(0xFFFFB8B8),
            shadow = Color(0xFFB52222),
            glow = Color(0xFFFF7A7A)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF67E83A),
            highlight = Color(0xFFC7FFAE),
            shadow = Color(0xFF2A9912),
            glow = Color(0xFF88F562)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFFA778FF),
            highlight = Color(0xFFDDC9FF),
            shadow = Color(0xFF6236B5),
            glow = Color(0xFFBC96FF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF9826),
            highlight = Color(0xFFFFD4A3),
            shadow = Color(0xFFBD5D00),
            glow = Color(0xFFFFAE52)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF3FA7F5),
            highlight = Color(0xFFAEDBFF),
            shadow = Color(0xFF1760A8),
            glow = Color(0xFF68BDFF)
        )
    }

    BoardColorPalette.SUNSET -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF4FD1FF),
            highlight = Color(0xFFBDEFFF),
            shadow = Color(0xFF1480B8),
            glow = Color(0xFF75DCFF)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFFD13B),
            highlight = Color(0xFFFFF3B5),
            shadow = Color(0xFFC48B08),
            glow = Color(0xFFFFDF66)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF407D),
            highlight = Color(0xFFFFA8C4),
            shadow = Color(0xFFB01348),
            glow = Color(0xFFFF6698)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF47DF8C),
            highlight = Color(0xFFB5FFD6),
            shadow = Color(0xFF169150),
            glow = Color(0xFF6EEDA7)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFFC75CFF),
            highlight = Color(0xFFEBB8FF),
            shadow = Color(0xFF7F1EB5),
            glow = Color(0xFFD882FF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF6E38),
            highlight = Color(0xFFFFBCA3),
            shadow = Color(0xFFB8320A),
            glow = Color(0xFFFF8F63)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF6178FF),
            highlight = Color(0xFFB8C3FF),
            shadow = Color(0xFF283BB5),
            glow = Color(0xFF8496FF)
        )
    }
}
