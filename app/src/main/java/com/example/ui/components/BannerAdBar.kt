package com.example.ui.components

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
import com.example.ads.AdConfig
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

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
                text = "Test Banner Ad",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = TextMuted
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    try {
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdConfig.bannerGameplayUnitId
                            loadAd(AdRequest.Builder().build())
                        }
                    } catch (_: Throwable) {
                        android.widget.TextView(ctx).apply {
                            text = "Test Ad"
                            textSize = 11f
                            gravity = android.view.Gravity.CENTER
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
