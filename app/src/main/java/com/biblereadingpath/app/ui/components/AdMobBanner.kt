package com.biblereadingpath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.biblereadingpath.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * AdMob banner ad composable
 *
 * Displays a banner ad at the bottom of screens
 */
@Composable
fun AdMobBanner(
    adUnitId: String = BuildConfig.ADMOB_BANNER_AD_UNIT_ID,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

/**
 * Test AdMob banner for development
 * Uses test ad unit ID
 */
@Composable
fun TestAdMobBanner(
    modifier: Modifier = Modifier
) {
    AdMobBanner(modifier = modifier)
}
