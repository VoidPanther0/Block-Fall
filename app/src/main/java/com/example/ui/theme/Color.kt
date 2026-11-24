package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette

// PlayStation Design System Tokens (per DESIGN.md)
val PsBlue = Color(0xFF0070D1)
val PsBluePressed = Color(0xFF0064B7)
val PsBlueActive = Color(0xFF004D8D)
val PsCommerce = Color(0xFFD53B00)
val PsCommercePressed = Color(0xFFAA2F00)
val CanvasDark = Color(0xFF000000)
val SurfaceDarkElevated = Color(0xFF121314)
val SurfaceDarkCard = Color(0xFF181818)
val Charcoal = Color(0xFF1F2024)
val HairlineDark = Color(0x33E5E5E5)
val HairlineLight = Color(0xFFF3F3F3)
val CanvasLight = Color(0xFFFFFFFF)
val SurfaceCardLight = Color(0xFFF5F7FA)
val SurfaceSoft = Color(0xFFF3F3F3)
val BodyDark = Color(0xB3FFFFFF) // 70% white
val MuteDark = Color(0x8CE5E5E5) // 55% white
val PsGoldStart = Color(0xFFFFCE21)
val PsGoldMid = Color(0xFFF5A623)
val PsGoldEnd = Color(0xFFEE8E00)
val MarathonYellow = Color(0xFFDEFF20)
val WarningRed = Color(0xFFC81B3A)

// Surfaces mapped to PlayStation Dark Canvas System
val StudioBgTop = CanvasDark
val StudioBgMid = Color(0xFF08090A)
val StudioBgBottom = SurfaceDarkElevated

val RoyalBlueTop = StudioBgTop
val RoyalBlueMid = StudioBgMid
val RoyalBlueBottom = StudioBgBottom

val ObsidianBg = CanvasDark
val ObsidianGlow = Color(0xFF0A1E3D)
val BoardWellBg = Color(0xFF0E0F12)
val BoardRim = PsBlue
val EmptySlotFill = Color(0xFF15161A)
val EmptySlotBorder = Color(0xFF23252C)

val SurfaceCard = SurfaceDarkCard
val SurfaceCardElevated = SurfaceDarkElevated
val SurfaceElevatedHigh = Charcoal
val GlassBorder = HairlineDark
val DividerSubtle = Color(0x1FFFFFFF)
val GlassTopSheen = Color(0x1AFFFFFF)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = BodyDark
val TextMuted = MuteDark

val AccentCyan = Color(0xFF53B1FF)
val AccentBlue = PsBlue
val AccentGold = PsGoldMid
val AccentGoldBright = PsGoldStart
val AccentGoldDeep = PsGoldEnd
val AccentDiamond = Color(0xFF53B1FF)
val AccentDiamondViolet = Color(0xFFA855F7)
val AccentCoral = Color(0xFFFF4D6D)
val AccentWarning = Color(0xFFFF9F1C)
val AccentEmerald = Color(0xFF22E584)
val AccentPurple = Color(0xFF8F52FF)
val InvalidTint = WarningRed

// PlayStation CTA Buttons (Pills, Flat Depth per DESIGN.md)
val GoldBtnTop = PsGoldStart
val GoldBtnBottom = PsGoldMid
val GoldBtnLip = PsGoldEnd
val GoldBtnText = Color(0xFF121314)

val BlueBtnTop = PsBlue
val BlueBtnBottom = PsBluePressed
val BlueBtnLip = PsBlueActive

val CommerceBtnTop = PsCommerce
val CommerceBtnBottom = PsCommercePressed
val CommerceBtnLip = Color(0xFF882400)

val PurpleBtnTop = Color(0xFF8247FF)
val PurpleBtnBottom = Color(0xFF5B1ED9)
val PurpleBtnLip = Color(0xFF3E0F9E)

val GreenBtnTop = Color(0xFF2AE67A)
val GreenBtnBottom = Color(0xFF0CA34A)
val GreenBtnLip = Color(0xFF06632B)

val RedBtnTop = Color(0xFFFF4763)
val RedBtnBottom = Color(0xFFD9163B)
val RedBtnLip = Color(0xFF870921)

val SlateBtnTop = Charcoal
val SlateBtnBottom = SurfaceDarkCard
val SlateBtnLip = SurfaceDarkElevated

