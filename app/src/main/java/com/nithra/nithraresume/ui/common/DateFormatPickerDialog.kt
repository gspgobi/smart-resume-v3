package com.nithra.nithraresume.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nithra.nithraresume.utils.ALL_DATE_FORMATS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.nithra.nithraresume.ui.preview.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFormatPickerDialog(
    currentFormat: String,
    currentDateMs: Long? = null,          // pre-selected date from the field (null → today)
    onConfirm: (format: String, dateStr: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by rememberSaveable { mutableStateOf(currentFormat) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDateMs ?: System.currentTimeMillis(),
        initialDisplayMode = DisplayMode.Input
    )

    val selectedDate = remember(datePickerState.selectedDateMillis) {
        Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())
    }

    val previewDate = remember(selectedFormat, datePickerState.selectedDateMillis) {
        runCatching {
            SimpleDateFormat(selectedFormat, Locale.getDefault()).format(selectedDate)
        }.getOrDefault("")
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(selectedFormat, previewDate) }) { Text("Select") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // ── Calendar picker ───────────────────────────────────────────────
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )

            // ── Format section ────────────────────────────────────────────────
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Shows the selected date in the chosen format; dropdown lists every
                // format as the actual formatted date so the user picks by appearance.
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = previewDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date Format") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ALL_DATE_FORMATS.forEach { fmt ->
                            val formatted = runCatching {
                                SimpleDateFormat(fmt, Locale.getDefault()).format(selectedDate)
                            }.getOrDefault(fmt)
                            DropdownMenuItem(
                                text = { Text(formatted) },
                                onClick = { selectedFormat = fmt; expanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun DateFormatPickerDialogPreview() {
    SmartResumeTheme {
        DateFormatPickerDialog(
            currentFormat = "dd/MM/yyyy",
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}
