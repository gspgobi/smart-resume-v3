package com.nithra.nithraresume.ui.generate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateResumeScreen(
    navController: NavController,
    viewModel: GenerateResumeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentFormat by viewModel.currentFormat.collectAsStateWithLifecycle()
    val sc1 by viewModel.sc1.collectAsStateWithLifecycle()
    val sc4 by viewModel.sc4.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var fileName by rememberSaveable { mutableStateOf("") }
    var fileNameInitialised by rememberSaveable { mutableStateOf(false) }

    val includeUserImage = sc1?.isUserImageEnable ?: false
    val includeSignature = sc4?.isSignatureImageEnable ?: false

    var showOverwriteDialog by rememberSaveable { mutableStateOf(false) }
    var pendingFileName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(profile) {
        if (!fileNameInitialised && profile != null) {
            fileName = profile!!.resumeFileName?.ifEmpty { profile!!.name } ?: profile!!.name
            fileNameInitialised = true
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GenerateResumeUiState.Done -> {
                navController.navigate(Screen.ViewShare.createRoute(viewModel.profileId, justGenerated = true))
                viewModel.resetState()
            }
            is GenerateResumeUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isLoading    = uiState is GenerateResumeUiState.Loading
    val isGenerating = uiState is GenerateResumeUiState.Generating

    val hasUserImageSet   = sc1 != null && sc1!!.userImagePath.isNotEmpty()
    val hasSignatureSet   = sc4 != null && sc4!!.signatureImagePath.isNotEmpty()
    val showGenerateWith  = sc1 != null || sc4 != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Resume") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        enabled = !isGenerating
                    ) {
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
        bottomBar = { LargeBannerAdBottomBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            isGenerating -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(56.dp))
                        Text("Generating your resume…", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── File Name ─────────────────────────────────────────────
                    Text(
                        "File Name",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name") },
                        supportingText = { Text("Do not include .pdf extension") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // ── Resume Settings ───────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "Resume Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FilledTonalButton(
                            onClick = {
                                navController.navigate(
                                    Screen.ResumeFormat.createRoute(viewModel.profileId)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Edit")
                        }
                    }
                    profile?.let { p ->
                        Card(
                            onClick = {
                                navController.navigate(
                                    Screen.ResumeFormat.createRoute(viewModel.profileId)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column {
                                SettingsInfoRow(
                                    label = "Resume Format",
                                    value = currentFormat?.title ?: "",
                                    showChevron = true
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsInfoRow("Font Style", p.fontStyle.replace(Regex("\\.TTF$", RegexOption.IGNORE_CASE), ""))
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsInfoRow("Font Size", "${p.fontSize} pt")
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsInfoRow(
                                    "Background",
                                    p.backgroundColor.ifEmpty { "White" }
                                )
                            }
                        }
                    }

                    // ── Generate Resume With ──────────────────────────────────
                    if (showGenerateWith) {
                        Text(
                            "Generate Resume With",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column {
                                if (sc1 != null) {
                                    GenerateWithRow(
                                        label = "User Photo",
                                        hint = if (hasUserImageSet) "Photo added"
                                               else "No photo added — add one in Contact Information section",
                                        checked = includeUserImage,
                                        enabled = hasUserImageSet,
                                        onCheckedChange = { viewModel.setIncludeUserImage(it) }
                                    )
                                }
                                if (sc1 != null && sc4 != null) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                                if (sc4 != null) {
                                    GenerateWithRow(
                                        label = "Signature",
                                        hint = if (hasSignatureSet) "Signature added"
                                               else "No signature added — add one in Declaration section",
                                        checked = includeSignature,
                                        enabled = hasSignatureSet,
                                        onCheckedChange = { viewModel.setIncludeSignature(it) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Generate button ───────────────────────────────────────
                    Button(
                        onClick = {
                            val trimmed = fileName.trim()
                            if (trimmed.isNotEmpty()) {
                                if (viewModel.fileExists(trimmed)) {
                                    pendingFileName = trimmed
                                    showOverwriteDialog = true
                                } else {
                                    viewModel.generate(trimmed)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = fileName.isNotBlank()
                    ) {
                        Text("Generate Resume", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text("File Already Exists") },
            text = {
                Text("A resume named \"$pendingFileName.pdf\" already exists. Do you want to overwrite it?")
            },
            confirmButton = {
                Button(onClick = {
                    showOverwriteDialog = false
                    viewModel.generate(pendingFileName)
                }) { Text("Overwrite") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showOverwriteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GenerateWithRow(
    label: String,
    hint: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (showChevron) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "GenerateResume - Generating")
@Composable
private fun GenerateResumeGeneratingPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Generate Resume") },
                    navigationIcon = {
                        IconButton(onClick = {}, enabled = false) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(56.dp))
                    Text("Generating your resume…", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "GenerateResume - Ready")
@Composable
private fun GenerateResumeReadyPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Generate Resume") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("File Name", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = "John_Doe_Resume", onValueChange = {},
                    label = { Text("File Name") },
                    supportingText = { Text("Do not include .pdf extension") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Resume Settings", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    FilledTonalButton(
                        onClick = {},
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) { Text("Edit", style = MaterialTheme.typography.labelSmall) }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        SettingsInfoRow("Resume Format", "Classic", showChevron = true)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow("Font Style", "Roboto")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow("Font Size", "11 pt")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow("Background", "White")
                    }
                }
                Button(
                    onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Generate Resume", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "GenerateResume - Ready with Photo & Signature")
@Composable
private fun GenerateResumeReadyWithOptionsPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Generate Resume") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("File Name", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = "John_Doe_Resume", onValueChange = {},
                    label = { Text("File Name") },
                    supportingText = { Text("Do not include .pdf extension") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Resume Settings", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    FilledTonalButton(
                        onClick = {},
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) { Text("Edit", style = MaterialTheme.typography.labelSmall) }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        SettingsInfoRow("Resume Format", "Modern", showChevron = true)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow("Font Style", "Lato")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow("Font Size", "12 pt")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoRow("Background", "Light Blue")
                    }
                }
                Text("Generate Resume With", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        GenerateWithRow(
                            label = "User Photo",
                            hint = "Photo added",
                            checked = true, enabled = true, onCheckedChange = {}
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        GenerateWithRow(
                            label = "Signature",
                            hint = "No signature added — add one in Declaration section",
                            checked = false, enabled = false, onCheckedChange = {}
                        )
                    }
                }
                Button(
                    onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Generate Resume", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
