package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.game.BlockColor
import com.example.game.ChallengeLevelProgress
import com.example.game.GameStatistics

/**
 * Unlockable tactile block surface themes.
 * Purely visual — never alters puzzle rules or BlockColor identity.
 */
enum class BlockTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val unlockRequirement: String
) {
    CLASSIC_COBALT(
        id = "classic_cobalt",
        displayName = "Tactile Cobalt",
        subtitle = "Crisp arcade gems with warm ivory highlights",
        unlockRequirement = "Unlocked by default"
    ),
    IVORY_BRASS(
        id = "ivory_brass",
        displayName = "Ivory & Brass",
        subtitle = "Warm architectural tiles with muted mineral tones",
        unlockRequirement = "Clear 15 lines or complete 1 Challenge"
    ),
    SEAFOAM_ARCADE(
        id = "seafoam_arcade",
        displayName = "Seafoam Studio",
        subtitle = "Cool coastal pigments with high contrast bevels",
        unlockRequirement = "Reach 250 Classic Best or 3 Challenges"
    ),
    ROYAL_VELVET(
        id = "royal_velvet",
        displayName = "Royal Velvet",
        subtitle = "Rich collector edition with champagne rims",
        unlockRequirement = "Trigger a 3x Cascade or complete all 8 Challenges"
    );

    fun isUnlocked(
        stats: GameStatistics,
        challengeProgress: Map<Int, ChallengeLevelProgress>
    ): Boolean {
        val completedCount = challengeProgress.values.count { it.completed }
        return when (this) {
            CLASSIC_COBALT -> true
            IVORY_BRASS -> stats.totalLinesCleared >= 15 || completedCount >= 1
            SEAFOAM_ARCADE -> stats.classicBestScore >= 250 || completedCount >= 3
            ROYAL_VELVET -> stats.maxCascadeDepth >= 3 || completedCount >= 8
        }
    }

    fun paletteFor(color: BlockColor): BlockPalette = when (this) {
        CLASSIC_COBALT -> when (color) {
            BlockColor.CYAN -> BlockPalette(
                fill = Color(0xFF00B8E6),
                highlight = Color(0xFF9DF0FF),
                shadow = Color(0xFF0076A3),
                glow = Color(0xFF4CD6FF)
            )
            BlockColor.AMBER -> BlockPalette(
                fill = Color(0xFFF4B942),
                highlight = Color(0xFFFFEBAA),
                shadow = Color(0xFFB3760C),
                glow = Color(0xFFFFD86B)
            )
            BlockColor.CORAL -> BlockPalette(
                fill = Color(0xFFF2545B),
                highlight = Color(0xFFFFB2B6),
                shadow = Color(0xFFAE222B),
                glow = Color(0xFFFF7A80)
            )
            BlockColor.EMERALD -> BlockPalette(
                fill = Color(0xFF22C55E),
                highlight = Color(0xFFA7F7C3),
                shadow = Color(0xFF11803B),
                glow = Color(0xFF5AE88F)
            )
            BlockColor.VIOLET -> BlockPalette(
                fill = Color(0xFF8360EE),
                highlight = Color(0xFFD4C4FF),
                shadow = Color(0xFF512FB8),
                glow = Color(0xFFA68BFF)
            )
            BlockColor.TANGERINE -> BlockPalette(
                fill = Color(0xFFFF823A),
                highlight = Color(0xFFFFCCA8),
                shadow = Color(0xFFBC490B),
                glow = Color(0xFFFFA16B)
            )
            BlockColor.COBALT -> BlockPalette(
                fill = Color(0xFF3373FF),
                highlight = Color(0xFFA8C7FF),
                shadow = Color(0xFF1745B5),
                glow = Color(0xFF6698FF)
            )
        }

        IVORY_BRASS -> when (color) {
            BlockColor.CYAN -> BlockPalette(
                fill = Color(0xFF3A9BB5),
                highlight = Color(0xFFB6E8F5),
                shadow = Color(0xFF1F6378),
                glow = Color(0xFF67C4DC)
            )
            BlockColor.AMBER -> BlockPalette(
                fill = Color(0xFFE5AE3E),
                highlight = Color(0xFFFDF0C2),
                shadow = Color(0xFF9F6D0D),
                glow = Color(0xFFF5CA6B)
            )
            BlockColor.CORAL -> BlockPalette(
                fill = Color(0xFFE05A47),
                highlight = Color(0xFFFBC0B6),
                shadow = Color(0xFF9C2E20),
                glow = Color(0xFFF07F6E)
            )
            BlockColor.EMERALD -> BlockPalette(
                fill = Color(0xFF4CA87A),
                highlight = Color(0xFFB8EBD0),
                shadow = Color(0xFF286E4B),
                glow = Color(0xFF72C99C)
            )
            BlockColor.VIOLET -> BlockPalette(
                fill = Color(0xFF7968B8),
                highlight = Color(0xFFD5CDF5),
                shadow = Color(0xFF4B3D80),
                glow = Color(0xFF9D8ED8)
            )
            BlockColor.TANGERINE -> BlockPalette(
                fill = Color(0xFFEDE2CD),
                highlight = Color(0xFFFFFFFF),
                shadow = Color(0xFFB4A589),
                glow = Color(0xFFF7EFE0)
            )
            BlockColor.COBALT -> BlockPalette(
                fill = Color(0xFF3F619E),
                highlight = Color(0xFFB8CEFA),
                shadow = Color(0xFF233B69),
                glow = Color(0xFF6A8ECC)
            )
        }

        SEAFOAM_ARCADE -> when (color) {
            BlockColor.CYAN -> BlockPalette(
                fill = Color(0xFF18C9C3),
                highlight = Color(0xFFA8F7F4),
                shadow = Color(0xFF0A8581),
                glow = Color(0xFF5AE8E3)
            )
            BlockColor.AMBER -> BlockPalette(
                fill = Color(0xFFF6C244),
                highlight = Color(0xFFFFF2C2),
                shadow = Color(0xFFB5820D),
                glow = Color(0xFFFFD978)
            )
            BlockColor.CORAL -> BlockPalette(
                fill = Color(0xFFFF686B),
                highlight = Color(0xFFFFC4C6),
                shadow = Color(0xFFB83034),
                glow = Color(0xFFFF8E91)
            )
            BlockColor.EMERALD -> BlockPalette(
                fill = Color(0xFF37D399),
                highlight = Color(0xFFB5F7DF),
                shadow = Color(0xFF178B60),
                glow = Color(0xFF67E8B9)
            )
            BlockColor.VIOLET -> BlockPalette(
                fill = Color(0xFF6B84F2),
                highlight = Color(0xFFC7D3FF),
                shadow = Color(0xFF3A4FB5),
                glow = Color(0xFF93A7FF)
            )
            BlockColor.TANGERINE -> BlockPalette(
                fill = Color(0xFFFF9654),
                highlight = Color(0xFFFFD8BD),
                shadow = Color(0xFFBF5A1A),
                glow = Color(0xFFFFB280)
            )
            BlockColor.COBALT -> BlockPalette(
                fill = Color(0xFF278CE8),
                highlight = Color(0xFFA6D5FF),
                shadow = Color(0xFF11589E),
                glow = Color(0xFF5CAEFF)
            )
        }

        ROYAL_VELVET -> when (color) {
            BlockColor.CYAN -> BlockPalette(
                fill = Color(0xFF12AEE0),
                highlight = Color(0xFFB5EEFF),
                shadow = Color(0xFF086B8F),
                glow = Color(0xFF4CCFFF)
            )
            BlockColor.AMBER -> BlockPalette(
                fill = Color(0xFFF5BF4A),
                highlight = Color(0xFFFFF0C7),
                shadow = Color(0xFFB37A09),
                glow = Color(0xFFFFD678)
            )
            BlockColor.CORAL -> BlockPalette(
                fill = Color(0xFFE03E5A),
                highlight = Color(0xFFFFB8C4),
                shadow = Color(0xFF91192F),
                glow = Color(0xFFF56C83)
            )
            BlockColor.EMERALD -> BlockPalette(
                fill = Color(0xFF1EB373),
                highlight = Color(0xFFA8F2CF),
                shadow = Color(0xFF0C6E44),
                glow = Color(0xFF4CD99A)
            )
            BlockColor.VIOLET -> BlockPalette(
                fill = Color(0xFF9355E8),
                highlight = Color(0xFFE0C7FF),
                shadow = Color(0xFF5726A3),
                glow = Color(0xFFB482FF)
            )
            BlockColor.TANGERINE -> BlockPalette(
                fill = Color(0xFFF07B32),
                highlight = Color(0xFFFFD2B5),
                shadow = Color(0xFFA3430B),
                glow = Color(0xFFFF9D5E)
            )
            BlockColor.COBALT -> BlockPalette(
                fill = Color(0xFF2C66E8),
                highlight = Color(0xFFB3CAFF),
                shadow = Color(0xFF133A99),
                glow = Color(0xFF5C8FFF)
            )
        }
    }

    companion object {
        fun fromId(id: String): BlockTheme =
            entries.firstOrNull { it.id == id } ?: CLASSIC_COBALT
    }
}
