package com.nithra.nithraresume.utils

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.nithra.nithraresume.BuildConfig

object AdMobManager {

    // ── Ad unit IDs ───────────────────────────────────────────────────────────

    private const val BANNER_01_ID       = "ca-app-pub-2103132550188369/3770766466"
    private const val BANNER_02_ID       = "ca-app-pub-2103132550188369/6974664243"
    private const val INTERSTITIAL_01_ID = "ca-app-pub-2103132550188369/8212945487"
    private const val INTERSTITIAL_02_ID = "ca-app-pub-2103132550188369/3378942277"

    private const val TEST_BANNER_ID       = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    // ── Public accessors ──────────────────────────────────────────────────────

    val isEnabled: Boolean get() = BuildConfig.isAdMobEnable

    fun banner01Id(): String = if (BuildConfig.isTestAdMobId) TEST_BANNER_ID else BANNER_01_ID
    fun banner02Id(): String = if (BuildConfig.isTestAdMobId) TEST_BANNER_ID else BANNER_02_ID
    fun interstitial01Id(): String = if (BuildConfig.isTestAdMobId) TEST_INTERSTITIAL_ID else INTERSTITIAL_01_ID
    fun interstitial02Id(): String = if (BuildConfig.isTestAdMobId) TEST_INTERSTITIAL_ID else INTERSTITIAL_02_ID

    fun buildAdRequest(): AdRequest = AdRequest.Builder().build()

    fun adaptiveBannerSize(context: Context): AdSize {
        val widthDp = (context.resources.displayMetrics.widthPixels /
                context.resources.displayMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
    }
}

// ── Activity-level shared banner ad ──────────────────────────────────────────
// One AdView instance, loaded once, visible across all screens.

@Composable
fun SharedBannerAdView(
    viewModel: SharedAdViewModel,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFFDDDDDD)),
            contentAlignment = Alignment.Center
        ) {
            Text("Ad", color = Color.Gray)
        }
        return
    }
    if (!AdMobManager.isEnabled) return

    val context = LocalContext.current
    val adView = viewModel.getOrCreate(context)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE  -> adView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    )
}
