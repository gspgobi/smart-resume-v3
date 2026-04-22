package com.nithra.nithraresume.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.ads.AdSize
import com.nithra.nithraresume.BuildConfig
import com.nithra.nithraresume.ui.common.FeedbackDialog
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.utils.AdMobManager
import com.nithra.nithraresume.utils.AssetDir
import com.nithra.nithraresume.utils.AssetFile
import com.nithra.nithraresume.utils.BannerAdView
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.nithra.nithraresume.ui.theme.SmartResumeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSend = { email, feedback ->
                viewModel.sendFeedback(email, feedback)
                scope.launch { snackbarHostState.showSnackbar("Thank you for your feedback!") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                unreadCount = unreadCount,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    when (item) {
                        DrawerItem.Home           -> { /* already here */ }
                        DrawerItem.SampleResumes  -> navController.navigate(Screen.SampleResumes.route)
                        DrawerItem.Notifications  -> navController.navigate(Screen.Notifications.route)
                        DrawerItem.ResumeTips     -> openResumeTipsPdf(context)
                        DrawerItem.Settings       -> navController.navigate(Screen.AppSettings.route)
                        DrawerItem.Feedback       -> showFeedbackDialog = true
                        DrawerItem.PrivacyPolicy  -> openPrivacyPolicy(context)
                        DrawerItem.RateUs         -> openPlayStore(context)
                        DrawerItem.InviteFriends  -> shareApp(context)
                    }
                },
                appVersionName = BuildConfig.VERSION_NAME
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Smart Resume") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadCount > 0) Icons.Default.Notifications
                                                  else Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            MainContent(
                modifier = Modifier.padding(innerPadding),
                onMyProfilesClick = { navController.navigate(Screen.UserProfiles.route) },
                onViewResumesClick = { navController.navigate(Screen.UserProfiles.route) }
            )
        }
    }
}

// ── Main body ─────────────────────────────────────────────────────────────────

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    onMyProfilesClick: () -> Unit,
    onViewResumesClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        HomeCard(
            icon = Icons.Default.Person,
            title = "My Profiles",
            subtitle = "Create and manage your resume profiles",
            onClick = onMyProfilesClick
        )

        HomeCard(
            icon = Icons.Default.Description,
            title = "View Resumes",
            subtitle = "View and share your generated resumes",
            onClick = onViewResumesClick
        )

        Spacer(Modifier.weight(1f))

        BannerAdView(
            adUnitId = AdMobManager.banner01Id(),
            adSize = AdSize.MEDIUM_RECTANGLE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HomeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Drawer ────────────────────────────────────────────────────────────────────

private enum class DrawerItem {
    Home, SampleResumes, Notifications, ResumeTips,
    Settings, Feedback, PrivacyPolicy, RateUs, InviteFriends
}

@Composable
private fun MainDrawerContent(
    unreadCount: Int,
    appVersionName: String,
    onItemClick: (DrawerItem) -> Unit
) {
    ModalDrawerSheet {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Smart Resume",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Version $appVersionName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = true,
            onClick = { onItemClick(DrawerItem.Home) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
            label = { Text("Sample Resumes") },
            selected = false,
            onClick = { onItemClick(DrawerItem.SampleResumes) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = {
                BadgedBox(badge = {
                    if (unreadCount > 0) Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                }
            },
            label = { Text("Notifications") },
            selected = false,
            onClick = { onItemClick(DrawerItem.Notifications) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.TipsAndUpdates, contentDescription = null) },
            label = { Text("Resume Making Tips") },
            selected = false,
            onClick = { onItemClick(DrawerItem.ResumeTips) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = false,
            onClick = { onItemClick(DrawerItem.Settings) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Feedback, contentDescription = null) },
            label = { Text("Feedback") },
            selected = false,
            onClick = { onItemClick(DrawerItem.Feedback) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Policy, contentDescription = null) },
            label = { Text("Privacy Policy") },
            selected = false,
            onClick = { onItemClick(DrawerItem.PrivacyPolicy) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("Rate Us") },
            selected = false,
            onClick = { onItemClick(DrawerItem.RateUs) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Share, contentDescription = null) },
            label = { Text("Invite Friends") },
            selected = false,
            onClick = { onItemClick(DrawerItem.InviteFriends) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

// ── Intent helpers ────────────────────────────────────────────────────────────

private fun openResumeTipsPdf(context: Context) {
    runCatching {
        val assetPath = "${AssetDir.RESUME_GUIDE}/${AssetFile.RESUME_GUIDE_PDF}"
        val outFile = File(context.filesDir, AssetFile.RESUME_GUIDE_PDF)
        context.assets.open(assetPath).use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private fun openPrivacyPolicy(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW,
        "https://www.nithra.mobi/privacy/smart_resume_privacy_policy.html".toUri())
    runCatching { context.startActivity(intent) }
}

private fun openPlayStore(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW,
                "market://details?id=${context.packageName}".toUri())
        )
    }.onFailure {
        context.startActivity(
            Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
        )
    }
}

private fun shareApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT,
            "Create a professional resume easily with Smart Resume!\n" +
            "https://play.google.com/store/apps/details?id=${context.packageName}")
    }
    context.startActivity(Intent.createChooser(intent, "Invite Friends"))
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Main Content")
@Composable
private fun MainContentPreview() {
    SmartResumeTheme {
        MainContent(
            onMyProfilesClick = {},
            onViewResumesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Card")
@Composable
private fun HomeCardPreview() {
    SmartResumeTheme {
        HomeCard(
            icon = Icons.Default.Person,
            title = "My Profiles",
            subtitle = "Create and manage your resume profiles",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Drawer — with badge", widthDp = 360)
@Composable
private fun MainDrawerContentWithBadgePreview() {
    SmartResumeTheme {
        MainDrawerContent(
            unreadCount = 5,
            appVersionName = BuildConfig.VERSION_NAME,
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Drawer — no badge", widthDp = 360)
@Composable
private fun MainDrawerContentNoBadgePreview() {
    SmartResumeTheme {
        MainDrawerContent(
            unreadCount = 0,
            appVersionName = BuildConfig.VERSION_NAME,
            onItemClick = {}
        )
    }
}
