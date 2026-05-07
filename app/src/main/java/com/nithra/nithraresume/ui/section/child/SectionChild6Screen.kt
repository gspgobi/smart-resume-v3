package com.nithra.nithraresume.ui.section.child

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.ui.navigation.Screen
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar
import com.nithra.nithraresume.utils.MAX_CHILD_ITEMS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild6Screen(
    navController: NavController,
    viewModel: SectionChild6ViewModel = hiltViewModel()
) {
    val sha by viewModel.sha.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val snackbar by viewModel.snackbar.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by rememberSaveable { mutableStateOf("") }
    var origTitle by rememberSaveable { mutableStateOf("") }
    var titleInitialised by rememberSaveable { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<SectionChild6?>(null) }

    LaunchedEffect(sha) {
        if (!titleInitialised && sha != null) {
            title = sha!!.title
            origTitle = title
            titleInitialised = true
        }
    }
    LaunchedEffect(snackbar) {
        snackbar?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }

    val isDirty = titleInitialised && title != origTitle
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isDirty) { showUnsavedDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { "Split Text" }) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showUnsavedDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveTitle(title) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save title",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            item {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Section Title") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true
                )
                HorizontalDivider()
            }

            item {
                Child6GroupHeader(
                    title = "Entries",
                    onEditClick = if (items.size > 1) {
                        { navController.navigate(Screen.ReorderChild.createRoute(viewModel.sectionHeadAddedId, 6)) }
                    } else null
                )
            }

            items(items.sortedBy { it.indexPosition }, key = { it.id }) { item ->
                Child6ListItem(
                    item = item,
                    onClick = {
                        navController.navigate(
                            Screen.SectionChild6Sub.createRoute(viewModel.sectionHeadAddedId, item.id)
                        )
                    },
                    onDelete = { deleteTarget = item }
                )
                HorizontalDivider()
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = viewModel.canAddItem(items.size),
                            onClick = {
                                navController.navigate(
                                    Screen.SectionChild6Sub.createRoute(viewModel.sectionHeadAddedId, -1)
                                )
                            }
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AddBox, contentDescription = null,
                        tint = if (viewModel.canAddItem(items.size)) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp))
                    Text(
                        text = if (viewModel.canAddItem(items.size)) "Add New Entry"
                               else "Maximum $MAX_CHILD_ITEMS entries reached",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (viewModel.canAddItem(items.size)) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
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
                    viewModel.saveTitle(title)
                    navController.popBackStack()
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

    if (deleteTarget != null) {
        val target = deleteTarget!!
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Entry") },
            text = { Text("Delete \"${target.contentTitle.ifEmpty { "this entry" }}\"?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteItem(target, items); deleteTarget = null }) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun Child6ListItem(
    item: SectionChild6,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (item.contentTitle.isNotEmpty()) {
                Text(item.contentTitle, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)
            } else {
                Text("(no title)", style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.contentDetail.isNotEmpty()) {
                Text(item.contentDetail, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2)
            }
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = { menuExpanded = false; onDelete() }
                )
            }
        }
    }
}

@Composable
private fun Child6GroupHeader(
    title: String,
    onEditClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        if (onEditClick != null) {
            TextButton(
                onClick = onEditClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Edit", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewChild6Items = listOf(
    SectionChild6(id = 1, sectionHeadAddedId = 1, indexPosition = 0,
        contentTitle = "Languages", contentDetail = "English, Tamil, Hindi"),
    SectionChild6(id = 2, sectionHeadAddedId = 1, indexPosition = 1,
        contentTitle = "Frameworks", contentDetail = "Android, Spring Boot"),
    SectionChild6(id = 3, sectionHeadAddedId = 1, indexPosition = 2,
        contentTitle = "", contentDetail = ""),
)

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Section Child 6 - Loading")
@Composable
private fun SectionChild6LoadingPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Split Text") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Check, contentDescription = "Save title",
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
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Section Child 6 - Empty")
@Composable
private fun SectionChild6EmptyPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Split Text") },
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                item {
                    OutlinedTextField(
                        value = "Split Text", onValueChange = {},
                        label = { Text("Section Title") },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        singleLine = true
                    )
                    HorizontalDivider()
                }
                item { Child6GroupHeader(title = "Entries") }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                        Text("Add New Entry",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Section Child 6 - With Items")
@Composable
private fun SectionChild6FilledPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Split Text") },
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                item {
                    OutlinedTextField(
                        value = "Split Text", onValueChange = {},
                        label = { Text("Section Title") },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        singleLine = true
                    )
                    HorizontalDivider()
                }
                item { Child6GroupHeader(title = "Entries", onEditClick = {}) }
                items(previewChild6Items, key = { it.id }) { item ->
                    Child6ListItem(item = item, onClick = {}, onDelete = {})
                    HorizontalDivider()
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                        Text("Add New Entry",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Child6 List Item")
@Composable
private fun Child6ListItemPreview() {
    SmartResumeTheme {
        Column {
            Child6ListItem(item = previewChild6Items[0], onClick = {}, onDelete = {})
            HorizontalDivider()
            Child6ListItem(item = previewChild6Items[2], onClick = {}, onDelete = {})
        }
    }
}

@Preview(showBackground = true, name = "Child6 Group Header - No Edit")
@Composable
private fun Child6GroupHeaderNoEditPreview() {
    SmartResumeTheme { Child6GroupHeader(title = "Entries") }
}

@Preview(showBackground = true, name = "Child6 Group Header - With Edit")
@Composable
private fun Child6GroupHeaderWithEditPreview() {
    SmartResumeTheme { Child6GroupHeader(title = "Entries", onEditClick = {}) }
}
