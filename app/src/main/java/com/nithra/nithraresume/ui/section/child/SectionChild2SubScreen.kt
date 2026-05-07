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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild2SubScreen(
    navController: NavController,
    viewModel: SectionChild2SubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item by viewModel.item.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var workRole by rememberSaveable { mutableStateOf("") }
    var companyName by rememberSaveable { mutableStateOf("") }
    var subtitle by rememberSaveable { mutableStateOf("") }
    var workPeriod by rememberSaveable { mutableStateOf("") }
    var accomplishments by rememberSaveable { mutableStateOf("") }
    var bulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var origWorkRole by rememberSaveable { mutableStateOf("") }
    var origCompanyName by rememberSaveable { mutableStateOf("") }
    var origSubtitle by rememberSaveable { mutableStateOf("") }
    var origWorkPeriod by rememberSaveable { mutableStateOf("") }
    var origAccomplishments by rememberSaveable { mutableStateOf("") }
    var origBulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var initialised by rememberSaveable { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (!initialised && uiState is Child2SubUiState.Ready) {
            item?.let {
                workRole = it.workRole;         origWorkRole = workRole
                companyName = it.companyName;   origCompanyName = companyName
                subtitle = it.subtitle;         origSubtitle = subtitle
                workPeriod = it.workPeriod;     origWorkPeriod = workPeriod
                accomplishments = it.accomplishments; origAccomplishments = accomplishments
                bulletType = it.accomplishmentsBulletType.ifEmpty { BULLET_NONE }
                origBulletType = bulletType
            }
            initialised = true
        }
        when (uiState) {
            is Child2SubUiState.Saved -> navController.popBackStack()
            is Child2SubUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child2SubUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isDirty = initialised && (
        workRole != origWorkRole || companyName != origCompanyName ||
        subtitle != origSubtitle || workPeriod != origWorkPeriod ||
        accomplishments != origAccomplishments || bulletType != origBulletType
    )
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }
    var workRoleError by rememberSaveable { mutableStateOf(false) }
    var companyNameError by rememberSaveable { mutableStateOf(false) }

    fun attemptSave() {
        workRoleError = workRole.isBlank()
        companyNameError = companyName.isBlank()
        if (!workRoleError && !companyNameError) {
            viewModel.save(workRole, companyName, subtitle, workPeriod, accomplishments, bulletType)
        }
    }

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (item != null) "Edit Entry" else "New Entry") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showUnsavedDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { attemptSave() }) {
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
                                workRole = ""; companyName = ""; subtitle = ""
                                workPeriod = ""; accomplishments = ""; bulletType = BULLET_NONE
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
        bottomBar = { LargeBannerAdBottomBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState is Child2SubUiState.Loading) {
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
                value = workRole,
                onValueChange = { workRole = it; if (it.isNotBlank()) workRoleError = false },
                label = { Text("Work Role / Position") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = workRoleError,
                supportingText = if (workRoleError) {{ Text("Work Role is required") }} else null
            )
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it; if (it.isNotBlank()) companyNameError = false },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = companyNameError,
                supportingText = if (companyNameError) {{ Text("Company Name is required") }} else null
            )
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("Subtitle (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = workPeriod,
                onValueChange = { workPeriod = it },
                label = { Text("Work Period") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = accomplishments,
                onValueChange = { accomplishments = it },
                label = { Text("Accomplishments / Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8, maxLines = 20
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it },
                primaryText = "Show accomplishments as bullet points",
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
                    showUnsavedDialog = false
                    attemptSave()
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
@Preview(showBackground = true, name = "Child2 Sub - New Entry")
@Composable
private fun SectionChild2SubNewPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("New Entry") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
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
                    label = { Text("Work Role / Position") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Subtitle (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Work Period") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Accomplishments / Description") },
                    modifier = Modifier.fillMaxWidth(), minLines = 8, maxLines = 20)
                BulletTypeDropdown(
                    selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show accomplishments as bullet points",
                    hintText = "(Here new line will be considered as bullet points)"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Child2 Sub - Edit Entry")
@Composable
private fun SectionChild2SubEditPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Entry") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
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
                OutlinedTextField(value = "Senior Android Developer", onValueChange = {},
                    label = { Text("Work Role / Position") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "Google", onValueChange = {},
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "Android Platform Team", onValueChange = {},
                    label = { Text("Subtitle (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "Jan 2021 – Present", onValueChange = {},
                    label = { Text("Work Period") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = "Led development of core Android features\nImproved app performance by 40%\nMentored junior developers",
                    onValueChange = {},
                    label = { Text("Accomplishments / Description") },
                    modifier = Modifier.fillMaxWidth(), minLines = 8, maxLines = 20)
                BulletTypeDropdown(
                    selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show accomplishments as bullet points",
                    hintText = "(Here new line will be considered as bullet points)"
                )
            }
        }
    }
}
