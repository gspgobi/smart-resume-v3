package com.nithra.nithraresume.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import com.nithra.nithraresume.ui.preview.AppPreview

@Composable
fun SectionDivider(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@AppPreview
@Composable
private fun SectionDividerPreview() {
    SmartResumeTheme {
        SectionDivider(label = "Personal Information")
    }
}
