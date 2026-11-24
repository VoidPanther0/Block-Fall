package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.BuildConfig
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
 * disable the "watch ad" action.
 *
 * Includes:
 * - Duplicate-reward guard (`isRewardGranted`) per impression.
 * - Main-looper dispatch for load/show and reward callbacks.
 * - Automatic exponential backoff retry on load failures so buttons never freeze in Error.
 * - Debug-only fallback to Google's official test unit when a real unit returns NO_FILL in debug.
 */
class RewardedAdManager(
    private val context: Context,
    private val adUnitId: String = AdConfig.rewardedGoldUnitId,
    private val debugFallbackAdUnitId: String? = null
) {

    companion object {
        private const val TAG = "RewardedAdManager"
        private const val INITIAL_RETRY_MS = 2_000L
        private const val MAX_RETRY_MS = 30_000L
    }

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeAdUnitId: String = adUnitId
    private var rewardedAd: RewardedAd? = null
    private var isRewardGranted = false
    private var retryAttempt = 0
    private var pendingRetryRunnable: Runnable? = null

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private fun cancelPendingRetry() {
        pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingRetryRunnable = null
    }

    private fun scheduleRetry() {
        cancelPendingRetry()
        retryAttempt += 1
        val delayMs = (INITIAL_RETRY_MS * (1L shl (retryAttempt - 1).coerceAtMost(4)))
            .coerceAtMost(MAX_RETRY_MS)
        val runnable = Runnable {
            pendingRetryRunnable = null
            if (_adState.value is AdState.Error || _adState.value is AdState.Idle) {
                loadAd()
            }
        }
        pendingRetryRunnable = runnable
        mainHandler.postDelayed(runnable, delayMs)
    }

    fun reloadIfNeeded() {
        val current = _adState.value
        if (current is AdState.Idle || current is AdState.Error) {
            loadAd()
        }
    }

    fun loadAd() {
        if (!AdConfig.isAppIdValid) {
            _adState.value = AdState.Error("AdMob App ID is missing or invalid")
            Log.w(TAG, "Ad load skipped: ADMOB_APP_ID is not a complete AdMob App ID")
            return
        }
        cancelPendingRetry()
        if (_adState.value is AdState.Loading || _adState.value is AdState.Ready || _adState.value is AdState.Showing) {
            return
        }
        _adState.value = AdState.Loading
        val adRequest = AdRequest.Builder().build()
        runOnMain {
            try {
                RewardedAd.load(
                    context,
                    activeAdUnitId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            retryAttempt = 0
                            rewardedAd = ad
                            _adState.value = AdState.Ready
                            Log.d(TAG, "Ad loaded ($activeAdUnitId)")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            rewardedAd = null
                            Log.w(TAG, "Ad failed to load ($activeAdUnitId, code=${error.code}): ${error.message}")
                            val shouldFallbackDebug = BuildConfig.DEBUG &&
                                debugFallbackAdUnitId != null &&
                                activeAdUnitId != debugFallbackAdUnitId &&
                                (error.code == AdRequest.ERROR_CODE_NO_FILL ||
                                    error.code == AdRequest.ERROR_CODE_INVALID_REQUEST ||
                                    error.code == AdRequest.ERROR_CODE_INTERNAL_ERROR)
                            if (shouldFallbackDebug) {
                                Log.i(TAG, "Debug fallback: switching to Google test rewarded ad unit")
                                activeAdUnitId = debugFallbackAdUnitId!!
                                retryAttempt = 0
                                _adState.value = AdState.Idle
                                loadAd()
                            } else {
                                _adState.value = AdState.Error(error.message)
                                scheduleRetry()
                            }
                        }
                    }
                )
            } catch (t: Throwable) {
                rewardedAd = null
                _adState.value = AdState.Error(t.message ?: "Unable to load ad")
                Log.w(TAG, "Ad load failed synchronously: ${t.message}")
                scheduleRetry()
            }
        }
    }

    fun showAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: () -> Unit
    ) {
        runOnMain {
            val ad = rewardedAd
            if (ad == null) {
                onFailed()
                reloadIfNeeded()
                return@runOnMain
            }
            isRewardGranted = false
            _adState.value = AdState.Showing

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    runOnMain {
                        rewardedAd = null
                        _adState.value = AdState.Idle
                        onDismissed()
                        loadAd()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    runOnMain {
                        rewardedAd = null
                        _adState.value = AdState.Error(error.message)
                        onFailed()
                        loadAd()
                    }
                }
            }

            ad.show(activity) { _ ->
                if (!isRewardGranted) {
                    isRewardGranted = true
                    runOnMain {
                        onRewarded()
                    }
                }
            }
        }
    }
}
