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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nithra.nithraresume.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.ui.preview.AppPreview

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

    LaunchedEffect(uiState) {
        if (!initialised && uiState is Child3SubUiState.Ready) {
            item?.let {
                studyDegree = it.studyDegree;   origStudyDegree = studyDegree
                schoolName  = it.schoolName;    origSchoolName  = schoolName
                subtitle    = it.subtitle;      origSubtitle    = subtitle
                studyPeriod = it.studyPeriod;   origStudyPeriod = studyPeriod
                concentrates = it.concentrates; origConcentrates = concentrates
                bulletType  = it.concentratesBulletType.ifEmpty { BULLET_NONE }
                origBulletType = bulletType
            }
            initialised = true
        }
        when (uiState) {
            is Child3SubUiState.Saved -> navController.popBackStack()
            is Child3SubUiState.Error -> {
                val msg = (uiState as Child3SubUiState.Error).message
                viewModel.resetState()
                snackbarHostState.showSnackbar(msg)
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
                title = { Text(if (item != null) stringResource(R.string.title_edit_entry) else stringResource(R.string.title_new_entry)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showUnsavedDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { attemptSave() }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.cd_save),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_clear_all)) },
                            onClick = {
                                showOverflowMenu = false
                                studyDegree = ""; schoolName = ""; subtitle = ""
                                studyPeriod = ""; concentrates = ""; bulletType = BULLET_NONE
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
        if (uiState is Child3SubUiState.Loading) {
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
                value = studyDegree,
                onValueChange = {
                    studyDegree = it
                    if (it.isNotBlank()) studyDegreeError = false
                },
                label = { Text(stringResource(R.string.label_study_degree)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = studyDegreeError,
                supportingText = if (studyDegreeError) {
                    { Text(stringResource(R.string.error_study_degree_required)) }
                } else null
            )
            OutlinedTextField(
                value = schoolName,
                onValueChange = {
                    schoolName = it
                    if (it.isNotBlank()) schoolNameError = false
                },
                label = { Text(stringResource(R.string.label_school_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = schoolNameError,
                supportingText = if (schoolNameError) {
                    { Text(stringResource(R.string.error_school_name_required)) }
                } else null
            )
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text(stringResource(R.string.label_subtitle)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = studyPeriod,
                onValueChange = { studyPeriod = it },
                label = { Text(stringResource(R.string.label_study_period)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = concentrates,
                onValueChange = { concentrates = it },
                label = { Text(stringResource(R.string.label_concentrates)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8, maxLines = 20
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it },
                primaryText = "Show concentration as bullet points",
                hintText = "(Here new line will be considered as bullet points)"
            )
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.dialog_title_unsaved_changes)) },
            text = { Text(stringResource(R.string.msg_unsaved_changes)) },
            confirmButton = {
                Button(onClick = {
                    showUnsavedDialog = false
                    attemptSave()
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showUnsavedDialog = false }) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        navController.popBackStack()
                    }) { Text(stringResource(R.string.btn_discard)) }
                }
            }
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
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
                    label = { Text("Study Degree / Qualification") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("School / University Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Subtitle") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Study Period") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Concentrates / Subjects") },
                    modifier = Modifier.fillMaxWidth(), minLines = 8, maxLines = 20)
                BulletTypeDropdown(
                    selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show concentration as bullet points",
                    hintText = "(Here new line will be considered as bullet points)"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
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
                OutlinedTextField(value = previewItem.studyDegree, onValueChange = {},
                    label = { Text("Study Degree / Qualification") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.schoolName, onValueChange = {},
                    label = { Text("School / University Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.subtitle, onValueChange = {},
                    label = { Text("Subtitle") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.studyPeriod, onValueChange = {},
                    label = { Text("Study Period") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = previewItem.concentrates, onValueChange = {},
                    label = { Text("Concentrates / Subjects") },
                    modifier = Modifier.fillMaxWidth(), minLines = 8, maxLines = 20)
                BulletTypeDropdown(
                    selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show concentration as bullet points",
                    hintText = "(Here new line will be considered as bullet points)"
                )
            }
        }
    }
}
