package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AdState {
    data object Idle : AdState()
    data object Loading : AdState()
    data object Ready : AdState()
    data object Showing : AdState()
    data class Error(val message: String) : AdState()
}

/**
 * Loads and presents a single rewarded ad, exposing load state so the UI can enable or
 * disable the "watch ad" action. The reward callback is guarded against duplicates because
 * AdMob may deliver the reward callback more than once for a single impression.
 *
 * The unit ID comes from [AdConfig]: debug builds always use Google's official test unit,
 * while release builds use the production "Rewarded Gold +50" unit.
 */
class RewardedAdManager(private val context: Context) {

    companion object {
        private const val TAG = "RewardedAdManager"
    }

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private var rewardedAd: RewardedAd? = null
    private var isRewardGranted = false

    fun loadAd() {
        if (!AdConfig.isAppIdValid) {
            _adState.value = AdState.Error("AdMob App ID is missing or invalid")
            Log.w(TAG, "Ad load skipped: ADMOB_APP_ID is not a complete AdMob App ID")
            return
        }
        if (_adState.value is AdState.Loading || _adState.value is AdState.Ready) return
        _adState.value = AdState.Loading
        val adRequest = AdRequest.Builder().build()
        try {
            RewardedAd.load(
                context,
                AdConfig.rewardedGoldUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        _adState.value = AdState.Ready
                        Log.d(TAG, "Ad loaded")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        _adState.value = AdState.Error(error.message)
                        Log.w(TAG, "Ad failed to load: ${error.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            rewardedAd = null
            _adState.value = AdState.Error(t.message ?: "Unable to load ad")
            Log.w(TAG, "Ad load failed synchronously: ${t.message}")
        }
    }

    fun showAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onFailed()
            loadAd()
            return
        }
        isRewardGranted = false
        _adState.value = AdState.Showing

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _adState.value = AdState.Idle
                onDismissed()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _adState.value = AdState.Error(error.message)
                onFailed()
                loadAd()
            }
        }

        ad.show(activity) { _ ->
            if (!isRewardGranted) {
                isRewardGranted = true
                onRewarded()
            }
        }
    }
}
