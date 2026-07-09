package com.nithra.nithraresume.ui.section.head

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.SectionHeadSampleData
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.GROUP_ID_ADDONS
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import com.nithra.nithraresume.utils.verticalScrollbar
import kotlinx.coroutines.launch
import com.nithra.nithraresume.ui.preview.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionHeadScreen(
    navController: NavController,
    viewModel: SectionHeadViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentFormat by viewModel.currentFormat.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val addons by viewModel.addons.collectAsStateWithLifecycle()
    val availableSections by viewModel.availableSections.collectAsStateWithLifecycle()
    val availableAddons by viewModel.availableAddons.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Bottom sheet state
    var showAddSectionSheet by rememberSaveable { mutableStateOf(false) }
    var showAddAddonSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Delete confirmation
    var deleteTarget by remember { mutableStateOf<SectionHeadAdded?>(null) }

    LaunchedEffect(uiEvent) {
        if (uiEvent is SectionHeadUiEvent.Error) {
            snackbarHostState.showSnackbar((uiEvent as SectionHeadUiEvent.Error).message)
            viewModel.resetUiEvent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.name ?: "Edit Profile") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScrollbar(listState),
        ) {

            // ── Resume format row ─────────────────────────────────────────────
            item {
                ResumeFormatRow(
                    formatTitle = currentFormat?.title ?: "",
                    onClick = {
                        navController.navigate(Screen.ResumeFormat.createRoute(viewModel.profileId))
                    }
                )
                HorizontalDivider()
            }

            // ── Sections group header ─────────────────────────────────────────
            item {
                GroupHeader(
                    title = "Sections",
                    onEditClick = if (sections.size > 1) {
                        { navController.navigate(Screen.ReorderSections.createRoute(viewModel.profileId, GROUP_ID_SECTIONS)) }
                    } else null
                )
            }

            // ── Section items ─────────────────────────────────────────────────
            items(sections, key = { it.id }) { sha ->
                SectionItem(
                    sha = sha,
                    isContactInfo = sha.headBaseId == 1,
                    onClick = { navController.navigate(childRoute(sha)) },
                    onToggleEnable = { viewModel.toggleEnable(sha) },
                    onDelete = { deleteTarget = sha }
                )
                HorizontalDivider()
            }

            // ── Add New Section ───────────────────────────────────────────────
            if (availableSections.isNotEmpty()) {
                item {
                    AddItemRow(label = "Add New Section") {
                        showAddSectionSheet = true
                    }
                    HorizontalDivider()
                }
            }

            // ── Add-ons group header ──────────────────────────────────────────
            item {
                GroupHeader(
                    title = "Add-ons",
                    onEditClick = if (addons.size > 1) {
                        { navController.navigate(Screen.ReorderSections.createRoute(viewModel.profileId, GROUP_ID_ADDONS)) }
                    } else null
                )
            }

            // ── Addon items ───────────────────────────────────────────────────
            items(addons, key = { it.id }) { sha ->
                SectionItem(
                    sha = sha,
                    isContactInfo = false,
                    onClick = { navController.navigate(childRoute(sha)) },
                    onToggleEnable = { viewModel.toggleEnable(sha) },
                    onDelete = { deleteTarget = sha }
                )
                HorizontalDivider()
            }

            // ── Add New Add-on (only when available) ─────────────────────────
            if (availableAddons.isNotEmpty()) {
                item {
                    AddItemRow(label = "Add New Add-on") {
                        showAddAddonSheet = true
                    }
                    HorizontalDivider()
                }
            }

            // ── Complete group header ──────────────────────────────────────────
            item {
                GroupHeader(
                    title = "Complete"
                )
            }

            // ── Generate + View/Share buttons ─────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                ActionButtons(
                    onGenerate = {
                        navController.navigate(Screen.GenerateResume.createRoute(viewModel.profileId))
                    },
                    onViewShare = {
                        navController.navigate(Screen.ViewShare.createRoute(viewModel.profileId))
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Add Section bottom sheet ──────────────────────────────────────────────
    if (showAddSectionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSectionSheet = false },
            sheetState = sheetState
        ) {
            AddSectionSheet(
                title = "Add New Section",
                items = availableSections,
                onItemClick = { sample ->
                    viewModel.addSection(sample, sections)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showAddSectionSheet = false
                    }
                }
            )
        }
    }

    // ── Add Add-on bottom sheet ───────────────────────────────────────────────
    if (showAddAddonSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddAddonSheet = false },
            sheetState = sheetState
        ) {
            AddSectionSheet(
                title = "Add New Add-on",
                items = availableAddons,
                onItemClick = { sample ->
                    viewModel.addSection(sample, addons)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showAddAddonSheet = false
                    }
                }
            )
        }
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (deleteTarget != null) {
        val sha = deleteTarget!!
        val groupList = if (sha.groupBaseId == 1) sections else addons
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Section") },
            text = { Text("Delete \"${sha.title.ifEmpty { "this section" }}\"? All data in this section will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSection(sha, groupList)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

// ── Helper: map sectionHeadBaseId → route ────────────────────────────────────

private fun childRoute(sha: SectionHeadAdded): String = when (sha.headBaseId) {
    1 -> Screen.SectionChild1.createRoute(sha.id)
    2 -> Screen.SectionChild2.createRoute(sha.id)
    3 -> Screen.SectionChild3.createRoute(sha.id)
    4 -> Screen.SectionChild4.createRoute(sha.id)
    5 -> Screen.SectionChild5.createRoute(sha.id)
    6 -> Screen.SectionChild6.createRoute(sha.id)
    7 -> Screen.SectionChild7.createRoute(sha.id)
    8 -> Screen.SectionChild8.createRoute(sha.id)
    else -> Screen.SectionChild1.createRoute(sha.id)
}

// ── Resume format row ─────────────────────────────────────────────────────────

@Composable
private fun ResumeFormatRow(formatTitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "Resume Format",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = formatTitle.ifEmpty { "Select format" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Group header ──────────────────────────────────────────────────────────────

@Composable
private fun GroupHeader(
    title: String,
    onEditClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        if (onEditClick != null) {
            TextButton(
                onClick = onEditClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Edit", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Add item row ──────────────────────────────────────────────────────────────

@Composable
private fun AddItemRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AddBox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Section item ──────────────────────────────────────────────────────────────

@Composable
private fun SectionItem(
    sha: SectionHeadAdded,
    isContactInfo: Boolean,
    onClick: () -> Unit,
    onToggleEnable: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val titleColor = if (sha.isEnable) MaterialTheme.colorScheme.onSurface
                         else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

        Column(modifier = Modifier.weight(1f)) {
            if (sha.title.isBlank()) {
                Text(
                    text = "No title",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = titleColor.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    text = sha.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )
            }
        }

        if (isContactInfo) {
            // Contact Info: only chevron — sized to match IconButton's 48 dp touch target
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options",
                        modifier = Modifier.size(24.dp))
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (sha.isEnable) "Disable" else "Enable") },
                        leadingIcon = {
                            Icon(
                                if (sha.isEnable) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        },
                        onClick = { menuExpanded = false; onToggleEnable() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

// ── Generate / View Share buttons ────────────────────────────────────────────

@Composable
private fun ActionButtons(
    onGenerate: () -> Unit,
    onViewShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Generate Resume")
        }
        OutlinedButton(
            onClick = onViewShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("View & Share")
        }
    }
}

// ── Add Section / Add-on bottom sheet ────────────────────────────────────────

@Composable
private fun AddSectionSheet(
    title: String,
    items: List<SectionHeadSampleData>,
    onItemClick: (SectionHeadSampleData) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item(key = "sheet_title") {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            HorizontalDivider()
        }
        if (items.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "All sections already added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(items, key = { "${it.id}_${it.title}" }) { sample ->
                if (sample.id == -1) {
                    Text(
                        text = sample.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(sample) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sample.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
        item(key = "footer_spacer") {
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewSections = listOf(
    SectionHeadAdded(id = 1, profileId = 1, groupBaseId = 1, headBaseId = 1, sampleDataId = null, title = "Contact Information", isEnable = true,  indexPosition = 0),
    SectionHeadAdded(id = 2, profileId = 1, groupBaseId = 1, headBaseId = 2, sampleDataId = null, title = "Work Experience",     isEnable = true,  indexPosition = 1),
    SectionHeadAdded(id = 3, profileId = 1, groupBaseId = 1, headBaseId = 3, sampleDataId = null, title = "Education",           isEnable = true,  indexPosition = 2),
    SectionHeadAdded(id = 4, profileId = 1, groupBaseId = 1, headBaseId = 6, sampleDataId = null, title = "Skills",              isEnable = false, indexPosition = 3),
)

private val previewAddons = listOf(
    SectionHeadAdded(id = 5, profileId = 1, groupBaseId = 2, headBaseId = 8, sampleDataId = null, title = "Cover Letter", isEnable = true, indexPosition = 0),
)

private val previewAvailableSections = listOf(
    SectionHeadSampleData(id = -1, title = "Standard",        isEnable = true, isDefault = false, groupName = "Standard", sectionHeadBaseId = 0, sectionHeadGroupBaseId = 1, indexPosition = 0),
    SectionHeadSampleData(id = 1,  title = "Accomplishments", isEnable = true, isDefault = false, groupName = "Standard", sectionHeadBaseId = 4, sectionHeadGroupBaseId = 1, indexPosition = 1),
    SectionHeadSampleData(id = 2,  title = "Projects",        isEnable = true, isDefault = false, groupName = "Standard", sectionHeadBaseId = 5, sectionHeadGroupBaseId = 1, indexPosition = 2),
    SectionHeadSampleData(id = -2, title = "Custom",          isEnable = true, isDefault = false, groupName = "Custom",   sectionHeadBaseId = 0, sectionHeadGroupBaseId = 1, indexPosition = 3),
    SectionHeadSampleData(id = 3,  title = "Languages",       isEnable = true, isDefault = false, groupName = "Custom",   sectionHeadBaseId = 7, sectionHeadGroupBaseId = 1, indexPosition = 4),
)

private val previewAvailableAddons = listOf(
    SectionHeadSampleData(id = -1, title = "Add-ons",    isEnable = true, isDefault = false, groupName = "Add-ons", sectionHeadBaseId = 0, sectionHeadGroupBaseId = 2, indexPosition = 0),
    SectionHeadSampleData(id = 9,  title = "References", isEnable = true, isDefault = false, groupName = "Add-ons", sectionHeadBaseId = 9, sectionHeadGroupBaseId = 2, indexPosition = 1),
)

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun SectionHeadScreenPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Software Engineer") },
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
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                ) {
                item {
                    ResumeFormatRow(formatTitle = "Classic", onClick = {})
                    HorizontalDivider()
                }
                item {
                    GroupHeader(
                        title = "Sections",
                        onEditClick = if (previewSections.size > 1) {{}} else null
                    )
                }
                items(previewSections, key = { it.id }) { sha ->
                    SectionItem(
                        sha = sha,
                        isContactInfo = sha.headBaseId == 1,
                        onClick = {},
                        onToggleEnable = {},
                        onDelete = {}
                    )
                    HorizontalDivider()
                }
                if (previewAvailableSections.isNotEmpty()) {
                    item {
                        AddItemRow(label = "Add New Section", onClick = {})
                        HorizontalDivider()
                    }
                }
                item {
                    GroupHeader(
                        title = "Add-ons",
                        onEditClick = if (previewAddons.size > 1) {{}} else null
                    )
                }
                items(previewAddons, key = { it.id }) { sha ->
                    SectionItem(
                        sha = sha,
                        isContactInfo = false,
                        onClick = {},
                        onToggleEnable = {},
                        onDelete = {}
                    )
                    HorizontalDivider()
                }
                if (previewAvailableAddons.isNotEmpty()) {
                    item {
                        AddItemRow(label = "Add New Add-on", onClick = {})
                        HorizontalDivider()
                    }
                }
                item {
                    GroupHeader(
                        title = "Complete"
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    ActionButtons(onGenerate = {}, onViewShare = {})
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun ResumeFormatRowPreview() {
    SmartResumeTheme {
        ResumeFormatRow(formatTitle = "Classic", onClick = {})
    }
}

@AppPreview
@Composable
private fun GroupHeaderPreview() {
    SmartResumeTheme {
        GroupHeader(title = "Sections")
    }
}

@AppPreview
@Composable
private fun GroupHeaderWithEditPreview() {
    SmartResumeTheme {
        GroupHeader(title = "Sections", onEditClick = {})
    }
}

@AppPreview
@Composable
private fun AddItemRowPreview() {
    SmartResumeTheme {
        Column {
            AddItemRow(label = "Add New Section", onClick = {})
            HorizontalDivider()
            AddItemRow(label = "Add New Add-on", onClick = {})
        }
    }
}

@AppPreview
@Composable
private fun SectionItemContactInfoPreview() {
    SmartResumeTheme {
        Column {
            SectionItem(sha = previewSections[0], isContactInfo = true,
                onClick = {}, onToggleEnable = {}, onDelete = {})
            HorizontalDivider()
        }
    }
}

@AppPreview
@Composable
private fun SectionItemEnabledPreview() {
    SmartResumeTheme {
        Column {
            SectionItem(sha = previewSections[1], isContactInfo = false,
                onClick = {}, onToggleEnable = {}, onDelete = {})
            HorizontalDivider()
        }
    }
}

@AppPreview
@Composable
private fun SectionItemDisabledPreview() {
    SmartResumeTheme {
        Column {
            SectionItem(sha = previewSections[3], isContactInfo = false,
                onClick = {}, onToggleEnable = {}, onDelete = {})
            HorizontalDivider()
        }
    }
}

@AppPreview
@Composable
private fun ActionButtonsPreview() {
    SmartResumeTheme {
        ActionButtons(onGenerate = {}, onViewShare = {})
    }
}

@AppPreview
@Composable
private fun AddSectionSheetPreview() {
    SmartResumeTheme {
        AddSectionSheet(
            title = "Add New Section",
            items = previewAvailableSections,
            onItemClick = {}
        )
    }
}

@AppPreview
@Composable
private fun AddSectionSheetEmptyPreview() {
    SmartResumeTheme {
        AddSectionSheet(title = "Add New Section", items = emptyList(), onItemClick = {})
    }
}

@AppPreview
@Composable
private fun DeleteSectionDialogPreview() {
    SmartResumeTheme {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Delete Section") },
            text  = { Text("Delete \"Work Experience\"? All data in this section will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {}) { Text("Cancel") }
            }
        )
    }
}
