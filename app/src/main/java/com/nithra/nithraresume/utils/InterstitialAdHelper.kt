package com.nithra.nithraresume.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class InterstitialAdHelper {

    private var ad: InterstitialAd? = null
    val isLoaded: Boolean get() = ad != null

    fun load(context: Context, adUnitId: String) {
        if (!AdMobManager.isEnabled) return
        InterstitialAd.load(
            context,
            adUnitId,
            AdMobManager.buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad = null
                }
            }
        )
    }

    fun show(activity: Activity, onDismissed: () -> Unit) {
        val loaded = ad
        if (loaded == null) {
            onDismissed()
            return
        }
        loaded.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ad = null
                onDismissed()
            }
        }
        loaded.show(activity)
    }

    suspend fun loadSuspend(context: Context, adUnitId: String): Boolean =
        suspendCancellableCoroutine { cont ->
            if (!AdMobManager.isEnabled) { if (cont.isActive) cont.resume(false); return@suspendCancellableCoroutine }
            InterstitialAd.load(
                context,
                adUnitId,
                AdMobManager.buildAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        ad = interstitialAd
                        if (cont.isActive) cont.resume(true)
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        ad = null
                        if (cont.isActive) cont.resume(false)
                    }
                }
            )
        }

    suspend fun showSuspend(activity: Activity): Unit = suspendCancellableCoroutine { cont ->
        show(activity) { if (cont.isActive) cont.resume(Unit) }
    }
}
