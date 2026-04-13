package com.nithra.nithraresume.ui.format

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.utils.ALL_BG_COLORS
import com.nithra.nithraresume.utils.ALL_FONT_STYLES
import com.nithra.nithraresume.utils.BG_COLOR_WHITE
import com.nithra.nithraresume.utils.FONT_SIZE_DEFAULT
import com.nithra.nithraresume.utils.FONT_SIZE_MAX
import com.nithra.nithraresume.utils.FONT_SIZE_MIN
import com.nithra.nithraresume.utils.FONT_TIMES_NEW_ROMAN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeFormatScreen(
    navController: NavController,
    viewModel: ResumeFormatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val formats by viewModel.formats.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedFormatId by rememberSaveable { mutableIntStateOf(0) }
    var selectedFontStyle by rememberSaveable { mutableStateOf(FONT_TIMES_NEW_ROMAN) }
    var selectedFontSize by rememberSaveable { mutableIntStateOf(FONT_SIZE_DEFAULT) }
    var selectedBgColor by rememberSaveable { mutableStateOf(BG_COLOR_WHITE) }
    var initialised by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!initialised && profile != null) {
            selectedFormatId = profile!!.resumeFormatBaseId
            selectedFontStyle = profile!!.fontStyle.ifEmpty { FONT_TIMES_NEW_ROMAN }
            selectedFontSize = profile!!.fontSize.takeIf { it in FONT_SIZE_MIN..FONT_SIZE_MAX }
                ?: FONT_SIZE_DEFAULT
            selectedBgColor = profile!!.backgroundColor.ifEmpty { BG_COLOR_WHITE }
            initialised = true
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ResumeFormatUiState.Saved -> navController.popBackStack()
            is ResumeFormatUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ResumeFormatUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Format") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.save(selectedFormatId, selectedFontStyle,
                            selectedFontSize, selectedBgColor)
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
        ) {
            // ── Template section ──────────────────────────────────────────────
            SectionHeader("Template")
            FormatGrid(
                formats = formats,
                selectedId = selectedFormatId,
                onSelect = { selectedFormatId = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Font section ──────────────────────────────────────────────────
            SectionHeader("Font")
            FontStyleDropdown(
                selected = selectedFontStyle,
                onSelected = { selectedFontStyle = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // ── Font Size section ─────────────────────────────────────────────
            SectionHeader("Font Size")
            FontSizeStepper(
                value = selectedFontSize,
                onDecrement = { if (selectedFontSize > FONT_SIZE_MIN) selectedFontSize-- },
                onIncrement = { if (selectedFontSize < FONT_SIZE_MAX) selectedFontSize++ },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // ── Background Color section ──────────────────────────────────────
            SectionHeader("Background Color")
            ALL_BG_COLORS.forEach { color ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedBgColor = color }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedBgColor == color,
                        onClick = { selectedBgColor = color }
                    )
                    Text(color, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun FormatGrid(
    formats: List<ResumeFormat>,
    selectedId: Int,
    onSelect: (Int) -> Unit
) {
    // Fixed-height grid (non-scrollable inside scroll column)
    val rows = (formats.size + 1) / 2
    val itemHeight = 80.dp
    val gridHeight = itemHeight * rows + 8.dp * (rows - 1)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(formats, key = { it.id }) { format ->
            FormatCard(
                format = format,
                isSelected = format.id == selectedId,
                onClick = { onSelect(format.id) }
            )
        }
    }
}

@Composable
private fun FormatCard(
    format: ResumeFormat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = format.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            if (format.description.isNotEmpty()) {
                Text(
                    text = format.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontStyleDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    // Display name strips the .TTF suffix
    val displayName = { s: String -> s.removeSuffix(".TTF").replace(".", " ") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayName(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text("Font") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ALL_FONT_STYLES.forEach { font ->
                DropdownMenuItem(
                    text = { Text(displayName(font)) },
                    onClick = { onSelected(font); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun FontSizeStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledIconButton(
            onClick = onDecrement,
            enabled = value > FONT_SIZE_MIN,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text("−", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        FilledIconButton(
            onClick = onIncrement,
            enabled = value < FONT_SIZE_MAX,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(
            text = "($FONT_SIZE_MIN – $FONT_SIZE_MAX)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
// update 117
