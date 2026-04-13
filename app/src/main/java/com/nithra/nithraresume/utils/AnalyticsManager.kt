package com.nithra.nithraresume.utils

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Sets the Android ID as both the Analytics user ID and a user property.
     * Called once on first launch from SplashViewModel.
     */
    fun setUserId(androidId: String) {
        if (androidId.isEmpty()) return
        analytics.setUserId(androidId)
        analytics.setUserProperty("android_id", androidId)
    }
}
