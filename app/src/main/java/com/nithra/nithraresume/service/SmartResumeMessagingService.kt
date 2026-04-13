package com.nithra.nithraresume.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nithra.nithraresume.MainActivity
import com.nithra.nithraresume.R
import com.nithra.nithraresume.data.api.ApiRepository
import com.nithra.nithraresume.data.model.FcmData
import com.nithra.nithraresume.data.repository.FcmRepository
import com.nithra.nithraresume.utils.FcmKey
import com.nithra.nithraresume.utils.FcmMsgType
import com.nithra.nithraresume.utils.FcmNotiType
import com.nithra.nithraresume.utils.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class SmartResumeMessagingService : FirebaseMessagingService() {

    @Inject lateinit var fcmRepository: FcmRepository
    @Inject lateinit var prefsManager: PrefsManager
    @Inject lateinit var apiRepository: ApiRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val EXTRA_FCM_DATA_ID = "extra_fcm_data_id"
        const val CHANNEL_ID = "notification.channel.informational"
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        serviceScope.launch {
            prefsManager.setFcmTokenSentToServer(false)
            apiRepository.registerFcmToken(token, firstOrUpdate = "first")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        if (data.isEmpty()) return

        val messageType     = data[FcmKey.MSGTYPE].orEmpty()
        val notificationType = data[FcmKey.NOTITYPE].orEmpty()
        val title           = data[FcmKey.TITLE].orEmpty()
        val message         = data[FcmKey.MESSAGE].orEmpty()
        val imageUrl        = data[FcmKey.IMAGE].orEmpty()
        val timestamp       = data[FcmKey.TIMESTAMP].orEmpty()
        val packageName     = data[FcmKey.P_NAME].orEmpty()

        serviceScope.launch {
            val notificationsEnabled = prefsManager.notificationsEnabled.first()
            val notificationId = System.currentTimeMillis().toInt()

            when {
                messageType.equals(FcmMsgType.CONTENT, ignoreCase = true) ||
                messageType.equals(FcmMsgType.LINK, ignoreCase = true) -> {
                    // Save to DB
                    val insertedId = fcmRepository.insert(
                        FcmData(
                            messageType = messageType,
                            notificationType = notificationType,
                            title = title,
                            message = message,
                            imageUrl = imageUrl,
                            timestamp = timestamp,
                            isRead = false
                        )
                    ).toInt()

                    if (notificationsEnabled && insertedId > 0) {
                        val tapIntent = Intent(this@SmartResumeMessagingService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_FCM_DATA_ID, insertedId)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            this@SmartResumeMessagingService, insertedId, tapIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val bitmap = if (imageUrl.isNotEmpty()) getBitmapFromUrl(imageUrl) else null
                        showContentNotification(insertedId, notificationType, title, message, bitmap, pendingIntent)
                    }
                }

                messageType.equals(FcmMsgType.PROMOTION, ignoreCase = true) -> {
                    if (notificationsEnabled && packageName.isNotEmpty() &&
                        !isAppInstalled(packageName)
                    ) {
                        val tapIntent = Intent(Intent.ACTION_VIEW).apply {
                            setData(android.net.Uri.parse("market://details?id=$packageName"))
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            this@SmartResumeMessagingService, notificationId, tapIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val bitmap = if (imageUrl.isNotEmpty()) getBitmapFromUrl(imageUrl) else null
                        showContentNotification(notificationId, notificationType, title, message, bitmap, pendingIntent)
                    }
                }
            }
        }
    }

    private fun showContentNotification(
        notificationId: Int,
        notificationType: String,
        title: String,
        message: String,
        bitmap: Bitmap?,
        pendingIntent: PendingIntent
    ) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        bitmap?.let { builder.setLargeIcon(it) }

        when (notificationType) {
            FcmNotiType.BIG_IMAGE -> {
                if (bitmap != null) {
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .setBigContentTitle(title)
                            .setSummaryText(message)
                            .bigPicture(bitmap)
                    )
                } else {
                    builder.setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(title)
                            .bigText(message)
                    )
                }
            }
            else -> {
                // BIG_TEXT, CUSTOM_BIG_TEXT, CUSTOM_BIG_IMAGE → BigText
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(message)
                )
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun getBitmapFromUrl(url: String): Bitmap? {
        return try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                doInput = true
                connect()
            }
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: IOException) {
            null
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
// update 112
