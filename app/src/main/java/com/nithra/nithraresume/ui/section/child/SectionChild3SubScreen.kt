package com.nithra.nithraresume.ui.section.child

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild3SubScreen(
    navController: NavController,
    viewModel: SectionChild3SubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item by viewModel.item.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var studyDegree by rememberSaveable { mutableStateOf("") }
    var schoolName  by rememberSaveable { mutableStateOf("") }
    var subtitle    by rememberSaveable { mutableStateOf("") }
    var studyPeriod by rememberSaveable { mutableStateOf("") }
    var concentrates by rememberSaveable { mutableStateOf("") }
    var bulletType  by rememberSaveable { mutableStateOf(BULLET_NONE) }
    var initialised by rememberSaveable { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    var origStudyDegree by rememberSaveable { mutableStateOf("") }
    var origSchoolName  by rememberSaveable { mutableStateOf("") }
    var origSubtitle    by rememberSaveable { mutableStateOf("") }
    var origStudyPeriod by rememberSaveable { mutableStateOf("") }
    var origConcentrates by rememberSaveable { mutableStateOf("") }
    var origBulletType  by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var studyDegreeError by rememberSaveable { mutableStateOf(false) }
    var schoolNameError  by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(item) {
        if (!initialised && item != null) {
            studyDegree = item!!.studyDegree
            schoolName  = item!!.schoolName
            subtitle    = item!!.subtitle
            studyPeriod = item!!.studyPeriod
            concentrates = item!!.concentrates
            bulletType  = item!!.concentratesBulletType.ifEmpty { BULLET_NONE }
            origStudyDegree = studyDegree
            origSchoolName  = schoolName
            origSubtitle    = subtitle
            origStudyPeriod = studyPeriod
            origConcentrates = concentrates
            origBulletType  = bulletType
            initialised = true
        } else if (!initialised && uiState is Child3SubUiState.Ready) {
            initialised = true
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Child3SubUiState.Saved -> navController.popBackStack()
            is Child3SubUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child3SubUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isDirty = initialised && (
        studyDegree != origStudyDegree ||
        schoolName  != origSchoolName  ||
        subtitle    != origSubtitle    ||
        studyPeriod != origStudyPeriod ||
        concentrates != origConcentrates ||
        bulletType  != origBulletType
    )
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    fun attemptSave() {
        studyDegreeError = studyDegree.isBlank()
        schoolNameError  = schoolName.isBlank()
        if (!studyDegreeError && !schoolNameError) {
            viewModel.save(studyDegree, schoolName, subtitle, studyPeriod, concentrates, bulletType)
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
                                studyDegree = ""; schoolName = ""; subtitle = ""
                                studyPeriod = ""; concentrates = ""; bulletType = BULLET_NONE
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = studyDegree,
                onValueChange = {
                    studyDegree = it
                    if (it.isNotBlank()) studyDegreeError = false
                },
                label = { Text("Study Degree / Qualification") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = studyDegreeError,
                supportingText = if (studyDegreeError) {
                    { Text("Study Degree is required") }
                } else null
            )
            OutlinedTextField(
                value = schoolName,
                onValueChange = {
                    schoolName = it
                    if (it.isNotBlank()) schoolNameError = false
                },
                label = { Text("School / University Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = schoolNameError,
                supportingText = if (schoolNameError) {
                    { Text("School / University Name is required") }
                } else null
            )
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("Subtitle (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = studyPeriod,
                onValueChange = { studyPeriod = it },
                label = { Text("Study Period") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = concentrates,
                onValueChange = { concentrates = it },
                label = { Text("Concentrates / Subjects") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3, maxLines = 8
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it }
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
@Preview(showBackground = true, name = "Child3Sub - New Entry")
@Composable
private fun SectionChild3SubNewPreview() {
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Study Degree / Qualification") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("School / University Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Subtitle (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Study Period") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Child3Sub - Edit Entry")
@Composable
private fun SectionChild3SubEditPreview() {
    val previewItem = SectionChild3(
        id = 1, sectionHeadAddedId = 1, indexPosition = 0,
        studyDegree = "B.Sc. Computer Science",
        schoolName = "MIT",
        subtitle = "Dean's List",
        studyPeriod = "2016 – 2020",
        concentrates = "Algorithms, Data Structures, Machine Learning",
        concentratesBulletType = BULLET_NONE
    )
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = previewItem.studyDegree, onValueChange = {},
                    label = { Text("Study Degree / Qualification") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.schoolName, onValueChange = {},
                    label = { Text("School / University Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.subtitle, onValueChange = {},
                    label = { Text("Subtitle (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.studyPeriod, onValueChange = {},
                    label = { Text("Study Period") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.concentrates, onValueChange = {},
                    label = { Text("Concentrates / Subjects") },
                    modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 8)
            }
        }
    }
}
