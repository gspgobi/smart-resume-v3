package com.nithra.nithraresume.ui.section.child

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.ui.common.ObjAccompBottomSheet
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.ui.preview.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild5Screen(
    navController: NavController,
    viewModel: SectionChild5ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sha by viewModel.sha.collectAsStateWithLifecycle()
    val child5 by viewModel.child5.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var bulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var origTitle by rememberSaveable { mutableStateOf("") }
    var origContent by rememberSaveable { mutableStateOf("") }
    var origBulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var initialised by rememberSaveable { mutableStateOf(false) }
    var titleError by rememberSaveable { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (!initialised && uiState is Child5UiState.Ready) {
            sha?.let { title = it.title; origTitle = title }
            child5?.let { c5 ->
                content = c5.content; origContent = content
                bulletType = c5.contentBulletType.ifEmpty { BULLET_NONE }; origBulletType = bulletType
            }
            initialised = true
        }
        when (uiState) {
            is Child5UiState.Saved -> navController.popBackStack()
            is Child5UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child5UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isDirty = initialised && (
        title != origTitle || content != origContent || bulletType != origBulletType
    )

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    if (showSuggestions) {
        ObjAccompBottomSheet(
            onDismiss = { showSuggestions = false },
            onItemSelected = { selected ->
                content = if (content.isBlank()) selected else "$content\n$selected"
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showUnsavedDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSuggestions = true }) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Suggestions",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = {
                        if (title.isBlank()) { titleError = true } else {
                            focusManager.clearFocus()
                            viewModel.save(title, content, bulletType)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear all") },
                            onClick = {
                                showOverflowMenu = false
                                focusManager.clearFocus()
                                content = ""; bulletType = BULLET_NONE
                                viewModel.onClearAll()
                            }
                        )
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
        if (uiState is Child5UiState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Section Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) { { Text("Section title is required") } } else null
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8, maxLines = 20
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it },
                primaryText = "Show content as bullet points",
                hintText = "(Here new line will be considered as bullet points)"
            )
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Save before leaving?") },
            confirmButton = {
                Button(onClick = {
                    if (title.isBlank()) {
                        showUnsavedDialog = false
                        titleError = true
                    } else {
                        showUnsavedDialog = false
                        focusManager.clearFocus()
                        viewModel.save(title, content, bulletType)
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showUnsavedDialog = false }) { Text("Cancel") }
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        navController.popBackStack()
                    }) { Text("Discard") }
                }
            }
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun SectionChild5EmptyPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Paragraph") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Suggestions",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Check, contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onPrimary)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Section Title") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(), minLines = 8, maxLines = 20)
                BulletTypeDropdown(
                    selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show content as bullet points",
                    hintText = "(Here new line will be considered as bullet points)"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun SectionChild5FilledPreview() {
    val sampleContent = "Motivated software engineer with 5+ years of experience building scalable Android applications.\nPassionate about clean architecture and delivering high-quality user experiences.\nSeeking a senior role to drive impactful product development."
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Objective") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Suggestions",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Check, contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onPrimary)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = "Objective", onValueChange = {},
                    label = { Text("Section Title") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sampleContent, onValueChange = {},
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(), minLines = 8, maxLines = 20)
                BulletTypeDropdown(
                    selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show content as bullet points",
                    hintText = "(Here new line will be considered as bullet points)"
                )
            }
        }
    }
}
