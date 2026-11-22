package com.example.ads

import com.example.BuildConfig

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
 * Policy note: the checked-in defaults and debug builds always use Google's official test
 * ad units. Serving production ads while developing/testing can be flagged as invalid
 * traffic and put the AdMob account at risk. Production IDs must only be enabled after the
 * app is published and the AdMob account is approved.
 */
object AdConfig {

    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_REWARDED_GOLD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private val APP_ID_PATTERN = Regex("^ca-app-pub-\\d{16}~\\d{10}$")
    private val AD_UNIT_PATTERN = Regex("^ca-app-pub-\\d{16}/\\d{10}$")

    val configuredAppId: String = BuildConfig.ADMOB_APP_ID

    val isAppIdValid: Boolean = APP_ID_PATTERN.matches(configuredAppId)

    private val configuredRewardedGoldUnitId: String = BuildConfig.ADMOB_REWARDED_GOLD_UNIT_ID

    val rewardedGoldUnitId: String = if (BuildConfig.DEBUG) {
        TEST_REWARDED_GOLD_UNIT_ID
    } else {
        configuredRewardedGoldUnitId
            .takeIf { AD_UNIT_PATTERN.matches(it) }
            ?: TEST_REWARDED_GOLD_UNIT_ID
    }
}
