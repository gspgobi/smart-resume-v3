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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
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
import com.nithra.nithraresume.ui.common.DateFormatPickerDialog
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS
import com.nithra.nithraresume.utils.DateTimeUtils
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild8Screen(
    navController: NavController,
    viewModel: SectionChild8ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sha by viewModel.sha.collectAsStateWithLifecycle()
    val child8 by viewModel.child8.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var dateDateFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var address by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    var origTitle by rememberSaveable { mutableStateOf("") }
    var origDate by rememberSaveable { mutableStateOf("") }
    var origDateFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var origAddress by rememberSaveable { mutableStateOf("") }
    var origContent by rememberSaveable { mutableStateOf("") }

    var initialised by rememberSaveable { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sha, child8) {
        if (!initialised && sha != null) {
            title = sha!!.title; origTitle = title
            val c8 = child8
            if (c8 != null) {
                date = c8.date; origDate = date
                dateDateFormat = c8.dateDateFormat.ifEmpty { ALL_DATE_FORMATS.first() }; origDateFormat = dateDateFormat
                address = c8.address; origAddress = address
                content = c8.content; origContent = content
            }
            initialised = true
        }
    }

    val isDirty = initialised && (
        title != origTitle || date != origDate || dateDateFormat != origDateFormat ||
        address != origAddress || content != origContent
    )

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Child8UiState.Saved -> navController.popBackStack()
            is Child8UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child8UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { "Cover Letter" }) },
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
                        viewModel.save(title, date, dateDateFormat, address, content)
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
                                date = ""; address = ""; content = ""
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
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Recipient") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4, maxLines = 8
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8, maxLines = 20
            )
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

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Save before leaving?") },
            confirmButton = {
                Button(onClick = {
                    showUnsavedDialog = false
                    focusManager.clearFocus()
                    viewModel.save(title, date, dateDateFormat, address, content)
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
