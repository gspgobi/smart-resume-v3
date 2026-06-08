package com.nithra.nithraresume.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.MAX_PROFILES
import com.nithra.nithraresume.utils.verticalScrollbar
import com.nithra.nithraresume.ui.preview.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Dialog state
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var targetProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(Unit) {
        if (viewModel.dummyCreated) {
            snackbarHostState.showSnackbar("Dummy profile created!")
        }
    }

    // React to uiState changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is UserProfileUiState.ProfileCreated -> {
                viewModel.resetUiState()
                snackbarHostState.showSnackbar("Profile created")
            }
            is UserProfileUiState.ProfileRenamed -> {
                viewModel.resetUiState()
                snackbarHostState.showSnackbar("Profile renamed")
            }
            is UserProfileUiState.ProfileDeleted -> {
                viewModel.resetUiState()
                snackbarHostState.showSnackbar("Profile deleted")
            }
            is UserProfileUiState.Error -> {
                val msg = (uiState as UserProfileUiState.Error).message
                viewModel.resetUiState()
                snackbarHostState.showSnackbar(msg)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Profiles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (profiles.size >= MAX_PROFILES) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Maximum $MAX_PROFILES profiles reached")
                        }
                    } else {
                        showCreateDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add profile",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScrollbar(listState)
        ) {
            if (profiles.isEmpty()) {
                item(key = "empty_state") {
                    EmptyProfilesPlaceholder()
                    HorizontalDivider()
                }
            } else {
                items(profiles, key = { it.id }) { profile ->
                    ProfileItem(
                        profile = profile,
                        onProfileClick = {
                            navController.navigate(Screen.SectionHead.createRoute(profile.id))
                        },
                        onRenameClick = {
                            targetProfile = profile
                            showRenameDialog = true
                        },
                        onDeleteClick = {
                            targetProfile = profile
                            showDeleteDialog = true
                        }
                    )
                    HorizontalDivider()
                }
            }

            item(key = "create_new") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 64.dp)
                        .clickable {
                            if (profiles.size >= MAX_PROFILES) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Maximum $MAX_PROFILES profiles reached")
                                }
                            } else {
                                showCreateDialog = true
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddBox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Create New Profile",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }
                HorizontalDivider()
            }

            item(key = "browse_samples") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { navController.navigate(Screen.SampleResumes.route) },
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
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Browse Sample Resumes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Explore ready-made resumes for every career",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Create dialog ─────────────────────────────────────────────────────────
    if (showCreateDialog) {
        ProfileNameDialog(
            title = "Create Profile",
            confirmLabel = "Create",
            initialName = viewModel.suggestNewProfileName(profiles),
            onConfirm = { name ->
                showCreateDialog = false
                viewModel.createProfile(name, profiles)
            },
            onDismiss = { showCreateDialog = false },
            isDuplicate = { name -> viewModel.isNameDuplicate(name, profiles) }
        )
    }

    // ── Rename dialog ─────────────────────────────────────────────────────────
    if (showRenameDialog && targetProfile != null) {
        val profile = targetProfile!!
        ProfileNameDialog(
            title = "Rename Profile",
            confirmLabel = "Rename",
            initialName = profile.name,
            onConfirm = { name ->
                showRenameDialog = false
                viewModel.renameProfile(profile, name)
                targetProfile = null
            },
            onDismiss = {
                showRenameDialog = false
                targetProfile = null
            },
            isDuplicate = { name ->
                name != profile.name && viewModel.isNameDuplicate(name, profiles)
            }
        )
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (showDeleteDialog && targetProfile != null) {
        val profile = targetProfile!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; targetProfile = null },
            title = { Text("Delete Profile") },
            text = { Text("Delete \"${profile.name}\"? All sections and data for this profile will be permanently removed.") },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteProfile(profile, profiles)
                    targetProfile = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; targetProfile = null }) {
                    Text("Cancel")
                }
            }
        )
    }

}

// ── Profile list item ─────────────────────────────────────────────────────────

@Composable
private fun ProfileItem(
    profile: UserProfile,
    onProfileClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
                    onClick = { menuExpanded = false; onRenameClick() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = { menuExpanded = false; onDeleteClick() }
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyProfilesPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No profiles yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first resume profile",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// ── Reusable profile name dialog ──────────────────────────────────────────────

@Composable
private fun ProfileNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    isDuplicate: (String) -> Boolean
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var error by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = { Text("Profile name") },
                    singleLine = true,
                    isError = error.isNotEmpty(),
                    supportingText = if (error.isNotEmpty()) {{ Text(error) }} else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmed = name.trim()
                when {
                    trimmed.isEmpty() -> error = "Profile name can't be empty"
                    isDuplicate(trimmed) -> error = "Profile name already exists"
                    else -> onConfirm(trimmed)
                }
            }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewProfiles = listOf(
    UserProfile(id = 1, name = "Software Engineer", indexPosition = 0, isSampleProfile = false, sampleProfileId = null, resumeFormatBaseId = 1, fontStyle = "Default", fontSize = 12, backgroundColor = "", resumeFileName = null),
    UserProfile(id = 2, name = "Product Manager", indexPosition = 1, isSampleProfile = false, sampleProfileId = null, resumeFormatBaseId = 1, fontStyle = "Default", fontSize = 12, backgroundColor = "", resumeFileName = null),
    UserProfile(id = 3, name = "UX Designer", indexPosition = 2, isSampleProfile = false, sampleProfileId = null, resumeFormatBaseId = 1, fontStyle = "Default", fontSize = 12, backgroundColor = "", resumeFileName = null),
)

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun UserProfileScreenListPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Resume Profiles") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Add, contentDescription = "Add profile", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                items(previewProfiles, key = { it.id }) { profile ->
                    ProfileItem(profile = profile, onProfileClick = {}, onRenameClick = {}, onDeleteClick = {})
                    HorizontalDivider()
                }
                item(key = "create_new") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Text("Create New Profile", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp))
                    }
                    HorizontalDivider()
                }
                item(key = "browse_samples") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Browse Sample Resumes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Explore ready-made resumes for every career",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun UserProfileScreenEmptyPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Resume Profiles") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.AddBox, contentDescription = "Add profile", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                item(key = "empty_state") {
                    EmptyProfilesPlaceholder()
                }
                item(key = "create_new") {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Text("Create New Profile", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp))
                    }
                    HorizontalDivider()
                }
                item(key = "browse_samples") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Browse Sample Resumes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Explore ready-made resumes for every career",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun ProfileItemPreview() {
    SmartResumeTheme {
        ProfileItem(
            profile = previewProfiles.first(),
            onProfileClick = {},
            onRenameClick = {},
            onDeleteClick = {}
        )
    }
}

@AppPreview
@Composable
private fun CreateProfileDialogPreview() {
    SmartResumeTheme {
        ProfileNameDialog(
            title = "Create Profile",
            confirmLabel = "Create",
            initialName = "My Resume",
            onConfirm = {},
            onDismiss = {},
            isDuplicate = { false }
        )
    }
}

@AppPreview
@Composable
private fun RenameProfileDialogPreview() {
    SmartResumeTheme {
        ProfileNameDialog(
            title = "Rename Profile",
            confirmLabel = "Rename",
            initialName = "Software Engineer",
            onConfirm = {},
            onDismiss = {},
            isDuplicate = { false }
        )
    }
}
