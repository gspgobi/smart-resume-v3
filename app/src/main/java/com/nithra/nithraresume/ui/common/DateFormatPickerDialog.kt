package com.nithra.nithraresume.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

/**
 * Shared dialog for selecting a date format.
 *
 * Formats today's date with the chosen format and returns both the format string
 * and the formatted date string to [onConfirm].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFormatPickerDialog(
    currentFormat: String,
    onConfirm: (format: String, dateStr: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by rememberSaveable { mutableStateOf(currentFormat) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val previewDate = remember(selectedFormat) {
        runCatching {
            SimpleDateFormat(selectedFormat, Locale.getDefault()).format(Date())
        }.getOrDefault("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Format") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFormat,
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
                            DropdownMenuItem(
                                text = {
                                    val sample = runCatching {
                                        SimpleDateFormat(fmt, Locale.getDefault()).format(Date())
                                    }.getOrDefault(fmt)
                                    Text("$fmt  ($sample)")
                                },
                                onClick = { selectedFormat = fmt; expanded = false }
                            )
                        }
                    }
                }
                if (previewDate.isNotEmpty()) {
                    Text(
                        text = "Preview: $previewDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedFormat, previewDate) }) { Text("Select") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
