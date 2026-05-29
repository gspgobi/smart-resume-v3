package com.nithra.nithraresume.ui.common

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import androidx.compose.runtime.LaunchedEffect
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
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.nithra.nithraresume.utils.AssetDir
import com.nithra.nithraresume.utils.AssetFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.nithra.nithraresume.ui.preview.AppPreview

// ── JSON data classes ─────────────────────────────────────────────────────────

private data class ObjAccompJson(
    @SerializedName("contents") val contents: List<ObjAccompItem> = emptyList()
)

private data class ObjAccompItem(
    @SerializedName("contentId")   val contentId: Int = 0,
    @SerializedName("contentType") val contentType: String = "",
    @SerializedName("contentData") val contentData: String = ""
)

// ── Composable ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjAccompBottomSheet(
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    // groups: contentType → list of contentData strings
    var groups by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        groups = loadObjAccompGroups(context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Text(
            text = "Objectives / Suggestions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            groups.forEach { (type, items) ->
                val isExpanded = expandedState[type] ?: false

                item(key = "header_$type") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { expandedState[type] = !isExpanded }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { expandedState[type] = !isExpanded }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess
                                              else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                            )
                        }
                    }
                    HorizontalDivider()
                }

                if (isExpanded) {
                    items(items, key = { "item_${type}_$it" }) { text ->
                        Column {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onItemSelected(text)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 24.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Asset loader ──────────────────────────────────────────────────────────────

@AppPreview
@Composable
private fun ObjAccompBottomSheetPreview() {
    SmartResumeTheme {
        ObjAccompBottomSheet(
            onDismiss = {},
            onItemSelected = {}
        )
    }
}

private suspend fun loadObjAccompGroups(context: Context): Map<String, List<String>> =
    withContext(Dispatchers.IO) {
        val assetPath = "${AssetDir.JSON}/${AssetFile.OBJECTIVES_JSON}"
        runCatching {
            val json = context.assets.open(assetPath).bufferedReader().readText()
            val parsed = Gson().fromJson(json, ObjAccompJson::class.java)
            parsed.contents
                .groupBy { it.contentType }
                .mapValues { (_, items) -> items.map { it.contentData } }
        }.getOrDefault(emptyMap())
    }
