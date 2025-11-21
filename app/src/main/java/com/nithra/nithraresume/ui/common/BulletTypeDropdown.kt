package com.nithra.nithraresume.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nithra.nithraresume.utils.ALL_BULLET_TYPES
import com.nithra.nithraresume.utils.BULLET_NONE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulletTypeDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    label: String = "Bullet Type",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ALL_BULLET_TYPES.forEach { bt ->
                DropdownMenuItem(
                    text = { Text(if (bt == BULLET_NONE) "None" else "Bullet  $bt") },
                    onClick = { onSelected(bt); expanded = false }
                )
            }
        }
    }
}
// update 113
