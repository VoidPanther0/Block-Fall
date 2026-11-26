package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.AchievementCategory
import com.example.game.BlockColor
import com.example.game.BoosterType
import com.example.game.ProfileAvatar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Crisp 3D PNG asset icon helper backed by pre-bundled Gemini-generated PNG drawables.
 */
@Composable
fun GameAssetIcon(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        contentScale = contentScale,
        alpha = alpha,
        modifier = modifier.size(size)
    )
}

enum class ChestTier(
    @DrawableRes val closedRes: Int,
    @DrawableRes val openRes: Int,
    val label: String,
    val accent: Color
) {
    BRONZE(R.drawable.img_chest_bronze_closed, R.drawable.img_chest_bronze_open, "Bronze Chest", Color(0xFFD97706)),
    SILVER(R.drawable.img_chest_silver_closed, R.drawable.img_chest_silver_open, "Silver Chest", Color(0xFF93C5FD)),
    EMERALD(R.drawable.img_chest_emerald_closed, R.drawable.img_chest_emerald_open, "Emerald Chest", Color(0xFF10B981)),
    ROYAL(R.drawable.img_chest_gold_closed, R.drawable.img_chest_open_f6, "Royal Chest", AccentGold),
    CRYSTAL(R.drawable.img_chest_crystal_closed, R.drawable.img_chest_crystal_open, "Grand Crystal Chest", AccentCyan)
}

data class ShowcaseAssetEntry(
    val title: String,
    val category: String,
    @DrawableRes val resId: Int
)

/**
 * Central mapping & catalog for all 129 Gemini-generated PNG drawables across the app.
 */
object GameAssetCatalog {

    val chestOpenFrames: List<Int> = listOf(
        R.drawable.img_chest_open_f1,
        R.drawable.img_chest_open_f2,
        R.drawable.img_chest_open_f3,
        R.drawable.img_chest_open_f4,
        R.drawable.img_chest_open_f5,
        R.drawable.img_chest_open_f6
    )

    @DrawableRes
    fun avatarRes(avatar: ProfileAvatar): Int = when (avatar) {
        ProfileAvatar.CLASSIC_BLOCK -> R.drawable.img_avatar_block
        ProfileAvatar.COBALT_SHIELD -> R.drawable.img_avatar_knight
        ProfileAvatar.EMERALD_GEM -> R.drawable.img_avatar_emerald
        ProfileAvatar.AMBER_CROWN -> R.drawable.img_avatar_lion
        ProfileAvatar.CORAL_SPARK -> R.drawable.img_avatar_phoenix
        ProfileAvatar.VIOLET_ORB -> R.drawable.img_avatar_owl
        ProfileAvatar.GOLD_ROBOT -> R.drawable.img_avatar_robot
        ProfileAvatar.GOOGLE_PHOTO -> R.drawable.img_avatar_star
    }

    @DrawableRes
    fun rankCrestRes(tierNumber: Int): Int = when (tierNumber.coerceIn(1, 8)) {
        1 -> R.drawable.img_rank_1_iron
        2 -> R.drawable.img_rank_2_bronze
        3 -> R.drawable.img_rank_3_silver
        4 -> R.drawable.img_rank_4_gold
        5 -> R.drawable.img_rank_5_emerald
        6 -> R.drawable.img_rank_6_diamond
        7 -> R.drawable.img_rank_7_amethyst
        else -> R.drawable.img_rank_8_cosmic
    }

    @DrawableRes
    fun boosterRes(type: BoosterType): Int = when (type) {
        BoosterType.HAMMER -> R.drawable.img_booster_hammer
        BoosterType.REFRESH -> R.drawable.img_booster_refresh
        BoosterType.SHUFFLE -> R.drawable.img_booster_shuffle
    }

    @DrawableRes
    fun jewelBlockRes(color: BlockColor): Int = when (color) {
        BlockColor.CORAL -> R.drawable.img_block_coral
        BlockColor.AMBER, BlockColor.TANGERINE -> R.drawable.img_block_amber
        BlockColor.CYAN -> R.drawable.img_block_cyan
        BlockColor.EMERALD -> R.drawable.img_block_emerald
        BlockColor.VIOLET -> R.drawable.img_block_violet
        BlockColor.COBALT -> R.drawable.img_block_cobalt
    }

