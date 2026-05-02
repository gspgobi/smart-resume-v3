package com.nithra.nithraresume.ui.section.child

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Image
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nithra.nithraresume.ui.common.DateFormatPickerDialog
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS
import com.nithra.nithraresume.utils.ALL_GENDERS
import com.nithra.nithraresume.utils.DateTimeUtils
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild1Screen(
    navController: NavController,
    viewModel: SectionChild1ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sha by viewModel.sha.collectAsStateWithLifecycle()
    val child1 by viewModel.child1.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Form fields
    var title by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") }
    var dobFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var nationality by rememberSaveable { mutableStateOf("") }

    // Original snapshots for dirty detection
    var origTitle by rememberSaveable { mutableStateOf("") }
    var origName by rememberSaveable { mutableStateOf("") }
    var origAddress by rememberSaveable { mutableStateOf("") }
    var origEmail by rememberSaveable { mutableStateOf("") }
    var origPhone by rememberSaveable { mutableStateOf("") }
    var origGender by rememberSaveable { mutableStateOf("") }
    var origDob by rememberSaveable { mutableStateOf("") }
    var origDobFormat by rememberSaveable { mutableStateOf(ALL_DATE_FORMATS.first()) }
    var origNationality by rememberSaveable { mutableStateOf("") }

    var fieldsInitialised by rememberSaveable { mutableStateOf(false) }

    // Populate fields once both sha and child1 are loaded to avoid a race
    // where sha arrives first (fieldsInitialised = true) before child1 data is ready.
    LaunchedEffect(sha, child1) {
        if (!fieldsInitialised && sha != null && child1 != null) {
            title = sha!!.title; origTitle = title
            child1!!.let { c ->
                name = c.name; origName = name
                address = c.address; origAddress = address
                email = c.email; origEmail = email
                phone = c.phone; origPhone = phone
                gender = c.gender; origGender = gender
                dob = c.dob; origDob = dob
                if (c.dobDateFormat.isNotEmpty()) dobFormat = c.dobDateFormat
                origDobFormat = dobFormat
                nationality = c.nationality; origNationality = nationality
            }
            fieldsInitialised = true
        }
    }

    val isDirty = fieldsInitialised && (
        title != origTitle || name != origName || address != origAddress ||
        email != origEmail || phone != origPhone || gender != origGender ||
        dob != origDob || dobFormat != origDobFormat || nationality != origNationality
    )
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Child1UiState.Saved -> {
                navController.popBackStack()
            }
            is Child1UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as Child1UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.saveImage(it) }
    }

    // Date picker dialog state
    var showDateDialog by rememberSaveable { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { "Contact Information" }) },
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
                        viewModel.save(title, name, address, email, phone,
                            gender, dob, dobFormat, nationality)
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
                                name = ""; address = ""; email = ""; phone = ""
                                gender = ""; dob = ""; nationality = ""
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
            // Section title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Section Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            SectionDivider("Contact Details")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            SectionDivider("Gender (optional)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                ALL_GENDERS.forEach { g ->
                    RadioButton(
                        selected = gender == g,
                        onClick = { gender = if (gender == g) "" else g }
                    )
                    Text(g, modifier = Modifier.padding(end = 16.dp))
                }
                if (gender.isNotEmpty()) {
                    TextButton(onClick = { gender = "" }) { Text("Clear") }
                }
            }

            SectionDivider("Date of Birth (optional)")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                IconButton(onClick = { showDateDialog = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }

            OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationality (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                )
            )

            SectionDivider("Profile Photo (optional)")
            UserImageSection(
                imagePath = child1?.userImagePath ?: "",
                onBrowseClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onDeleteClick = { viewModel.deleteImage() }
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // Unsaved changes dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Save before leaving?") },
            confirmButton = {
                Button(onClick = {
                    showUnsavedDialog = false
                    focusManager.clearFocus()
                    viewModel.save(title, name, address, email, phone, gender, dob, dobFormat, nationality)
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

    // Date picker dialog
    if (showDateDialog) {
        DateFormatPickerDialog(
            currentFormat = dobFormat,
            currentDateMs = DateTimeUtils.parseDateToUtcMillis(dob, dobFormat),
            onConfirm = { fmt, dateStr ->
                dobFormat = fmt
                dob = dateStr
                showDateDialog = false
            },
            onDismiss = { showDateDialog = false }
        )
    }
}

// ── Composable helpers ────────────────────────────────────────────────────────

@Composable
private fun SectionDivider(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun UserImageSection(
    imagePath: String,
    onBrowseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val imageFile = remember(imagePath) { if (imagePath.isNotEmpty()) File(imagePath) else null }
    val hasImage = imageFile?.exists() == true

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = imageFile,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBrowseClick) {
                Text(if (hasImage) "Change Photo" else "Browse Photo")
            }
            if (hasImage) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}
