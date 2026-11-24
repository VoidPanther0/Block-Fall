package com.example.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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

@Composable
fun BannerAdBar(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val shape = RoundedCornerShape(10.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, GlassBorder, shape)
            .testTag("gameplay_banner_ad")
    ) {
        if (isPreview) {
            Text(
                text = "Banner Ad",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = TextMuted
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    val container = FrameLayout(ctx)
                    fun attachBanner(unitId: String, allowDebugFallback: Boolean) {
                        container.removeAllViews()
                        val banner = AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = unitId
                            adListener = object : AdListener() {
                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    if (allowDebugFallback && BuildConfig.DEBUG && unitId != AdConfig.TEST_BANNER_GAMEPLAY_UNIT_ID) {
                                        attachBanner(AdConfig.TEST_BANNER_GAMEPLAY_UNIT_ID, allowDebugFallback = false)
                                    }
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                        val lp = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                        )
                        container.addView(banner, lp)
                    }
                    try {
                        attachBanner(AdConfig.bannerGameplayUnitId, allowDebugFallback = true)
                    } catch (_: Throwable) {
                        container.addView(
                            android.widget.TextView(ctx).apply {
                                text = "Block Fall"
                                textSize = 11f
                                gravity = Gravity.CENTER
                            }
                        )
                    }
                    container
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