val SwitchTrackOn = PsBlue
val SwitchTrackOff = Charcoal

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
        studioTop = Color(0xFF050A1E),
        studioMid = Color(0xFF0A153A),
        studioBottom = Color(0xFF122662),
        studioGlowPrimary = Color(0xFF26D0FF),
        studioGlowSecondary = Color(0xFF8F45FF),
        wellTop = Color(0xFF0A1538),
        wellBottom = Color(0xFF070F2B),
        rim = Color(0xFF249EFF),
        slotFill = Color(0xFF0F2052),
        slotBorder = Color(0xFF22428F),
        trayTop = Color(0xFF122359),
        trayBottom = Color(0xFF0B173D),
        trayBorder = Color(0xFF2C55B5),
        selectedSlotBg = Color(0xFF1B3A8C),
        slotBg = Color(0xFF0B163C),
        swatchPrimary = Color(0xFF2474FF),
        swatchSecondary = Color(0xFF0A1538)
    )
    BoardColorPalette.MIDNIGHT -> BoardThemeColors(
        studioTop = Color(0xFF050712),
        studioMid = Color(0xFF0B1024),
        studioBottom = Color(0xFF141B3B),
        studioGlowPrimary = Color(0xFF477BFF),
        studioGlowSecondary = Color(0xFF7B4DFF),
        wellTop = Color(0xFF0B1024),
        wellBottom = Color(0xFF070A18),
        rim = Color(0xFF4E67B8),
        slotFill = Color(0xFF121A38),
        slotBorder = Color(0xFF28376B),
        trayTop = Color(0xFF131B3B),
        trayBottom = Color(0xFF0B1024),
        trayBorder = Color(0xFF334685),
        selectedSlotBg = Color(0xFF1E2B5C),
        slotBg = Color(0xFF0A0E21),
        swatchPrimary = Color(0xFF477BFF),
        swatchSecondary = Color(0xFF070A18)
    )
    BoardColorPalette.EMERALD -> BoardThemeColors(
        studioTop = Color(0xFF041418),
        studioMid = Color(0xFF082229),
        studioBottom = Color(0xFF0E3642),
        studioGlowPrimary = Color(0xFF22E584),
        studioGlowSecondary = Color(0xFF19C9E6),
        wellTop = Color(0xFF072126),
        wellBottom = Color(0xFF041519),
        rim = Color(0xFF1FD495),
        slotFill = Color(0xFF0D2F36),
        slotBorder = Color(0xFF1D5863),
        trayTop = Color(0xFF0D2E36),
        trayBottom = Color(0xFF071D22),
        trayBorder = Color(0xFF216E73),
        selectedSlotBg = Color(0xFF12474D),
        slotBg = Color(0xFF061B20),
        swatchPrimary = Color(0xFF22E584),
        swatchSecondary = Color(0xFF041519)
    )
    BoardColorPalette.SUNSET -> BoardThemeColors(
        studioTop = Color(0xFF120826),
        studioMid = Color(0xFF1E0E40),
        studioBottom = Color(0xFF2D165E),
        studioGlowPrimary = Color(0xFFFF9E2C),
        studioGlowSecondary = Color(0xFFB54CFF),
        wellTop = Color(0xFF190C36),
        wellBottom = Color(0xFF100724),
        rim = Color(0xFFFFA834),
        slotFill = Color(0xFF24134D),
        slotBorder = Color(0xFF46288C),
        trayTop = Color(0xFF24124A),
        trayBottom = Color(0xFF160B30),
        trayBorder = Color(0xFF5D36B5),
        selectedSlotBg = Color(0xFF391D75),
        slotBg = Color(0xFF14092E),
        swatchPrimary = Color(0xFFFF9E2C),
        swatchSecondary = Color(0xFF100724)
    )
}

