package com.example.ads

import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central AdMob configuration.
 *
 * Values are injected by the Secrets Gradle Plugin from `.env` (local, gitignored) or
 * `.env.example` (committed defaults):
 *
 * ```
 * ADMOB_APP_ID=ca-app-pub-3940256099942544~3347511713
 * ADMOB_REWARDED_GOLD_UNIT_ID=ca-app-pub-3940256099942544/5224354917
 * ```
 *
 * The "Rewarded Gold +50" placement is the free-gold card in the Shop's Coins tab.
 *
 * Debug and release builds both use the configured IDs when they are real; otherwise they
 * fall back to Google's official test ad units. Checked-in defaults are test units. Serving production ads while developing/testing can be flagged as invalid
 * traffic and put the AdMob account at risk. Production IDs must only be enabled after the
 * app is published and the AdMob account is approved.
 */
object AdConfig {

    const val REAL_APP_ID = "ca-app-pub-3956806922961659~5844639339"
    const val REAL_REWARDED_GOLD_UNIT_ID = "ca-app-pub-3956806922961659/3967743045"
    const val REAL_BANNER_GAMEPLAY_UNIT_ID = "ca-app-pub-3956806922961659/2616122603"
    const val REAL_REWARDED_CONTINUE_UNIT_ID = "ca-app-pub-3956806922961659/1692042840"
    const val REAL_REWARDED_DIAMOND_UNIT_ID = "ca-app-pub-3956806922961659/6496939128"
    const val REAL_REWARDED_WHEEL_UNIT_ID = "ca-app-pub-3956806922961659/5922224058"

    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_REWARDED_GOLD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_BANNER_GAMEPLAY_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_REWARDED_CONTINUE_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_REWARDED_DIAMOND_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_REWARDED_WHEEL_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private val APP_ID_PATTERN = Regex("^ca-app-pub-\\d{16}~\\d{10}$")
    private val AD_UNIT_PATTERN = Regex("^ca-app-pub-\\d{16}/\\d{10}$")

    private val _sdkInitialized = MutableStateFlow(false)
    val sdkInitialized: StateFlow<Boolean> = _sdkInitialized.asStateFlow()

    fun markSdkInitialized() {
        _sdkInitialized.value = true
    }

    val configuredAppId: String = BuildConfig.ADMOB_APP_ID.ifBlank { REAL_APP_ID }

    /** True when a build has no real production IDs and is serving Google test ads. */
    val isUsingTestIds: Boolean get() = configuredAppId == TEST_APP_ID

    val isAppIdValid: Boolean = APP_ID_PATTERN.matches(configuredAppId)

    private val configuredRewardedGoldUnitId: String = BuildConfig.ADMOB_REWARDED_GOLD_UNIT_ID
    private val configuredBannerGameplayUnitId: String = BuildConfig.ADMOB_BANNER_GAMEPLAY_UNIT_ID
    private val configuredRewardedContinueUnitId: String = BuildConfig.ADMOB_REWARDED_CONTINUE_UNIT_ID
    private val configuredRewardedDiamondUnitId: String = BuildConfig.ADMOB_REWARDED_DIAMOND_UNIT_ID
    private val configuredRewardedWheelUnitId: String = BuildConfig.ADMOB_REWARDED_WHEEL_UNIT_ID

    val rewardedGoldUnitId: String =
        configuredRewardedGoldUnitId
            .takeIf { AD_UNIT_PATTERN.matches(it) }
            ?: REAL_REWARDED_GOLD_UNIT_ID

    val bannerGameplayUnitId: String =
        configuredBannerGameplayUnitId
            .takeIf { AD_UNIT_PATTERN.matches(it) }
            ?: REAL_BANNER_GAMEPLAY_UNIT_ID

    val rewardedContinueUnitId: String =
        configuredRewardedContinueUnitId
            .takeIf { AD_UNIT_PATTERN.matches(it) }
            ?: REAL_REWARDED_CONTINUE_UNIT_ID

    val rewardedDiamondUnitId: String =
        configuredRewardedDiamondUnitId
            .takeIf { AD_UNIT_PATTERN.matches(it) }
            ?: REAL_REWARDED_DIAMOND_UNIT_ID

    val rewardedWheelUnitId: String =
        configuredRewardedWheelUnitId
            .takeIf { AD_UNIT_PATTERN.matches(it) }
            ?: REAL_REWARDED_WHEEL_UNIT_ID
}
