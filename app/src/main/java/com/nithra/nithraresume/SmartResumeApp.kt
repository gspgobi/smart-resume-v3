package com.nithra.nithraresume

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.nithra.nithraresume.service.SmartResumeMessagingService
import com.nithra.nithraresume.utils.AdMobManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartResumeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initAdMob()
        createNotificationChannels()
    }

    private fun initAdMob() {
        if (AdMobManager.isEnabled) {
            MobileAds.initialize(this)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SmartResumeMessagingService.CHANNEL_ID,
                getString(R.string.notification_channel_informational),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
// update 59
