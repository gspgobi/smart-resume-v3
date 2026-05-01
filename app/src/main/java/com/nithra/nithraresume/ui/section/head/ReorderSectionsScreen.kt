package com.nithra.nithraresume.ui.section.head

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderSectionsScreen(
    navController: NavController,
    viewModel: ReorderViewModel = hiltViewModel()
) {
    val sourceItems by viewModel.items.collectAsStateWithLifecycle()
    val title = if (viewModel.groupId == GROUP_ID_SECTIONS) "Reorder Sections" else "Reorder Add-ons"

    val mutableItems = remember { mutableStateListOf<SectionHeadAdded>() }

    LaunchedEffect(sourceItems) {
        val localIds  = mutableItems.map { it.id }.toSet()
        val sourceIds = sourceItems.map { it.id }.toSet()
        if (localIds != sourceIds) {
            mutableItems.clear()
            mutableItems.addAll(sourceItems)
        } else {
            val byId = sourceItems.associateBy { it.id }
            mutableItems.forEachIndexed { idx, item ->
                byId[item.id]?.let { updated ->
                    if (mutableItems[idx] != updated) mutableItems[idx] = updated
                }
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fi = mutableItems.indexOfFirst { it.id == from.key }
        val ti = mutableItems.indexOfFirst { it.id == to.key }
        if (fi >= 0 && ti >= 0) mutableItems.apply { add(ti, removeAt(fi)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        }
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(mutableItems, key = { it.id }) { item ->
                ReorderableItem(reorderState, key = item.id) { _ ->
                    ReorderRow(
                        item = item,
                        dragHandleModifier = Modifier.draggableHandle(
                            onDragStopped = { viewModel.persistOrder(mutableItems.toList()) }
                        )
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ReorderRow(
    item: SectionHeadAdded,
    dragHandleModifier: Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            modifier = dragHandleModifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.title.isBlank()) {
            Text(
                text = "No title",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.isEnable) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
