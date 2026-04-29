package com.nithra.nithraresume.ui.format

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
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
    val context = LocalContext.current

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
            FormatList(
                formats = formats,
                selectedId = selectedFormatId,
                onSelect = { selectedFormatId = it },
                onPreview = { formatId -> openFormatPreview(context, formatId) }
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
            FontSizeSlider(
                value = selectedFontSize,
                onValueChange = { selectedFontSize = it },
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
private fun FormatList(
    formats: List<ResumeFormat>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onPreview: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        formats.forEachIndexed { index, format ->
            FormatListItem(
                format = format,
                isSelected = format.id == selectedId,
                onClick = { onSelect(format.id) },
                onPreviewClick = { onPreview(format.id) }
            )
            if (index < formats.lastIndex) HorizontalDivider()
        }
    }
}

@Composable
private fun FormatListItem(
    format: ResumeFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Check icon — always reserves space; transparent when not selected
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            modifier = Modifier.size(24.dp)
        )
        // Format title
        Text(
            text = format.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        // Preview icon — independent click, opens PDF sample
        Box(
            modifier = Modifier
                .clickable(onClick = onPreviewClick)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FindInPage,
                contentDescription = "Preview",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
    val displayName = { s: String -> s.removeSuffix(".TTF").replace(".", " ") }

    val context = LocalContext.current
    val fontFamilies = remember {
        ALL_FONT_STYLES.associateWith { font ->
            FontFamily(Typeface.createFromAsset(context.assets, "fonts/$font"))
        }
    }
    val selectedFontFamily = fontFamilies[selected] ?: FontFamily.Default

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
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = selectedFontFamily),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ALL_FONT_STYLES.forEach { font ->
                val fontFamily = fontFamilies[font] ?: FontFamily.Default
                DropdownMenuItem(
                    text = { Text(displayName(font), fontFamily = fontFamily) },
                    onClick = { onSelected(font); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSizeSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            FONT_SIZE_MIN.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = FONT_SIZE_MIN.toFloat()..FONT_SIZE_MAX.toFloat(),
            steps = FONT_SIZE_MAX - FONT_SIZE_MIN - 1,
            modifier = Modifier.weight(1f),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )
        Text(
            FONT_SIZE_MAX.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewFormats = listOf(
    ResumeFormat(id = 1, title = "Classic",      description = "", isDefault = true,  fontStyle = FONT_TIMES_NEW_ROMAN, fontSize = FONT_SIZE_DEFAULT, backgroundColor = BG_COLOR_WHITE),
    ResumeFormat(id = 2, title = "Modern",       description = "", isDefault = false, fontStyle = FONT_TIMES_NEW_ROMAN, fontSize = FONT_SIZE_DEFAULT, backgroundColor = BG_COLOR_WHITE),
    ResumeFormat(id = 3, title = "Professional", description = "", isDefault = false, fontStyle = FONT_TIMES_NEW_ROMAN, fontSize = FONT_SIZE_DEFAULT, backgroundColor = BG_COLOR_WHITE),
    ResumeFormat(id = 4, title = "Creative",     description = "", isDefault = false, fontStyle = FONT_TIMES_NEW_ROMAN, fontSize = FONT_SIZE_DEFAULT, backgroundColor = BG_COLOR_WHITE),
    ResumeFormat(id = 5, title = "Minimal",      description = "", isDefault = false, fontStyle = FONT_TIMES_NEW_ROMAN, fontSize = FONT_SIZE_DEFAULT, backgroundColor = BG_COLOR_WHITE),
    ResumeFormat(id = 6, title = "Executive",    description = "", isDefault = false, fontStyle = FONT_TIMES_NEW_ROMAN, fontSize = FONT_SIZE_DEFAULT, backgroundColor = BG_COLOR_WHITE),
)

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Resume Format Screen")
@Composable
private fun ResumeFormatScreenPreview() {
    SmartResumeTheme {
        var selectedFormatId by remember { mutableIntStateOf(1) }
        var selectedFontStyle by remember { mutableStateOf(FONT_TIMES_NEW_ROMAN) }
        var selectedFontSize by remember { mutableIntStateOf(FONT_SIZE_DEFAULT) }
        var selectedBgColor by remember { mutableStateOf(BG_COLOR_WHITE) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Resume Format") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader("Template")
                FormatList(
                    formats = previewFormats,
                    selectedId = selectedFormatId,
                    onSelect = { selectedFormatId = it },
                    onPreview = {}
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SectionHeader("Font")
                FontStyleDropdown(
                    selected = selectedFontStyle,
                    onSelected = { selectedFontStyle = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()

                SectionHeader("Font Size")
                FontSizeSlider(
                    value = selectedFontSize,
                    onValueChange = { selectedFontSize = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()

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
}

@Preview(showBackground = true, name = "Format List Item - Unselected")
@Composable
private fun FormatListItemUnselectedPreview() {
    SmartResumeTheme {
        FormatListItem(
            format = previewFormats[0],
            isSelected = false,
            onClick = {},
            onPreviewClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Format List Item - Selected")
@Composable
private fun FormatListItemSelectedPreview() {
    SmartResumeTheme {
        FormatListItem(
            format = previewFormats[0],
            isSelected = true,
            onClick = {},
            onPreviewClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Font Size Slider")
@Composable
private fun FontSizeSliderPreview() {
    SmartResumeTheme {
        FontSizeSlider(
            value = FONT_SIZE_DEFAULT,
            onValueChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun openFormatPreview(context: Context, formatId: Int) {
    val filename = "ResumeFormatPreview$formatId.pdf"
    val file = File(context.filesDir, filename)
    runCatching {
        context.assets.open("resume-format-previews/$filename").use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    }
}
