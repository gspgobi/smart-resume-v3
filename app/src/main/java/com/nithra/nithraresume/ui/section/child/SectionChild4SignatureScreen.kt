package com.nithra.nithraresume.ui.section.child

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

enum class SignatureMode { Draw, Browse }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionChild4SignatureScreen(
    navController: NavController,
    viewModel: SectionChild4SignatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val child4 by viewModel.child4.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMode by remember { mutableStateOf(SignatureMode.Draw) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var clearCanvasKey by remember { mutableIntStateOf(0) }
    val captureController = rememberSignatureCaptureController()

    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
        if (uri != null) viewModel.saveSignatureImage(uri)
    }

    LaunchedEffect(uiState) {
        if (uiState is Child4SignatureUiState.Error) {
            snackbarHostState.showSnackbar((uiState as Child4SignatureUiState.Error).message)
        }
    }

    val sigPath = child4?.signatureImagePath?.takeIf { it.isNotEmpty() }
    val hasImage = sigPath != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signature") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Signature preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasImage && child4?.isSignatureImageEnable == true) {
                    AsyncImage(
                        model = sigPath,
                        contentDescription = "Signature",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(8.dp)
                    )
                } else {
                    Text(
                        text = if (hasImage) "Signature hidden" else "No signature added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Show in resume toggle (only when image exists)
            if (hasImage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show in resume", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = child4?.isSignatureImageEnable == true,
                        onCheckedChange = { viewModel.toggleSignatureEnable() }
                    )
                }
            }

            // Mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedMode = SignatureMode.Draw },
                    modifier = Modifier.weight(1f),
                    colors = if (selectedMode == SignatureMode.Draw)
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    else ButtonDefaults.outlinedButtonColors()
                ) { Text("Draw") }
                OutlinedButton(
                    onClick = { selectedMode = SignatureMode.Browse },
                    modifier = Modifier.weight(1f),
                    colors = if (selectedMode == SignatureMode.Browse)
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    else ButtonDefaults.outlinedButtonColors()
                ) { Text("Browse Gallery") }
            }

            // Draw mode panel
            if (selectedMode == SignatureMode.Draw) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    SignatureCanvas(
                        clearKey = clearCanvasKey,
                        captureController = captureController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { clearCanvasKey++ },
                        modifier = Modifier.weight(1f)
                    ) { Text("Clear") }
                    Button(
                        onClick = {
                            val bmp = captureController.captureBitmap()
                            if (bmp != null) viewModel.saveDrawnSignature(bmp)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = captureController.hasStrokes
                    ) { Text("Save Signature") }
                }
            }

            // Browse mode panel
            if (selectedMode == SignatureMode.Browse) {
                Button(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (hasImage) "Change Signature" else "Browse Gallery")
                }
            }

            // Delete button (only when image exists)
            if (hasImage) {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete Signature") }
            }

            Text(
                text = "Tip: Use a transparent-background PNG for best results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Signature") },
            text = { Text("Are you sure you want to delete the signature?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteSignatureImage()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
