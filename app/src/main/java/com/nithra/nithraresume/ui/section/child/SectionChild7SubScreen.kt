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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild7SubScreen(
    navController: NavController,
    viewModel: SectionChild7SubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item by viewModel.item.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var contentTitle by rememberSaveable { mutableStateOf("") }
    var contentSubtitle by rememberSaveable { mutableStateOf("") }
    var contentDetail by rememberSaveable { mutableStateOf("") }
    var bulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var origContentTitle by rememberSaveable { mutableStateOf("") }
    var origContentSubtitle by rememberSaveable { mutableStateOf("") }
    var origContentDetail by rememberSaveable { mutableStateOf("") }
    var origBulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }

    var initialised by rememberSaveable { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (!initialised && uiState is Child7SubUiState.Ready) {
            item?.let {
                contentTitle = it.contentTitle; origContentTitle = contentTitle
                contentSubtitle = it.contentSubtitle; origContentSubtitle = contentSubtitle
                contentDetail = it.contentDetail; origContentDetail = contentDetail
                bulletType = it.contentDetailBulletType.ifEmpty { BULLET_NONE }; origBulletType = bulletType
            }
            initialised = true
        }
        when (uiState) {
            is Child7SubUiState.Saved -> navController.popBackStack()
            is Child7SubUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child7SubUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isDirty = initialised && (
        contentTitle != origContentTitle || contentSubtitle != origContentSubtitle ||
        contentDetail != origContentDetail || bulletType != origBulletType
    )

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
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        viewModel.save(contentTitle, contentSubtitle, contentDetail, bulletType)
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
                                contentTitle = ""; contentSubtitle = ""
                                contentDetail = ""; bulletType = BULLET_NONE
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
        if (uiState is Child7SubUiState.Loading) {
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
                value = contentTitle,
                onValueChange = { contentTitle = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = contentSubtitle,
                onValueChange = { contentSubtitle = it },
                label = { Text("Subtitle (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = contentDetail,
                onValueChange = { contentDetail = it },
                label = { Text("Detail") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8, maxLines = 20
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it },
                primaryText = "Show detail as bullet points",
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
                    focusManager.clearFocus()
                    viewModel.save(contentTitle, contentSubtitle, contentDetail, bulletType)
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
