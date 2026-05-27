package com.nithra.nithraresume.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.nithra.nithraresume.R
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.BuildConfig
import com.nithra.nithraresume.ui.common.FeedbackDialog
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.utils.AssetDir
import com.nithra.nithraresume.utils.AssetFile
import com.nithra.nithraresume.utils.MediumRectangleAdBottomBar
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.net.toUri
import com.nithra.nithraresume.ui.preview.AppDrawerPreview
import com.nithra.nithraresume.ui.preview.AppPreview
import com.nithra.nithraresume.ui.theme.SmartResumeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel(),
    onExitApp: () -> Unit = {}
) {
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val migrationState by viewModel.migrationState.collectAsStateWithLifecycle()
    val showDoYouLoveAppDialog by viewModel.showDoYouLoveAppDialog.collectAsStateWithLifecycle()
    val showRateUs5StarsDialog by viewModel.showRateUs5StarsDialog.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var exitAfterFeedback by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val permName = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onPermissionResult(granted) }

    val migrationDialogText = remember {
        buildAnnotatedString {
            append("The app has been updated. To move your ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("photos") }
            append(", ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("signatures") }
            append(", & ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("created resume PDFs") }
            append(" to the new location, storage permission is required.")
        }
    }

    LaunchedEffect(Unit) { viewModel.onScreenOpened() }

    LaunchedEffect(Unit) {
        viewModel.dummyProfileCreated.collect {
            scope.launch { drawerState.close() }
            navController.navigate(Screen.UserProfiles.createRoute(dummyCreated = true))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.rateUsEvent.collect { event ->
            when (event) {
                RateUsEvent.TriggerExit          -> onExitApp()
                RateUsEvent.OpenPlayStore        -> openPlayStore(context)
                RateUsEvent.ShowFeedbackThenExit -> { showFeedbackDialog = true; exitAfterFeedback = true }
            }
        }
    }

    LaunchedEffect(migrationState) {
        if (migrationState is MigrationUiState.PermissionDenied) {
            snackbarHostState.showSnackbar(
                "Permission denied. Photos from the previous version could not be restored."
            )
            viewModel.acknowledgeMigrationDenied()
        }
    }

    if (migrationState is MigrationUiState.ShowRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.onPermissionResult(false) },
            title = { Text("App Updated") },
            text  = { Text(migrationDialogText) },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(permName) }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onPermissionResult(false) }) { Text("Skip file migration") }
            }
        )
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = {
                showFeedbackDialog = false
                if (exitAfterFeedback) {
                    exitAfterFeedback = false
                    onExitApp()
                }
            },
            onSend = { email, feedback ->
                viewModel.sendFeedback(email, feedback)
                scope.launch { snackbarHostState.showSnackbar("Thank you for your feedback!") }
            }
        )
    }

    if (showDoYouLoveAppDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDoYouLoveAppDismissed() },
            title = { Text("Enjoying Smart Resume Builder?") },
            text  = { Text("We'd love to hear how it's working for you — your feedback helps us keep improving!") },
            confirmButton = {
                Button(onClick = { viewModel.onLoveItClicked() }) { Text("Love it! ❤️") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCouldBeBetterClicked() }) { Text("Could be better") }
            }
        )
    }

    if (showRateUs5StarsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onMaybeLater() },
            title = { Text("Glad you love it! 🎉") },
            text  = { Text("A 5-star ⭐⭐⭐⭐⭐ rating on the Play Store helps more job seekers find us and keeps the app free. It only takes a second!") },
            confirmButton = {
                Button(onClick = { viewModel.onSureTakeMeThere() }) { Text("Rate Now") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.onMaybeLater() }) { Text("Later") }
                    TextButton(onClick = { viewModel.onNoThanks() }) { Text("No thanks") }
                }
            }
        )
    }

    BackHandler(enabled = !drawerState.isOpen) { viewModel.onExitRequested() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                unreadCount = unreadCount,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    when (item) {
                        DrawerItem.Home           -> { /* already here */ }
                        DrawerItem.SampleResumes  -> { viewModel.onNavSampleResumesClicked(); navController.navigate(Screen.SampleResumes.route) }
                        DrawerItem.Notifications  -> { viewModel.onNavNotificationsClicked(); navController.navigate(Screen.Notifications.route) }
                        DrawerItem.ResumeTips     -> { viewModel.onNavResumeTipsClicked(); openResumeTipsPdf(context) }
                        DrawerItem.Settings       -> { viewModel.onNavAppSettingsClicked(); navController.navigate(Screen.AppSettings.route) }
                        DrawerItem.Feedback       -> { viewModel.onNavFeedbackClicked(); showFeedbackDialog = true }
                        DrawerItem.PrivacyPolicy  -> { viewModel.onNavPrivacyPolicyClicked(); openPrivacyPolicy(context) }
                        DrawerItem.RateUs         -> { viewModel.onNavRateUsClicked(); openPlayStore(context) }
                        DrawerItem.InviteFriends  -> { viewModel.onNavInviteFriendsClicked(); shareApp(context) }
                    }
                },
                appVersionName = BuildConfig.VERSION_NAME,
                onVersionTap = { viewModel.createDummyProfile() }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Smart Resume Builder") },
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
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Feedback") },
                                leadingIcon = { Icon(Icons.Default.Feedback, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    showFeedbackDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rate Us") },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    openPlayStore(context)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Invite Friends") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    shareApp(context)
                                }
                            )
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
            bottomBar = { MediumRectangleAdBottomBar() },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            MainContent(
                modifier = Modifier.padding(innerPadding),
                migrationState = migrationState,
                onMyProfilesClick = { navController.navigate(Screen.UserProfiles.route) },
                onViewResumesClick = { navController.navigate(Screen.GeneratedResumes.route) }
            )
        }
    }
}

