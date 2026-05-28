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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.nithra.nithraresume.utils.SharedAdViewModel
import com.nithra.nithraresume.utils.SharedBannerAdView
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
    private var showUpdateReadyBanner by mutableStateOf(false)
    private val exitAdHelper = InterstitialAdHelper()

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { /* flexible update — no action needed on result */ }

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateReadyBanner = true
        }
    }

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

        appUpdateManager.registerListener(installStateListener)
        requestNotificationsPermissionIfNeeded()
        exitAdHelper.load(this, AdMobManager.interstitial01Id())

        val fcmDataId = intent.getIntExtra(SmartResumeMessagingService.EXTRA_FCM_DATA_ID, -1)

        setContent {
            SmartResumeTheme {
                val sharedAdViewModel: SharedAdViewModel = hiltViewModel()
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .consumeWindowInsets(WindowInsets.navigationBars)
                    ) {
                        val navController = rememberNavController()

                        if (fcmDataId > 0) {
                            LaunchedEffect(fcmDataId) {
                                navController.navigate(Screen.NotificationDetail.createRoute(fcmDataId))
                            }
                        }

                        SmartResumeNavGraph(navController = navController, onExitApp = ::onExitApp, analyticsManager = analyticsManager)

                        if (showExitOverlay) AppExitOverlay()

                        if (showUpdateReadyBanner) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "An update is ready to install.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    TextButton(onClick = { appUpdateManager.completeUpdate() }) {
                                        Text(
                                            text = "Restart",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    SharedBannerAdView(viewModel = sharedAdViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    appUpdateManager.startUpdateFlowForResult(
                        info, updateResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                }
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    showUpdateReadyBanner = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateListener)
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
