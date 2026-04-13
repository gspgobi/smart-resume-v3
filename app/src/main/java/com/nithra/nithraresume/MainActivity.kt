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
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.nithra.nithraresume.service.SmartResumeMessagingService
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.ui.navigation.SmartResumeNavGraph
import com.nithra.nithraresume.ui.splash.SplashViewModel
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

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

        val fcmDataId = intent.getIntExtra(SmartResumeMessagingService.EXTRA_FCM_DATA_ID, -1)

        setContent {
            SmartResumeTheme {
                val navController = rememberNavController()

                if (fcmDataId > 0) {
                    LaunchedEffect(fcmDataId) {
                        navController.navigate(Screen.NotificationDetail.createRoute(fcmDataId))
                    }
                }

                SmartResumeNavGraph(navController = navController)
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
