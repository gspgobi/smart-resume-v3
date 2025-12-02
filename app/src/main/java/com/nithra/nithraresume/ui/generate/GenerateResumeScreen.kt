package com.nithra.nithraresume.ui.generate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController
import com.nithra.nithraresume.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateResumeScreen(
    navController: NavController,
    viewModel: GenerateResumeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var fileName by rememberSaveable { mutableStateOf("") }
    var fileNameInitialised by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!fileNameInitialised && profile != null) {
            fileName = profile!!.resumeFileName?.ifEmpty { profile!!.name } ?: profile!!.name
            fileNameInitialised = true
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GenerateResumeUiState.Done -> {
                navController.navigate(
                    Screen.ViewShare.createRoute(viewModel.profileId)
                ) {
                    popUpTo(Screen.GenerateResume.createRoute(viewModel.profileId)) {
                        inclusive = true
                    }
                }
            }
            is GenerateResumeUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isGenerating = uiState is GenerateResumeUiState.Generating

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Resume") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isGenerating) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isGenerating) {
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
                    Text("Generating your resume…",
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Your resume will be saved as a PDF in the app's storage folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    supportingText = { Text("Do not include .pdf extension") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                profile?.let { p ->
                    val formatId = p.resumeFormatBaseId
                    InfoRow("Format", "Format $formatId")
                    InfoRow("Font Size", "${p.fontSize} pt")
                    InfoRow("Background", p.backgroundColor)
                }

                Button(
                    onClick = {
                        val trimmed = fileName.trim()
                        if (trimmed.isNotEmpty()) viewModel.generate(trimmed)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fileName.isNotBlank()
                ) {
                    Text("Generate Resume")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
// update 119