    @DrawableRes
    fun achievementMedalRes(category: AchievementCategory, tier: Int): Int = when {
        category == AchievementCategory.CASCADES -> if (tier >= 3) R.drawable.img_badge_supernova else R.drawable.img_medal_wave
        category == AchievementCategory.SCORE -> if (tier >= 3) R.drawable.img_medal_trophy_gold else R.drawable.img_medal_trophy_silver
        category == AchievementCategory.CAMPAIGN -> if (tier >= 3) R.drawable.img_medal_crown_orb else R.drawable.img_medal_scroll
        tier >= 3 -> R.drawable.img_badge_winged_star
        tier == 2 -> R.drawable.img_medal_lightning
        else -> R.drawable.img_medal_ribbon_star
    }

    @DrawableRes
    fun dailyRewardRes(day: Int): Int = when (day) {
        1 -> R.drawable.img_coin_stack
        2 -> R.drawable.img_gem_single
        3 -> R.drawable.img_coin_pouch
        4 -> R.drawable.img_booster_shuffle
        5 -> R.drawable.img_gem_cluster
        6 -> R.drawable.img_coin_pile
        else -> R.drawable.img_chest_crystal_closed
    }

    @DrawableRes
    fun shopItemRes(itemId: String, boosterType: BoosterType?): Int = when {
        boosterType != null -> boosterRes(boosterType)
        itemId.contains("bundle") -> R.drawable.img_gift_winged
        itemId.contains("gold_small") -> R.drawable.img_coin_stack
        itemId.contains("gold_medium") -> R.drawable.img_coin_pouch
        itemId.contains("gold_large") -> R.drawable.img_vault_gold
        itemId.contains("crown") -> R.drawable.img_crown_gold
        itemId.contains("emerald") -> R.drawable.img_crest_emerald
        itemId.contains("amethyst") -> R.drawable.img_crown_amethyst
        itemId.contains("gem") || itemId.contains("diamond") -> R.drawable.img_gem_pouch
        else -> R.drawable.img_gift_box_gold
    }

