package com.nithra.nithraresume.ui.sample

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.io.File
import com.nithra.nithraresume.ui.preview.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleResumesScreen(
    navController: NavController,
    viewModel: SampleResumesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // expand/collapse state per group index
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }

    var confirmAddId by remember { mutableStateOf<Int?>(null) }

    // Handle side-effect states
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SampleResumesUiState.Added -> {
                navController.popBackStack()
            }
            is SampleResumesUiState.PreviewReady -> {
                openPdfFile(context, state.file)
                viewModel.onPreviewHandled()
            }
            is SampleResumesUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.onErrorHandled()
            }
            else -> Unit
        }
    }

    val groups = when (val s = uiState) {
        is SampleResumesUiState.Ready      -> s.groups
        is SampleResumesUiState.Adding     -> emptyList()
        is SampleResumesUiState.Added      -> emptyList()
        is SampleResumesUiState.PreviewReady -> s.groups
        is SampleResumesUiState.Error      -> s.groups
        else                               -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sample Resumes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        when (uiState) {
            is SampleResumesUiState.Loading,
            is SampleResumesUiState.Adding -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    groups.forEachIndexed { index, group ->
                        val isExpanded = expanded[index] ?: false

                        item(key = "group_$index") {
                            GroupHeader(
                                title = group.title,
                                isExpanded = isExpanded,
                                itemCount = group.items.size,
                                onToggle = { expanded[index] = !isExpanded }
                            )
                            HorizontalDivider()
                        }

                        if (isExpanded) {
                            items(group.items, key = { "item_${it.sampleProfileId}" }) { item ->
                                SampleResumeItem(
                                    name = item.name,
                                    hasPreview = item.hasPreview,
                                    onAdd = { confirmAddId = item.sampleProfileId },
                                    onPreview = { viewModel.openPreview(item.sampleProfileId) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm add dialog
    confirmAddId?.let { sampleId ->
        AlertDialog(
            onDismissRequest = { confirmAddId = null },
            title = { Text("Add sample profile?") },
            text = { Text("This will create a new profile pre-filled with sample data. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addSampleProfile(sampleId)
                    confirmAddId = null
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { confirmAddId = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GroupHeader(
    title: String,
    isExpanded: Boolean,
    itemCount: Int,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (itemCount > 0) {
            Text(
                text = "$itemCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SampleResumeItem(
    name: String,
    hasPreview: Boolean,
    onAdd: () -> Unit,
    onPreview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (hasPreview) {
            IconButton(
                onClick = onPreview,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Preview",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        IconButton(
            onClick = onAdd,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.AddBox,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun openPdfFile(context: android.content.Context, file: File) {
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@AppPreview
@Composable
private fun GroupHeaderExpandedPreview() {
    SmartResumeTheme {
        GroupHeader(title = "Engineering", isExpanded = true, itemCount = 5, onToggle = {})
    }
}

@AppPreview
@Composable
private fun GroupHeaderCollapsedPreview() {
    SmartResumeTheme {
        GroupHeader(title = "Management", isExpanded = false, itemCount = 3, onToggle = {})
    }
}

@AppPreview
@Composable
private fun SampleResumeItemWithPreviewPreview() {
    SmartResumeTheme {
        SampleResumeItem(name = "Software Engineer Resume", hasPreview = true, onAdd = {}, onPreview = {})
    }
}

@AppPreview
@Composable
private fun SampleResumeItemNoPreviewPreview() {
    SmartResumeTheme {
        SampleResumeItem(name = "AI / ML Engineer", hasPreview = false, onAdd = {}, onPreview = {})
    }
}
