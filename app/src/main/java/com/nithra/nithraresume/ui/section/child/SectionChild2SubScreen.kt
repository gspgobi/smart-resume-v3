package com.nithra.nithraresume.ui.section.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
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
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.utils.BULLET_NONE

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
    var initialised by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(item) {
        if (!initialised && item != null) {
            workRole = item!!.workRole
            companyName = item!!.companyName
            subtitle = item!!.subtitle
            workPeriod = item!!.workPeriod
            accomplishments = item!!.accomplishments
            bulletType = item!!.accomplishmentsBulletType.ifEmpty { BULLET_NONE }
            initialised = true
        } else if (!initialised && uiState is Child2SubUiState.Ready) {
            initialised = true // new item
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Child2SubUiState.Saved -> navController.popBackStack()
            is Child2SubUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child2SubUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (item != null) "Edit Entry" else "New Entry") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        workRole = ""; companyName = ""; subtitle = ""
                        workPeriod = ""; accomplishments = ""; bulletType = BULLET_NONE
                    }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear all",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = {
                        viewModel.save(workRole, companyName, subtitle,
                            workPeriod, accomplishments, bulletType)
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
                value = workRole,
                onValueChange = { workRole = it },
                label = { Text("Work Role / Position") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                minLines = 3, maxLines = 8
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it }
            )
        }
    }
}

// update 136
