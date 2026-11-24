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
 * Loads and presents a rewarded ad with:
 * - Primary load against the configured AdMob unit ID (`adUnitId`), preferring real ads whenever filled.
 * - Concurrent debug standby load (`debugFallbackAdUnitId`) so debug/emulator sessions are ready
 *   within milliseconds without waiting 3-5s for NO_FILL timeouts.
 * - Background preloading of `nextAd` while an ad is showing so consecutive watches are instant.
 * - Duplicate-reward guard (`isRewardGranted`), main-looper dispatch, and fast exponential retry.
 */
class RewardedAdManager(
    private val context: Context,
    private val adUnitId: String = AdConfig.rewardedGoldUnitId,
    private val debugFallbackAdUnitId: String? = null
) {

    companion object {
        private const val TAG = "RewardedAdManager"
        private const val INITIAL_RETRY_MS = 500L
        private const val MAX_RETRY_MS = 8_000L
        private const val MAX_RETRY_ATTEMPTS = 4
        private const val DEBUG_FALLBACK_DELAY_MS = 1_200L
    }

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var primaryAd: RewardedAd? = null
    private var fallbackAd: RewardedAd? = null
    private var nextAd: RewardedAd? = null

    private var isPrimaryLoading = false
    private var isFallbackLoading = false
    private var isNextAdLoading = false
    private var isRewardGranted = false
    private var retryAttempt = 0
    private var appInForeground = true
    private var isReleased = false
    private var pendingRetryRunnable: Runnable? = null
    private var pendingDebugFallbackRunnable: Runnable? = null

    private val appContext = context.applicationContext

    private fun currentReadyAd(): RewardedAd? = primaryAd ?: fallbackAd ?: nextAd

    private fun hasAnyReadyAd(): Boolean = currentReadyAd() != null

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

    private fun cancelPendingDebugFallback() {
        pendingDebugFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingDebugFallbackRunnable = null
    }

    private fun scheduleDebugFallback() {
        if (isReleased || !BuildConfig.DEBUG) return
        val fallbackId = debugFallbackAdUnitId?.takeIf { it.isNotBlank() && it != adUnitId } ?: return
        if (fallbackAd != null || isFallbackLoading || hasAnyReadyAd()) return
        cancelPendingDebugFallback()
        val runnable = Runnable {
            pendingDebugFallbackRunnable = null
            if (!isReleased && appInForeground && !hasAnyReadyAd() && AdConfig.sdkInitialized.value) {
                loadDebugStandbyUnit(fallbackId)
            }
        }
        pendingDebugFallbackRunnable = runnable
        mainHandler.postDelayed(runnable, DEBUG_FALLBACK_DELAY_MS)
    }

    private fun scheduleRetry() {
        cancelPendingRetry()
        if (isReleased || !appInForeground || !AdConfig.sdkInitialized.value) return
        if (retryAttempt >= MAX_RETRY_ATTEMPTS) return
        retryAttempt += 1
        val delayMs = (INITIAL_RETRY_MS * (1L shl (retryAttempt - 1).coerceAtMost(4)))
            .coerceAtMost(MAX_RETRY_MS)
        val runnable = Runnable {
            pendingRetryRunnable = null
            if (!isReleased && appInForeground && !hasAnyReadyAd() && _adState.value !is AdState.Showing) {
                loadAd()
            }
        }
        pendingRetryRunnable = runnable
        mainHandler.postDelayed(runnable, delayMs)
    }

    fun setAppForeground(inForeground: Boolean) {
        runOnMain {
            appInForeground = inForeground
            if (!inForeground) {
                cancelPendingRetry()
                cancelPendingDebugFallback()
            } else if (!isReleased && AdConfig.sdkInitialized.value && !hasAnyReadyAd() && _adState.value is AdState.Error) {
                retryAttempt = 0
                loadAd()
            }
        }
    }

    fun release() {
        runOnMain {
            isReleased = true
            cancelPendingRetry()
            cancelPendingDebugFallback()
            primaryAd = null
            fallbackAd = null
            nextAd = null
        }
    }

    fun reloadIfNeeded() {
        if (isReleased) return
        if (hasAnyReadyAd()) {
            if (_adState.value !is AdState.Showing) {
                _adState.value = AdState.Ready
            }
            return
        }
        if (!AdConfig.sdkInitialized.value) return
        val current = _adState.value
        if (current is AdState.Idle || current is AdState.Error) {
            retryAttempt = 0
            loadAd()
        }
    }

    /** Called once MobileAds.initialize finishes so initial preload starts cleanly without duplicate requests. */
    fun reloadAfterSdkInit() {
        if (isReleased || !AdConfig.sdkInitialized.value) return
        if (hasAnyReadyAd()) {
            if (_adState.value !is AdState.Showing) {
                _adState.value = AdState.Ready
            }
            return
        }
        if (_adState.value is AdState.Showing || isPrimaryLoading || isFallbackLoading) return
        retryAttempt = 0
        loadAd()
    }

    fun isReady(): Boolean = hasAnyReadyAd()

    fun loadAd() {
        if (isReleased) return
        if (!AdConfig.isAppIdValid) {
            _adState.value = AdState.Error("AdMob App ID is missing or invalid")
            Log.w(TAG, "Ad load skipped: ADMOB_APP_ID is not a complete AdMob App ID")
            return
        }
        if (!AdConfig.sdkInitialized.value) {
            if (_adState.value !is AdState.Showing) {
                _adState.value = AdState.Loading
            }
            return
        }
        cancelPendingRetry()
        if (hasAnyReadyAd()) {
            if (_adState.value !is AdState.Showing) {
                _adState.value = AdState.Ready
            }
            return
        }
        if (_adState.value is AdState.Showing) {
            preloadNextAd()
            return
        }
        if (!isPrimaryLoading && !isFallbackLoading) {
            _adState.value = AdState.Loading
        }
        loadPrimaryUnit()
        scheduleDebugFallback()
    }

    private fun loadPrimaryUnit() {
        if (isReleased || primaryAd != null || isPrimaryLoading || !AdConfig.sdkInitialized.value) return
        isPrimaryLoading = true
        val adRequest = AdRequest.Builder().build()
        runOnMain {
            if (isReleased) {
                isPrimaryLoading = false
                return@runOnMain
            }
            try {
                RewardedAd.load(
                    appContext,
                    adUnitId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            isPrimaryLoading = false
                            retryAttempt = 0
                            cancelPendingDebugFallback()
                            if (isReleased) return
                            if (_adState.value is AdState.Showing) {
                                nextAd = ad
                            } else {
                                primaryAd = ad
                                _adState.value = AdState.Ready
                            }
                            Log.d(TAG, "Primary rewarded ad loaded ($adUnitId)")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isPrimaryLoading = false
                            Log.w(TAG, "Primary ad failed ($adUnitId, code=${error.code}): ${error.message}")
                            if (isReleased) return
                            if (BuildConfig.DEBUG && !debugFallbackAdUnitId.isNullOrBlank() && debugFallbackAdUnitId != adUnitId) {
                                cancelPendingDebugFallback()
                                loadDebugStandbyUnit(debugFallbackAdUnitId)
                                return
                            }
                            if (!hasAnyReadyAd() && !isFallbackLoading && _adState.value !is AdState.Showing) {
                                _adState.value = AdState.Error(error.message)
                                scheduleRetry()
                            }
                        }
                    }
                )
            } catch (t: Throwable) {
                isPrimaryLoading = false
                if (!isReleased && !hasAnyReadyAd() && !isFallbackLoading && _adState.value !is AdState.Showing) {
                    _adState.value = AdState.Error(t.message ?: "Unable to load ad")
                    scheduleRetry()
                }
            }
        }
    }

    private fun loadDebugStandbyUnit(fallbackId: String) {
        if (isReleased || fallbackAd != null || isFallbackLoading || !AdConfig.sdkInitialized.value) return
        isFallbackLoading = true
        val adRequest = AdRequest.Builder().build()
        runOnMain {
            if (isReleased) {
                isFallbackLoading = false
                return@runOnMain
            }
            try {
                RewardedAd.load(
                    appContext,
                    fallbackId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            isFallbackLoading = false
                            retryAttempt = 0
                            if (isReleased) return
                            if (_adState.value is AdState.Showing) {
                                if (nextAd == null) nextAd = ad
                            } else {
                                fallbackAd = ad
                                _adState.value = AdState.Ready
                            }
                            Log.d(TAG, "Standby rewarded ad ready ($fallbackId)")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isFallbackLoading = false
                            if (!isReleased && !hasAnyReadyAd() && !isPrimaryLoading && _adState.value !is AdState.Showing) {
                                _adState.value = AdState.Error(error.message)
                                scheduleRetry()
                            }
                        }
                    }
                )
            } catch (_: Throwable) {
                isFallbackLoading = false
            }
        }
    }

    private fun preloadNextAd() {
        if (isReleased || !AdConfig.isAppIdValid || !AdConfig.sdkInitialized.value || nextAd != null || isNextAdLoading) return
        isNextAdLoading = true
        val adRequest = AdRequest.Builder().build()
        runOnMain {
            if (isReleased) {
                isNextAdLoading = false
                return@runOnMain
            }
            try {
                RewardedAd.load(
                    appContext,
                    adUnitId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            isNextAdLoading = false
                            if (isReleased) return
                            if (_adState.value is AdState.Showing) {
                                nextAd = ad
                            } else if (primaryAd == null) {
                                primaryAd = ad
                                _adState.value = AdState.Ready
                            } else {
                                nextAd = ad
                            }
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isNextAdLoading = false
                            if (!isReleased && BuildConfig.DEBUG && !debugFallbackAdUnitId.isNullOrBlank() && debugFallbackAdUnitId != adUnitId) {
                                loadDebugStandbyUnit(debugFallbackAdUnitId)
                            }
                        }
                    }
                )
            } catch (_: Throwable) {
                isNextAdLoading = false
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
            if (isReleased || activity.isFinishing || activity.isDestroyed) {
                onFailed()
                return@runOnMain
            }
            if (_adState.value is AdState.Showing) {
                return@runOnMain
            }
            val ad = when {
                primaryAd != null -> primaryAd.also { primaryAd = null }
                fallbackAd != null -> fallbackAd.also { fallbackAd = null }
                nextAd != null -> nextAd.also { nextAd = null }
                else -> null
            }
            if (ad == null) {
                onFailed()
                reloadIfNeeded()
                return@runOnMain
            }
            cancelPendingDebugFallback()
            cancelPendingRetry()
            isRewardGranted = false
            _adState.value = AdState.Showing
            // Preload next ad while this impression is active.
            preloadNextAd()

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    runOnMain {
                        val buffered = nextAd
                        if (buffered != null) {
                            nextAd = null
                            primaryAd = buffered
                            _adState.value = AdState.Ready
                        } else if (hasAnyReadyAd()) {
                            _adState.value = AdState.Ready
                        } else if (isPrimaryLoading || isFallbackLoading || isNextAdLoading) {
                            _adState.value = AdState.Loading
                        } else {
                            _adState.value = AdState.Idle
                            loadAd()
                        }
                        onDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    runOnMain {
                        val buffered = nextAd
                        if (buffered != null) {
                            nextAd = null
                            primaryAd = buffered
                            _adState.value = AdState.Ready
                        } else if (hasAnyReadyAd()) {
                            _adState.value = AdState.Ready
                        } else {
                            _adState.value = AdState.Error(error.message)
                            loadAd()
                        }
                        onFailed()
                    }
                }
            }

            try {
                ad.show(activity) { _ ->
                    if (!isRewardGranted) {
                        isRewardGranted = true
                        runOnMain {
                            onRewarded()
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Ad show failed: ${t.message}")
                _adState.value = if (hasAnyReadyAd()) AdState.Ready else AdState.Idle
                onFailed()
                loadAd()
            }
        }
    }
}
