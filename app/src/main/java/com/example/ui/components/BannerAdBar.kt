package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.ads.AdConfig
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private tailrec fun Context.findBannerActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findBannerActivity()
    else -> null
}

@Composable
fun BannerAdBar(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val sdkReady by AdConfig.sdkInitialized.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(10.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, GlassBorder.copy(alpha = 0.65f), shape)
            .testTag("gameplay_banner_ad")
    ) {
        if (isPreview || !sdkReady || !AdConfig.isAppIdValid) {
            Text(
                text = if (isPreview) "Banner Ad" else "BLOCK FALL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = TextMuted
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    val container = FrameLayout(ctx)
                    container.tag = BannerHolder()
                    container
                },
                update = { container ->
                    val holder = container.tag as? BannerHolder ?: return@AndroidView
                    if (holder.released || holder.loaded) return@AndroidView
                    holder.loaded = true
                    attachBannerSafely(
                        container = container,
                        holder = holder,
                        unitId = AdConfig.bannerGameplayUnitId,
                        allowDebugFallback = false
                    )
                },
                onRelease = { container ->
                    val holder = container.tag as? BannerHolder
                    holder?.released = true
                    holder?.destroyActiveAdView()
                    container.removeAllViews()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private class BannerHolder {
    var released: Boolean = false
    var loaded: Boolean = false
    var activeAdView: AdView? = null

    fun destroyActiveAdView() {
        runCatching {
            activeAdView?.adListener = object : AdListener() {}
            activeAdView?.destroy()
        }
        activeAdView = null
    }
}

private fun attachBannerSafely(
    container: FrameLayout,
    holder: BannerHolder,
    unitId: String,
    allowDebugFallback: Boolean
) {
    if (holder.released) return
    val ctx = container.context
    val activity = ctx.findBannerActivity()
    if (activity != null && (activity.isFinishing || activity.isDestroyed)) return

    holder.destroyActiveAdView()
    container.removeAllViews()

    try {
        val banner = AdView(ctx).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = unitId
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (
                        !holder.released &&
                        container.isAttachedToWindow &&
                        allowDebugFallback &&
                        BuildConfig.DEBUG &&
                        unitId != AdConfig.TEST_BANNER_GAMEPLAY_UNIT_ID
                    ) {
                        attachBannerSafely(
                            container = container,
                            holder = holder,
                            unitId = AdConfig.TEST_BANNER_GAMEPLAY_UNIT_ID,
                            allowDebugFallback = false
                        )
                    }
                }
            }
            loadAd(AdRequest.Builder().build())
        }
        holder.activeAdView = banner
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        container.addView(banner, lp)
    } catch (_: Throwable) {
        container.removeAllViews()
        container.addView(
            android.widget.TextView(ctx).apply {
                text = "Block Fall"
                textSize = 11f
                gravity = Gravity.CENTER
            }
        )
    }
}
