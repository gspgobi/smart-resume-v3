package com.nithra.nithraresume.ui.section.child

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.nithra.nithraresume.ui.common.BulletTypeDropdown
import com.nithra.nithraresume.ui.common.DateFormatPickerDialog
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.DateTimeUtils
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

    var title by rememberSaveable { mutableStateOf("") }
    var declarationContent by rememberSaveable { mutableStateOf("") }
    var bulletType by rememberSaveable { mutableStateOf(BULLET_NONE) }
    var date by rememberSaveable { mutableStateOf("") }
    var dateDateFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var place by rememberSaveable { mutableStateOf("") }
    var initialised by rememberSaveable { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sha, child4) {
        if (!initialised && sha != null) {
            title = sha!!.title
            val c4 = child4
            if (c4 != null) {
                declarationContent = c4.declarationContent
                bulletType = c4.declarationContentBulletType.ifEmpty { BULLET_NONE }
                date = c4.date
                dateDateFormat = c4.dateDateFormat.ifEmpty { ALL_DATE_FORMATS.first() }
                place = c4.place
            }
            initialised = true
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { "Declaration" }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.save(title, declarationContent, bulletType,
                            date, dateDateFormat, place)
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
                minLines = 3, maxLines = 10
            )
            BulletTypeDropdown(
                selected = bulletType,
                onSelected = { bulletType = it }
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDateFormatDialog = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date format")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("Place") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(
                            Screen.SectionChild4Signature.createRoute(viewModel.sectionHeadAddedId)
                        )
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Signature",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
}
