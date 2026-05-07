package com.nithra.nithraresume.ui.section.child

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.ui.common.DateFormatPickerDialog
import com.nithra.nithraresume.ui.common.SectionDivider
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.DateTimeUtils
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild4Screen(
    navController: NavController,
    viewModel: SectionChild4ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sha by viewModel.sha.collectAsStateWithLifecycle()
    val child4 by viewModel.child4.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var title by rememberSaveable { mutableStateOf("") }
    var declarationContent by rememberSaveable { mutableStateOf("") }
    var bulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }
    var date by rememberSaveable { mutableStateOf("") }
    var dateDateFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var place by rememberSaveable { mutableStateOf("") }

    var origTitle by rememberSaveable { mutableStateOf("") }
    var origDeclarationContent by rememberSaveable { mutableStateOf("") }
    var origBulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }
    var origDate by rememberSaveable { mutableStateOf("") }
    var origDateFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var origPlace by rememberSaveable { mutableStateOf("") }

    var initialised by rememberSaveable { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteSigDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
        if (uri != null) viewModel.saveSignatureFromUri(uri)
    }

    LaunchedEffect(sha, child4) {
        if (!initialised && sha != null) {
            title = sha!!.title; origTitle = title
            val c4 = child4
            if (c4 != null) {
                declarationContent = c4.declarationContent; origDeclarationContent = declarationContent
                bulletType = c4.declarationContentBulletType.ifEmpty { BULLET_NONE }; origBulletType = bulletType
                date = c4.date; origDate = date
                dateDateFormat = c4.dateDateFormat.ifEmpty { ALL_DATE_FORMATS.first() }; origDateFormat = dateDateFormat
                place = c4.place; origPlace = place
            }
            initialised = true
        }
    }

    val isDirty = initialised && (
        title != origTitle || declarationContent != origDeclarationContent ||
        bulletType != origBulletType || date != origDate ||
        dateDateFormat != origDateFormat || place != origPlace
    )

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Child4UiState.Saved -> navController.popBackStack()
            is Child4UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child4UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val sigPath = child4?.signatureImagePath?.takeIf { it.isNotEmpty() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { "Declaration" }) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showUnsavedDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        viewModel.save(title, declarationContent, bulletType,
                            date, dateDateFormat, place)
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
                                declarationContent = ""; bulletType = BULLET_NONE
                                date = ""; place = ""
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Section Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = declarationContent,
                onValueChange = { declarationContent = it },
                label = { Text("Declaration Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8, maxLines = 20
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it },
                primaryText = "Show declaration as bullet points",
                hintText = "(Here new line will be considered as bullet points)"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { showDateFormatDialog = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("Place") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SectionDivider("Signature Image (optional)")

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (sigPath != null) {
                    AsyncImage(
                        model = sigPath,
                        contentDescription = "Signature preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                } else {
                    Text(
                        text = "No signature added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate(
                            Screen.SectionChild4Signature.createRoute(viewModel.sectionHeadAddedId)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("New Signature") }
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Browse Gallery") }
            }

            if (sigPath != null) {
                OutlinedButton(
                    onClick = { showDeleteSigDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete Signature") }
            }
        }
    }

    if (showDateFormatDialog) {
        DateFormatPickerDialog(
            currentFormat = dateDateFormat,
            currentDateMs = DateTimeUtils.parseDateToUtcMillis(date, dateDateFormat),
            onConfirm = { fmt, dateStr ->
                dateDateFormat = fmt
                date = dateStr
                showDateFormatDialog = false
            },
            onDismiss = { showDateFormatDialog = false }
        )
    }

    if (showDeleteSigDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSigDialog = false },
            title = { Text("Delete Signature") },
            text = { Text("Are you sure you want to delete the signature?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteSigDialog = false
                        viewModel.deleteSignatureImage()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSigDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Save before leaving?") },
            confirmButton = {
                Button(onClick = {
                    showUnsavedDialog = false
                    focusManager.clearFocus()
                    viewModel.save(title, declarationContent, bulletType, date, dateDateFormat, place)
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
@Preview(showBackground = true, name = "Section Child 4 - Empty")
@Composable
private fun SectionChild4EmptyPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Declaration") },
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
                OutlinedTextField(value = "Declaration", onValueChange = {},
                    label = { Text("Section Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Declaration Content") }, modifier = Modifier.fillMaxWidth(),
                    minLines = 8, maxLines = 20)
                BulletTypeDropdown(selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show declaration as bullet points",
                    hintText = "(Here new line will be considered as bullet points)")
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = "", onValueChange = {},
                        label = { Text("Date") }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Place") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SectionDivider("Signature(optional)")
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No signature added", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("New Signature") }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Browse Gallery") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Section Child 4 - With Signature")
@Composable
private fun SectionChild4WithSignaturePreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Declaration") },
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
                OutlinedTextField(value = "Declaration", onValueChange = {},
                    label = { Text("Section Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = "I hereby declare that all the information provided above is true and correct to the best of my knowledge.",
                    onValueChange = {},
                    label = { Text("Declaration Content") }, modifier = Modifier.fillMaxWidth(),
                    minLines = 8, maxLines = 20)
                BulletTypeDropdown(selected = BULLET_NONE, onSelected = {},
                    primaryText = "Show declaration as bullet points",
                    hintText = "(Here new line will be considered as bullet points)")
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = "01 Jan 2025", onValueChange = {},
                        label = { Text("Date") }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                OutlinedTextField(value = "Chennai", onValueChange = {},
                    label = { Text("Place") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SectionDivider("Signature(optional)")
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("~ Signature ~", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("New Signature") }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Browse Gallery") }
                }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete Signature") }
            }
        }
    }
}
