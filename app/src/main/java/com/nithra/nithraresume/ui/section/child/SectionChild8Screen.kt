package com.nithra.nithraresume.ui.section.child

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.ui.common.DateFormatPickerDialog
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS

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

    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var dateDateFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var address by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var initialised by rememberSaveable { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sha, child8) {
        if (!initialised && sha != null) {
            title = sha!!.title
            val c8 = child8
            if (c8 != null) {
                date = c8.date
                dateDateFormat = c8.dateDateFormat.ifEmpty { ALL_DATE_FORMATS.first() }
                address = c8.address
                content = c8.content
            }
            initialised = true
        }
    }

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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.save(title, date, dateDateFormat, address, content)
                    }) {
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
        },
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
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDateFormatDialog = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date format")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDateFormatDialog = true }
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Recipient") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5, maxLines = 20
            )
        }
    }

    if (showDateFormatDialog) {
        DateFormatPickerDialog(
            currentFormat = dateDateFormat,
            onConfirm = { fmt, dateStr ->
                dateDateFormat = fmt
                date = dateStr
                showDateFormatDialog = false
            },
            onDismiss = { showDateFormatDialog = false }
        )
    }
}
