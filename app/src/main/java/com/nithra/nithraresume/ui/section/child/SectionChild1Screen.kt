package com.nithra.nithraresume.ui.section.child

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.nithra.nithraresume.ui.common.DateFormatPickerDialog
import com.nithra.nithraresume.ui.common.SectionDivider
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS
import com.nithra.nithraresume.utils.ALL_GENDERS
import com.nithra.nithraresume.utils.DateTimeUtils
import java.io.File
import com.nithra.nithraresume.ui.preview.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild1Screen(
    navController: NavController,
    viewModel: SectionChild1ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val child1 by viewModel.child1.collectAsStateWithLifecycle()
    val originalFormState by viewModel.originalFormState.collectAsStateWithLifecycle()
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

    var fieldsInitialised by rememberSaveable { mutableStateOf(false) }
    var titleError by rememberSaveable { mutableStateOf(false) }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var addressError by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf(false) }
    var phoneError by rememberSaveable { mutableStateOf(false) }

    // Populate fields once originalFormState is loaded from the DB.
    // fieldsInitialised (rememberSaveable) guards against re-overwriting on process death restore.
    LaunchedEffect(originalFormState) {
        if (fieldsInitialised) return@LaunchedEffect
        val orig = originalFormState ?: return@LaunchedEffect
        title = orig.title
        name = orig.name
        address = orig.address
        email = orig.email
        phone = orig.phone
        gender = orig.gender
        dob = orig.dob
        if (orig.dobFormat.isNotEmpty()) dobFormat = orig.dobFormat
        nationality = orig.nationality
        fieldsInitialised = true
    }

    val isDirty by remember {
        derivedStateOf {
            val orig = originalFormState ?: return@derivedStateOf false
            title != orig.title || name != orig.name || address != orig.address ||
            email != orig.email || phone != orig.phone || gender != orig.gender ||
            dob != orig.dob || dobFormat != orig.dobFormat || nationality != orig.nationality
        }
    }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Child1UiState.Saved -> {
                navController.popBackStack()
            }
            is Child1UiState.Error -> {
                val msg = (uiState as Child1UiState.Error).message
                viewModel.resetState()
                snackbarHostState.showSnackbar(msg)
            }
            else -> {}
        }
    }

    val cropImage = rememberLauncherForActivityResult(SmartResumeCropContract()) { result ->
        if (result.isSuccessful) result.uriContent?.let { viewModel.saveImage(it) }
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) cropImage.launch(
            CropImageContractOptions(
                uri = uri,
                cropImageOptions = CropImageOptions(
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    fixAspectRatio = true,
                    outputRequestWidth = 512,
                    outputRequestHeight = 512
                )
            )
        )
    }

    // Date picker dialog state
    var showDateDialog by rememberSaveable { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showUnsavedDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val tErr = title.isBlank(); val nErr = name.isBlank()
                        val aErr = address.isBlank(); val eErr = email.isBlank()
                        val pErr = phone.isBlank()
                        if (tErr || nErr || aErr || eErr || pErr) {
                            titleError = tErr; nameError = nErr; addressError = aErr
                            emailError = eErr; phoneError = pErr
                        } else {
                            focusManager.clearFocus()
                            viewModel.save(title, name, address, email, phone,
                                gender, dob, dobFormat, nationality)
                        }
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
        if (uiState is Child1UiState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Child1FormContent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                title = title, onTitleChange = { title = it; titleError = false }, titleError = titleError,
                name = name, onNameChange = { name = it; nameError = false }, nameError = nameError,
                address = address, onAddressChange = { address = it; addressError = false }, addressError = addressError,
                email = email, onEmailChange = { email = it; emailError = false }, emailError = emailError,
                phone = phone, onPhoneChange = { phone = it; phoneError = false }, phoneError = phoneError,
                gender = gender, onGenderChange = { gender = it },
                dob = dob, onDobChange = { dob = it },
                nationality = nationality, onNationalityChange = { nationality = it },
                imagePath = child1?.userImagePath ?: "",
                onPickPhotoClick = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onDeletePhotoClick = { viewModel.deleteImage() },
                onDateDialogOpen = { showDateDialog = true }
            )
        }
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onSave = {
                val tErr = title.isBlank(); val nErr = name.isBlank()
                val aErr = address.isBlank(); val eErr = email.isBlank()
                val pErr = phone.isBlank()
                showUnsavedDialog = false
                if (tErr || nErr || aErr || eErr || pErr) {
                    titleError = tErr; nameError = nErr; addressError = aErr
                    emailError = eErr; phoneError = pErr
                } else {
                    focusManager.clearFocus()
                    viewModel.save(title, name, address, email, phone, gender, dob, dobFormat, nationality)
                }
            },
            onDiscard = {
                showUnsavedDialog = false
                navController.popBackStack()
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
private fun Child1FormContent(
    modifier: Modifier = Modifier,
    title: String, onTitleChange: (String) -> Unit, titleError: Boolean,
    name: String, onNameChange: (String) -> Unit, nameError: Boolean,
    address: String, onAddressChange: (String) -> Unit, addressError: Boolean,
    email: String, onEmailChange: (String) -> Unit, emailError: Boolean,
    phone: String, onPhoneChange: (String) -> Unit, phoneError: Boolean,
    gender: String, onGenderChange: (String) -> Unit,
    dob: String, onDobChange: (String) -> Unit,
    nationality: String, onNationalityChange: (String) -> Unit,
    imagePath: String,
    onPickPhotoClick: () -> Unit,
    onDeletePhotoClick: () -> Unit,
    onDateDialogOpen: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Section Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError,
            supportingText = if (titleError) { { Text("Section title is required") } } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        SectionDivider("Contact Details")

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) { { Text("Name is required") } } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4, maxLines = 8,
            isError = addressError,
            supportingText = if (addressError) { { Text("Address is required") } } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) { { Text("Email is required") } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = phoneError,
            supportingText = if (phoneError) { { Text("Phone is required") } } else null,
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
                    onClick = { onGenderChange(if (gender == g) "" else g) }
                )
                Text(g, modifier = Modifier.padding(end = 16.dp))
            }
            if (gender.isNotEmpty()) {
                TextButton(onClick = { onGenderChange("") }) { Text("Clear") }
            }
        }

        SectionDivider("Date of Birth (optional)")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dob,
                onValueChange = onDobChange,
                label = { Text("Date of Birth") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            IconButton(onClick = onDateDialogOpen) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedTextField(
            value = nationality,
            onValueChange = onNationalityChange,
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
            imagePath = imagePath,
            onBrowseClick = onPickPhotoClick,
            onDeleteClick = onDeletePhotoClick
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unsaved Changes") },
        text = { Text("You have unsaved changes. Save before leaving?") },
        confirmButton = {
            Button(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(onClick = onDiscard) { Text("Discard") }
            }
        }
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp)
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
                Text(
                    text = "No user image found",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (hasImage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBrowseClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Change Photo")
                }
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
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
        } else {
            Button(
                onClick = onBrowseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Browse Photo")
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun SectionChild1EmptyPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Contact Information") },
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
                OutlinedTextField(value = "Contact Information", onValueChange = {},
                    label = { Text("Section Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                SectionDivider("Contact Details")
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 8)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                SectionDivider("Gender (optional)")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ALL_GENDERS.forEach { g ->
                        RadioButton(selected = false, onClick = {})
                        Text(g, modifier = Modifier.padding(end = 16.dp))
                    }
                }
                SectionDivider("Date of Birth (optional)")
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = "", onValueChange = {},
                        label = { Text("Date of Birth") }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                OutlinedTextField(value = "", onValueChange = {},
                    label = { Text("Nationality (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                SectionDivider("Profile Photo (optional)")
                UserImageSection(imagePath = "", onBrowseClick = {}, onDeleteClick = {})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@AppPreview
@Composable
private fun SectionChild1FilledPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Contact Information") },
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
                OutlinedTextField(value = "Contact Information", onValueChange = {},
                    label = { Text("Section Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                SectionDivider("Contact Details")
                OutlinedTextField(value = "John Doe", onValueChange = {},
                    label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "123 Main Street, Springfield", onValueChange = {},
                    label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 8)
                OutlinedTextField(value = "john.doe@email.com", onValueChange = {},
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = "+1 555 123 4567", onValueChange = {},
                    label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                SectionDivider("Gender (optional)")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ALL_GENDERS.forEach { g ->
                        RadioButton(selected = g == "Male", onClick = {})
                        Text(g, modifier = Modifier.padding(end = 16.dp))
                    }
                    TextButton(onClick = {}) { Text("Clear") }
                }
                SectionDivider("Date of Birth (optional)")
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = "15 Jan 1990", onValueChange = {},
                        label = { Text("Date of Birth") }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                OutlinedTextField(value = "American", onValueChange = {},
                    label = { Text("Nationality (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                SectionDivider("Profile Photo (optional)")
                UserImageSection(imagePath = "", onBrowseClick = {}, onDeleteClick = {})
            }
        }
    }
}

@AppPreview
@Composable
private fun SectionDividerPreview() {
    SmartResumeTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionDivider("Contact Details")
            SectionDivider("Gender (optional)")
            SectionDivider("Date of Birth (optional)")
            SectionDivider("Profile Photo (optional)")
        }
    }
}

@AppPreview
@Composable
private fun UserImageSectionNoImagePreview() {
    SmartResumeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            UserImageSection(imagePath = "", onBrowseClick = {}, onDeleteClick = {})
        }
    }
}

@AppPreview
@Composable
private fun UserImageSectionWithImagePreview() {
    SmartResumeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            UserImageSection(imagePath = "/tmp/profile.jpg", onBrowseClick = {}, onDeleteClick = {})
        }
    }
}
