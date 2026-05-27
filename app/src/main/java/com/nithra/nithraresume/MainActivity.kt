package com.nithra.nithraresume

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.nithra.nithraresume.service.SmartResumeMessagingService
import com.nithra.nithraresume.ui.common.AppExitOverlay
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.ui.navigation.SmartResumeNavGraph
import com.nithra.nithraresume.ui.splash.SplashViewModel
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.AdMobManager
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.InterstitialAdHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var analyticsManager: AnalyticsManager

    private val splashViewModel: SplashViewModel by viewModels()

    private var showExitOverlay by mutableStateOf(false)
    private val exitAdHelper = InterstitialAdHelper()

    private fun onExitApp() {
        showExitOverlay = true
        exitAdHelper.show(this) {
            lifecycleScope.launch {
                delay(1500)
                finishAffinity()
            }
        }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // User responded — proceed regardless of grant/deny
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            !splashViewModel.isReady.value
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationsPermissionIfNeeded()
        exitAdHelper.load(this, AdMobManager.interstitial01Id())

        val fcmDataId = intent.getIntExtra(SmartResumeMessagingService.EXTRA_FCM_DATA_ID, -1)

        setContent {
            SmartResumeTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    if (fcmDataId > 0) {
                        LaunchedEffect(fcmDataId) {
                            navController.navigate(Screen.NotificationDetail.createRoute(fcmDataId))
                        }
                    }

                    SmartResumeNavGraph(navController = navController, onExitApp = ::onExitApp, analyticsManager = analyticsManager)

                    if (showExitOverlay) {
                        AppExitOverlay()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