fun BlockColor.toPalette(
    boardPalette: BoardColorPalette = BoardColorPalette.ROYAL
): BlockPalette = when (boardPalette) {
    BoardColorPalette.ROYAL -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF00C8FF),
            highlight = Color(0xFFBDF4FF),
            shadow = Color(0xFF0074B8),
            glow = Color(0xFF5CE1FF)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFFCE00),
            highlight = Color(0xFFFFF6B3),
            shadow = Color(0xFFD97A00),
            glow = Color(0xFFFFE14D)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF2E4D),
            highlight = Color(0xFFFFB8C3),
            shadow = Color(0xFFA80A24),
            glow = Color(0xFFFF6B81)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF2CE622),
            highlight = Color(0xFFC2FFB8),
            shadow = Color(0xFF0E9608),
            glow = Color(0xFF70FF66)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFF993DFF),
            highlight = Color(0xFFE0C2FF),
            shadow = Color(0xFF5712B8),
            glow = Color(0xFFB878FF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF7A00),
            highlight = Color(0xFFFFD8AD),
            shadow = Color(0xFFBA4000),
            glow = Color(0xFFFFA64D)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF197BFF),
            highlight = Color(0xFFAAD2FF),
            shadow = Color(0xFF093EB5),
            glow = Color(0xFF5CA3FF)
        )
    }

    BoardColorPalette.MIDNIGHT -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF06C7E8),
            highlight = Color(0xFFB3F7FF),
            shadow = Color(0xFF097696),
            glow = Color(0xFF67EAFF)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFF5C400),
            highlight = Color(0xFFFFF3A8),
            shadow = Color(0xFFB37400),
            glow = Color(0xFFFFE047)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF335E),
            highlight = Color(0xFFFFC2CE),
            shadow = Color(0xFFAD0E32),
            glow = Color(0xFFFF708F)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF1CE07A),
            highlight = Color(0xFFB3FFDA),
            shadow = Color(0xFF078A46),
            glow = Color(0xFF57F5A4)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFF9C5CFF),
            highlight = Color(0xFFE4D1FF),
            shadow = Color(0xFF5B1FC4),
            glow = Color(0xFFBC8FFF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF7D1A),
            highlight = Color(0xFFFFDAB8),
            shadow = Color(0xFFB84300),
            glow = Color(0xFFFFA35C)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF3385FF),
            highlight = Color(0xFFC2DCFF),
            shadow = Color(0xFF1147B8),
            glow = Color(0xFF70ACFF)
        )
    }

    BoardColorPalette.EMERALD -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF14D9CE),
            highlight = Color(0xFFB3FFF9),
            shadow = Color(0xFF07827B),
            glow = Color(0xFF5EF2E9)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFFBE1A),
            highlight = Color(0xFFFFEEB3),
            shadow = Color(0xFFB87000),
            glow = Color(0xFFFFD866)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF4252),
            highlight = Color(0xFFFFC2C7),
            shadow = Color(0xFFB01220),
            glow = Color(0xFFFF7A85)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF2AE659),
            highlight = Color(0xFFC2FFD0),
            shadow = Color(0xFF0C8F2E),
            glow = Color(0xFF66F589)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFF945CFF),
            highlight = Color(0xFFE0D1FF),
            shadow = Color(0xFF5822BD),
            glow = Color(0xFFB48CFF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF8833),
            highlight = Color(0xFFFFDFC7),
            shadow = Color(0xFFBF4900),
            glow = Color(0xFFFFAB6E)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF199BFF),
            highlight = Color(0xFFB8E2FF),
            shadow = Color(0xFF0856A8),
            glow = Color(0xFF5EBBFF)
        )
    }

    BoardColorPalette.SUNSET -> when (this) {
        BlockColor.CYAN -> BlockPalette(
            fill = Color(0xFF33CCFF),
            highlight = Color(0xFFC7F2FF),
            shadow = Color(0xFF0078B8),
            glow = Color(0xFF75DEFF)
        )
        BlockColor.AMBER -> BlockPalette(
            fill = Color(0xFFFFCC29),
            highlight = Color(0xFFFFF3C2),
            shadow = Color(0xFFC27C00),
            glow = Color(0xFFFFDF70)
        )
        BlockColor.CORAL -> BlockPalette(
            fill = Color(0xFFFF4D7D),
            highlight = Color(0xFFFFC7D6),
            shadow = Color(0xFFB81242),
            glow = Color(0xFFFF85A6)
        )
        BlockColor.EMERALD -> BlockPalette(
            fill = Color(0xFF2EE69B),
            highlight = Color(0xFFC2FFEA),
            shadow = Color(0xFF08945B),
            glow = Color(0xFF6CF5BD)
        )
        BlockColor.VIOLET -> BlockPalette(
            fill = Color(0xFFB84DFF),
            highlight = Color(0xFFECC7FF),
            shadow = Color(0xFF7014BD),
            glow = Color(0xFFD08AFF)
        )
        BlockColor.TANGERINE -> BlockPalette(
            fill = Color(0xFFFF7A29),
            highlight = Color(0xFFFFD8BF),
            shadow = Color(0xFFBF4000),
            glow = Color(0xFFFFA066)
        )
        BlockColor.COBALT -> BlockPalette(
            fill = Color(0xFF5271FF),
            highlight = Color(0xFFCCD6FF),
            shadow = Color(0xFF2038BA),
            glow = Color(0xFF859CFF)
        )
    }
}
