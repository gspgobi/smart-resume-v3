package com.nithra.nithraresume.ui.splash

import android.content.Context
import android.provider.Settings
import com.nithra.nithraresume.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.api.ApiRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsManager: PrefsManager,
    private val apiRepository: ApiRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        viewModelScope.launch {
            performAppInit()
            _isReady.value = true
        }
    }

    private suspend fun performAppInit() {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val storedVersionCode = prefsManager.currentAppVersionCode.first()

        if (storedVersionCode == 0) {
            // First ever launch — mark as a brand-new V2+ user
            prefsManager.setIsPerfectNewSrv2User(true)
            prefsManager.setAppInstalledDuringSrv2DbVersion(1)
        }

        if (storedVersionCode != currentVersionCode) {
            prefsManager.setCurrentAppVersionCode(currentVersionCode)
        }

        // Set Android ID as Firebase Analytics user property
        val androidId = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        ).orEmpty()
        analyticsManager.setUserId(androidId)

        // Retry FCM token registration if it never reached the server
        val tokenSent = prefsManager.fcmTokenSentToServer.first()
        if (!tokenSent) {
            val token = prefsManager.fcmTokenId.first()
            if (token.isNotEmpty()) {
                apiRepository.registerFcmToken(token, firstOrUpdate = "update")
            }
        }
    }

}
