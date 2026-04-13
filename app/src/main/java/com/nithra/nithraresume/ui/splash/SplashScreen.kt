package com.nithra.nithraresume.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

/**
 * Splash destination in the NavHost.
 *
 * The OS splash screen (configured in themes.xml + installSplashScreen() in
 * MainActivity) is held on screen until SplashViewModel.isReady = true.
 * By the time Compose draws this composable the OS splash has already dismissed,
 * so we just navigate straight to Main on the first composition.
 */
@Composable
fun SplashScreen(onNavigateToMain: () -> Unit) {
    LaunchedEffect(Unit) {
        onNavigateToMain()
    }

    // Neutral background while the lambda resolves (typically one frame)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    )
}
// update 163