// ── Main body ─────────────────────────────────────────────────────────────────

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    migrationState: MigrationUiState = MigrationUiState.Idle,
    onMyProfilesClick: () -> Unit,
    onViewResumesClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (migrationState is MigrationUiState.Running) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Restoring files… (${migrationState.done} / ${migrationState.total})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        if (migrationState.total > 0) migrationState.done.toFloat() / migrationState.total else 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HomeCard(
            icon = Icons.Default.Person,
            title = "My Resume Profiles",
            subtitle = "Create and manage your resume profiles",
            onClick = onMyProfilesClick
        )

        HomeCard(
            icon = Icons.Default.Description,
            title = "View Saved Resumes",
            subtitle = "View and share your generated resumes",
            onClick = onViewResumesClick
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
    onItemClick: (DrawerItem) -> Unit,
    onVersionTap: () -> Unit = {}
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var firstTapTime by remember { mutableLongStateOf(0L) }

    ModalDrawerSheet {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_launcher_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Smart Resume Builder",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Version $appVersionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.clickable {
                            val now = System.currentTimeMillis()
                            if (tapCount > 0 && now - firstTapTime > 3000L) {
                                tapCount = 1
                                firstTapTime = now
                            } else {
                                if (tapCount == 0) firstTapTime = now
                                tapCount++
                                if (tapCount >= 12) {
                                    onVersionTap()
                                    tapCount = 0
                                }
                            }
                        }
                    )
                }
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

@AppPreview
@Composable
private fun MainContentPreview() {
    SmartResumeTheme {
        MainContent(
            onMyProfilesClick = {},
            onViewResumesClick = {}
        )
    }
}

@AppPreview
@Composable
private fun MigrationPermissionDialogPreview() {
    SmartResumeTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("App Updated", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("The app has been updated. To move your ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("photos") }
                            append(", ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("signatures") }
                            append(", & ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("created resume PDFs") }
                            append(" to the new location, storage permission is required.")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {}) { Text("Skip file migration") }
                        Button(onClick = {}) { Text("Allow") }
                    }
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun MainContentMigrationRunningPreview() {
    SmartResumeTheme {
        MainContent(
            migrationState = MigrationUiState.Running(done = 3, total = 7),
            onMyProfilesClick = {},
            onViewResumesClick = {}
        )
    }
}

@AppPreview
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

@AppDrawerPreview
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

@AppDrawerPreview
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

@AppPreview
@Composable
private fun DoYouLoveAppDialogPreview() {
    SmartResumeTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Enjoying Smart Resume Builder?", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "We'd love to hear how it's working for you — your feedback helps us keep improving!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {}) { Text("Could be better") }
                        Button(onClick = {}) { Text("Love it! ❤️") }
                    }
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun RateUs5StarsDialogPreview() {
    SmartResumeTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Glad you love it! 🎉", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "A 5-star ⭐⭐⭐⭐⭐ rating on the Play Store helps more job seekers find us and keeps the app free. It only takes a second!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row {
                            TextButton(onClick = {}) { Text("Later") }
                            TextButton(onClick = {}) { Text("No thanks") }
                        }
                        Button(onClick = {}) { Text("Rate Now") }
                    }
                }
            }
        }
    }
}