    val allShowcaseAssets: List<ShowcaseAssetEntry> by lazy {
        listOf(
            // Chests & Animation (15)
            ShowcaseAssetEntry("Chest Closed", "Chests", R.drawable.img_chest_open_f1),
            ShowcaseAssetEntry("Chest Glow", "Chests", R.drawable.img_chest_open_f2),
            ShowcaseAssetEntry("Chest Crack", "Chests", R.drawable.img_chest_open_f3),
            ShowcaseAssetEntry("Chest Reveal", "Chests", R.drawable.img_chest_open_f4),
            ShowcaseAssetEntry("Chest Burst", "Chests", R.drawable.img_chest_open_f5),
            ShowcaseAssetEntry("Chest Open", "Chests", R.drawable.img_chest_open_f6),
            ShowcaseAssetEntry("Bronze Closed", "Chests", R.drawable.img_chest_bronze_closed),
            ShowcaseAssetEntry("Bronze Open", "Chests", R.drawable.img_chest_bronze_open),
            ShowcaseAssetEntry("Silver Closed", "Chests", R.drawable.img_chest_silver_closed),
            ShowcaseAssetEntry("Silver Open", "Chests", R.drawable.img_chest_silver_open),
            ShowcaseAssetEntry("Emerald Closed", "Chests", R.drawable.img_chest_emerald_closed),
            ShowcaseAssetEntry("Emerald Open", "Chests", R.drawable.img_chest_emerald_open),
            ShowcaseAssetEntry("Crystal Closed", "Chests", R.drawable.img_chest_crystal_closed),
            ShowcaseAssetEntry("Crystal Open", "Chests", R.drawable.img_chest_crystal_open),
            ShowcaseAssetEntry("Royal Gift", "Chests", R.drawable.img_gift_box_royal),
            ShowcaseAssetEntry("Gold Chest", "Chests", R.drawable.img_chest_gold_closed),

            // Economy & Rewards (16)
            ShowcaseAssetEntry("Gold Coin", "Economy", R.drawable.img_coin_single),
            ShowcaseAssetEntry("Coin Stack", "Economy", R.drawable.img_coin_stack),
            ShowcaseAssetEntry("Coin Pouch", "Economy", R.drawable.img_coin_pouch),
            ShowcaseAssetEntry("Coin Pile", "Economy", R.drawable.img_coin_pile),
            ShowcaseAssetEntry("Diamond", "Economy", R.drawable.img_gem_single),
            ShowcaseAssetEntry("Gem Trio", "Economy", R.drawable.img_gem_cluster),
            ShowcaseAssetEntry("Prism Shards", "Economy", R.drawable.img_crystal_shards),
            ShowcaseAssetEntry("Gem Pouch", "Economy", R.drawable.img_gem_pouch),
            ShowcaseAssetEntry("XP Star", "Economy", R.drawable.img_xp_star),
            ShowcaseAssetEntry("Energy Orb", "Economy", R.drawable.img_energy_orb),
            ShowcaseAssetEntry("VIP Ticket", "Economy", R.drawable.img_vip_ticket),
            ShowcaseAssetEntry("Spin Token", "Economy", R.drawable.img_spin_token),
            ShowcaseAssetEntry("Gold Crown", "Economy", R.drawable.img_crown_gold),
            ShowcaseAssetEntry("Treasure Key", "Economy", R.drawable.img_key_gold),
            ShowcaseAssetEntry("Lucky Clover", "Economy", R.drawable.img_clover_emerald),
            ShowcaseAssetEntry("Gold Gift", "Economy", R.drawable.img_gift_box_gold),

            // Boosters & FX (16)
            ShowcaseAssetEntry("Hammer", "Boosters", R.drawable.img_booster_hammer),
            ShowcaseAssetEntry("Refresh", "Boosters", R.drawable.img_booster_refresh),
            ShowcaseAssetEntry("Shuffle", "Boosters", R.drawable.img_booster_shuffle),
            ShowcaseAssetEntry("Magnet", "Boosters", R.drawable.img_booster_magnet),
            ShowcaseAssetEntry("Crystal Bomb", "Boosters", R.drawable.img_booster_bomb),
            ShowcaseAssetEntry("Nova Bomb", "Boosters", R.drawable.img_booster_rainbow_bomb),
            ShowcaseAssetEntry("Combo Flame", "FX", R.drawable.img_fx_combo_flame),
            ShowcaseAssetEntry("Wing Shield", "FX", R.drawable.img_fx_shield_wing),
            ShowcaseAssetEntry("Padlock", "Status", R.drawable.img_status_padlock),
            ShowcaseAssetEntry("Verified", "Status", R.drawable.img_status_check_shield),
            ShowcaseAssetEntry("Lightning", "FX", R.drawable.img_fx_lightning),
            ShowcaseAssetEntry("Hourglass", "FX", R.drawable.img_fx_hourglass),
            ShowcaseAssetEntry("Sparkles", "FX", R.drawable.img_fx_sparkles),
            ShowcaseAssetEntry("Target", "Missions", R.drawable.img_icon_target),
            ShowcaseAssetEntry("Star Wand", "Boosters", R.drawable.img_booster_wand),
            ShowcaseAssetEntry("Mystery Orb", "FX", R.drawable.img_fx_mystery_orb),

            // Ranks & Medals (16)
            ShowcaseAssetEntry("Rank I", "Ranks", R.drawable.img_rank_1_iron),
            ShowcaseAssetEntry("Rank II", "Ranks", R.drawable.img_rank_2_bronze),
            ShowcaseAssetEntry("Rank III", "Ranks", R.drawable.img_rank_3_silver),
            ShowcaseAssetEntry("Rank IV", "Ranks", R.drawable.img_rank_4_gold),
            ShowcaseAssetEntry("Rank V", "Ranks", R.drawable.img_rank_5_emerald),
            ShowcaseAssetEntry("Rank VI", "Ranks", R.drawable.img_rank_6_diamond),
            ShowcaseAssetEntry("Rank VII", "Ranks", R.drawable.img_rank_7_amethyst),
            ShowcaseAssetEntry("Rank VIII", "Ranks", R.drawable.img_rank_8_cosmic),
            ShowcaseAssetEntry("Gold Cup", "Medals", R.drawable.img_medal_trophy_gold),
            ShowcaseAssetEntry("Silver Cup", "Medals", R.drawable.img_medal_trophy_silver),
            ShowcaseAssetEntry("Star Medal", "Medals", R.drawable.img_medal_ribbon_star),
            ShowcaseAssetEntry("Wave Medal", "Medals", R.drawable.img_medal_wave),
            ShowcaseAssetEntry("Flame Cup", "Medals", R.drawable.img_medal_flame),
            ShowcaseAssetEntry("Scroll Medal", "Medals", R.drawable.img_medal_scroll),
            ShowcaseAssetEntry("Bolt Medal", "Medals", R.drawable.img_medal_lightning),
            ShowcaseAssetEntry("Crown Orb", "Medals", R.drawable.img_medal_crown_orb),

            // Avatars & Modes (16)
            ShowcaseAssetEntry("Block Mascot", "Avatars", R.drawable.img_avatar_block),
            ShowcaseAssetEntry("Knight", "Avatars", R.drawable.img_avatar_knight),
            ShowcaseAssetEntry("Emerald", "Avatars", R.drawable.img_avatar_emerald),
            ShowcaseAssetEntry("Royal Lion", "Avatars", R.drawable.img_avatar_lion),
            ShowcaseAssetEntry("Phoenix", "Avatars", R.drawable.img_avatar_phoenix),
            ShowcaseAssetEntry("Mystic Owl", "Avatars", R.drawable.img_avatar_owl),
            ShowcaseAssetEntry("Star", "Avatars", R.drawable.img_avatar_star),
            ShowcaseAssetEntry("Mecha Bot", "Avatars", R.drawable.img_avatar_robot),
            ShowcaseAssetEntry("Classic Mode", "Modes", R.drawable.img_mode_classic),
            ShowcaseAssetEntry("Challenge", "Modes", R.drawable.img_mode_challenge),
            ShowcaseAssetEntry("Daily Mode", "Modes", R.drawable.img_mode_daily),
            ShowcaseAssetEntry("Blitz Mode", "Modes", R.drawable.img_mode_blitz),
            ShowcaseAssetEntry("Podium", "Nav", R.drawable.img_nav_podium),
            ShowcaseAssetEntry("Shop Stall", "Nav", R.drawable.img_nav_shop),
            ShowcaseAssetEntry("Gear", "Nav", R.drawable.img_nav_settings),
            ShowcaseAssetEntry("Music", "Nav", R.drawable.img_nav_music),

            // Jewel Blocks & Badges (16)
            ShowcaseAssetEntry("Ruby Cube", "Blocks", R.drawable.img_block_coral),
            ShowcaseAssetEntry("Amber Cube", "Blocks", R.drawable.img_block_amber),
            ShowcaseAssetEntry("Cyan Cube", "Blocks", R.drawable.img_block_cyan),
            ShowcaseAssetEntry("Jade Cube", "Blocks", R.drawable.img_block_emerald),
            ShowcaseAssetEntry("Violet Cube", "Blocks", R.drawable.img_block_violet),
            ShowcaseAssetEntry("Cobalt Cube", "Blocks", R.drawable.img_block_cobalt),
            ShowcaseAssetEntry("Rose Cube", "Blocks", R.drawable.img_block_rose),
            ShowcaseAssetEntry("Obsidian Cube", "Blocks", R.drawable.img_block_obsidian),
            ShowcaseAssetEntry("Laurel", "Badges", R.drawable.img_badge_laurel),
            ShowcaseAssetEntry("Winged Star", "Badges", R.drawable.img_badge_winged_star),
            ShowcaseAssetEntry("Streak Flame", "Badges", R.drawable.img_badge_fire_streak),
            ShowcaseAssetEntry("Supernova", "Badges", R.drawable.img_badge_supernova),
            ShowcaseAssetEntry("Ribbon", "Badges", R.drawable.img_badge_ribbon),
            ShowcaseAssetEntry("Halo Crown", "Badges", R.drawable.img_badge_halo_crown),
            ShowcaseAssetEntry("Cracked Gem", "Badges", R.drawable.img_badge_cracked_crystal),
            ShowcaseAssetEntry("Treasure Map", "Badges", R.drawable.img_badge_treasure_map),

            // UI Ornaments & Extra Icons (32)
            ShowcaseAssetEntry("Home", "UI", R.drawable.img_ui_home),
            ShowcaseAssetEntry("Puzzle Grid", "UI", R.drawable.img_ui_puzzle_grid),
            ShowcaseAssetEntry("Sound On", "UI", R.drawable.img_ui_sound_on),
            ShowcaseAssetEntry("Palette", "UI", R.drawable.img_ui_palette),
            ShowcaseAssetEntry("Stats Chart", "UI", R.drawable.img_ui_stats_chart),
            ShowcaseAssetEntry("Megaphone", "UI", R.drawable.img_ui_megaphone),
            ShowcaseAssetEntry("Coin Burst", "FX", R.drawable.img_fx_coin_burst),
            ShowcaseAssetEntry("Gem Burst", "FX", R.drawable.img_fx_gem_burst),
            ShowcaseAssetEntry("Magic Portal", "FX", R.drawable.img_fx_portal_ring),
            ShowcaseAssetEntry("Bell", "UI", R.drawable.img_ui_bell),
            ShowcaseAssetEntry("Swords", "UI", R.drawable.img_ui_crossed_swords),
            ShowcaseAssetEntry("Throne", "UI", R.drawable.img_ui_throne),
            ShowcaseAssetEntry("Edit", "UI", R.drawable.img_ui_pencil),
            ShowcaseAssetEntry("Replay", "UI", R.drawable.img_ui_replay),
            ShowcaseAssetEntry("Shield Star", "UI", R.drawable.img_ui_shield_star),
            ShowcaseAssetEntry("Confetti", "FX", R.drawable.img_fx_confetti),
            ShowcaseAssetEntry("Lucky Wheel", "Extra", R.drawable.img_wheel_emblem),
            ShowcaseAssetEntry("Gold Vault", "Extra", R.drawable.img_vault_gold),
            ShowcaseAssetEntry("Gem Pedestal", "Extra", R.drawable.img_pedestal_diamond),
            ShowcaseAssetEntry("Reward Video", "Extra", R.drawable.img_ad_clapper),
            ShowcaseAssetEntry("Gravity Orb", "Extra", R.drawable.img_planet_gravity),
            ShowcaseAssetEntry("Play Gem", "Extra", R.drawable.img_ui_play_gem),
            ShowcaseAssetEntry("Muted", "Extra", R.drawable.img_ui_sound_off),
            ShowcaseAssetEntry("Daily Dawn", "Extra", R.drawable.img_daily_sunrise),
            ShowcaseAssetEntry("Emerald Crest", "Extra", R.drawable.img_crest_emerald),
            ShowcaseAssetEntry("Prism Crown", "Extra", R.drawable.img_crown_amethyst),
            ShowcaseAssetEntry("Star Trio", "Extra", R.drawable.img_stars_trio),
            ShowcaseAssetEntry("Winged Gift", "Extra", R.drawable.img_gift_winged),
            ShowcaseAssetEntry("Heart Gem", "Extra", R.drawable.img_heart_gem),
            ShowcaseAssetEntry("Compass", "Extra", R.drawable.img_ui_compass),
            ShowcaseAssetEntry("Trophy Stand", "Extra", R.drawable.img_trophy_pedestal),
            ShowcaseAssetEntry("Home Citadel", "Banners", R.drawable.img_banner_home_hero),
            ShowcaseAssetEntry("Festival Sky", "Banners", R.drawable.img_banner_festival_events)
        )
    }
}

@Composable
fun AssetVaultSheet(
    onDismiss: () -> Unit
) {
    PremiumModalSheet(onDismissRequest = onDismiss) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
                .testTag("asset_vault_sheet")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameAssetIcon(
                    resId = R.drawable.img_chest_crystal_open,
                    contentDescription = null,
                    size = 38.dp
                )
                Column {
                    Text(
                        text = "3D Treasure & Asset Vault",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${GameAssetCatalog.allShowcaseAssets.size} custom 3D PNG assets & animations",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentCyan
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 76.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(GameAssetCatalog.allShowcaseAssets, key = { "${it.category}_${it.title}_${it.resId}" }) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BoardWellBg)
                            .border(1.dp, GlassBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(6.dp)
                    ) {
                        GameAssetIcon(
                            resId = item.resId,
                            contentDescription = item.title,
                            size = 44.dp
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}
