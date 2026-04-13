package com.nithra.nithraresume.data.api

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.nithra.nithraresume.BuildConfig
import com.nithra.nithraresume.utils.PrefsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val APP_TYPE = "SM"

@Singleton
class ApiRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val prefsManager: PrefsManager
) {

    suspend fun registerFcmToken(token: String, firstOrUpdate: String = "first") {
        runCatching {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
            val metrics = getDisplayMetrics()
            val response = apiService.registerFcmToken(
                firstOrUpdate = firstOrUpdate,
                appType       = APP_TYPE,
                androidId     = androidId,
                token         = token,
                versionName   = BuildConfig.VERSION_NAME,
                versionCode   = BuildConfig.VERSION_CODE.toString(),
                androidVersion = Build.VERSION.RELEASE,
                sw  = "", asw = "",
                widthPixels  = metrics.widthPixels.toString(),
                heightPixels = metrics.heightPixels.toString(),
                density      = metrics.density.toString(),
                buildModel   = Build.MODEL,
                uid          = androidId
            )
            if (response.isSuccessful) {
                prefsManager.setFcmTokenSentToServer(true)
                prefsManager.setFcmTokenId(token)
            }
        }
    }

    suspend fun postFeedback(feedback: String, email: String) {
        runCatching {
            apiService.postFeedback(
                appType       = APP_TYPE,
                feedbackText  = feedback,
                emailId       = email,
                appVersionCode = BuildConfig.VERSION_CODE.toString(),
                deviceModelName = Build.MODEL
            )
        }
    }

    suspend fun postReferrer(source: String, medium: String, comp: String, email: String) {
        runCatching {
            apiService.postReferrer(
                appType = APP_TYPE,
                source  = source,
                medium  = medium,
                comp    = comp,
                emailId = email
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun getDisplayMetrics(): DisplayMetrics {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(metrics)
        } else {
            wm.defaultDisplay.getMetrics(metrics)
        }
        return metrics
    }
}
