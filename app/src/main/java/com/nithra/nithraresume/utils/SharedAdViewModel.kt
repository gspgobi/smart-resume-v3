package com.nithra.nithraresume.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedAdViewModel @Inject constructor() : ViewModel() {

    private var adView: AdView? = null

    private val _adHeightDp = MutableStateFlow(0)
    val adHeightDp: StateFlow<Int> = _adHeightDp

    fun getOrCreate(context: Context): AdView {
        return adView ?: AdView(context.applicationContext).apply {
            val size = AdMobManager.adaptiveBannerSize(context)
            setAdSize(size)
            adUnitId = AdMobManager.banner01Id()
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    _adHeightDp.value = adSize?.height ?: 0
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    viewModelScope.launch {
                        delay(AD_RETRY_DELAY_MS)
                        loadAd(AdMobManager.buildAdRequest())
                    }
                }
            }
            loadAd(AdMobManager.buildAdRequest())
        }.also { adView = it }
    }

    override fun onCleared() {
        adView?.destroy()
        adView = null
        super.onCleared()
    }
}
