package com.nithra.nithraresume.ui.section.head

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.ui.tooling.preview.Preview
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderSectionsScreen(
    navController: NavController,
    viewModel: ReorderViewModel = hiltViewModel()
) {
    val sourceItems by viewModel.items.collectAsStateWithLifecycle()
    val title = if (viewModel.groupId == GROUP_ID_SECTIONS) "Reorder Sections" else "Reorder Add-ons"

    // Contact Information is always pinned at top — never draggable
    val pinnedItem = if (viewModel.groupId == GROUP_ID_SECTIONS)
        sourceItems.firstOrNull { it.headBaseId == 1 }
    else null

    // pinnedCount is stable (groupId never changes) — safe to capture in coroutine closures
    val pinnedCount = if (viewModel.groupId == GROUP_ID_SECTIONS) 1 else 0

    val mutableItems = remember { mutableStateListOf<SectionHeadAdded>() }

    LaunchedEffect(sourceItems) {
        val filtered = if (viewModel.groupId == GROUP_ID_SECTIONS)
            sourceItems.filter { it.headBaseId != 1 }
        else
            sourceItems

        val localIds  = mutableItems.map { it.id }.toSet()
        val sourceIds = filtered.map { it.id }.toSet()
        if (localIds != sourceIds) {
            mutableItems.clear()
            mutableItems.addAll(filtered)
        } else {
            val byId = filtered.associateBy { it.id }
            mutableItems.forEachIndexed { idx, item ->
                byId[item.id]?.let { updated ->
                    if (mutableItems[idx] != updated) mutableItems[idx] = updated
                }
            }
        }
    }

    // Drag state
    var draggingItemId by remember { mutableIntStateOf(-1) }
    var dragOffsetY    by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx  = remember { mutableStateMapOf<Int, Float>() }

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
        },
        bottomBar = { LargeBannerAdBottomBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Pinned: Contact Information ───────────────────────────────────
            if (pinnedItem != null) {
                ReorderRow(item = pinnedItem, isPinned = true)
                HorizontalDivider()
            }

            // ── Draggable items ───────────────────────────────────────────────
            mutableItems.forEachIndexed { _, item ->
                key(item.id) {
                    val isDragging = item.id == draggingItemId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { itemHeightsPx[item.id] = it.size.height.toFloat() }
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationY = dragOffsetY
                                    shadowElevation = 8f
                                }
                            }
                    ) {
                        ReorderRow(
                            item = item,
                            dragHandleModifier = Modifier.pointerInput(item.id) {
                                detectDragGestures(
                                    onDragStart = { _ ->
                                        draggingItemId = item.id
                                        dragOffsetY    = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                        val curIdx = mutableItems.indexOfFirst { it.id == draggingItemId }
                                        if (curIdx < 0) return@detectDragGestures
                                        // Swap up
                                        if (curIdx > 0) {
                                            val h = itemHeightsPx[mutableItems[curIdx - 1].id]
                                                ?: return@detectDragGestures
                                            if (dragOffsetY < -(h / 2f)) {
                                                mutableItems.add(curIdx - 1, mutableItems.removeAt(curIdx))
                                                dragOffsetY += h
                                            }
                                        }
                                        // Swap down (re-locate index after possible up-swap)
                                        val newIdx = mutableItems.indexOfFirst { it.id == draggingItemId }
                                        if (newIdx in 0 until mutableItems.size - 1) {
                                            val h = itemHeightsPx[mutableItems[newIdx + 1].id]
                                                ?: return@detectDragGestures
                                            if (dragOffsetY > h / 2f) {
                                                mutableItems.add(newIdx + 1, mutableItems.removeAt(newIdx))
                                                dragOffsetY -= h
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        viewModel.persistOrder(mutableItems.toList(), startIndex = pinnedCount)
                                        draggingItemId = -1
                                        dragOffsetY    = 0f
                                    },
                                    onDragCancel = {
                                        draggingItemId = -1
                                        dragOffsetY    = 0f
                                    }
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ReorderRow(
    item: SectionHeadAdded,
    dragHandleModifier: Modifier = Modifier,
    isPinned: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.DragHandle,
            contentDescription = if (isPinned) "Fixed at top" else "Drag to reorder",
            modifier = if (isPinned) Modifier.size(20.dp) else dragHandleModifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isPinned) 0.45f else 1f)
        )
        Text(
            text = item.title.ifBlank { "No title" },
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = if (item.title.isBlank()) FontStyle.Italic else FontStyle.Normal,
            color = if (item.isEnable) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewSectionItems = listOf(
    SectionHeadAdded(id = 1, profileId = 1, groupBaseId = 1, headBaseId = 1, sampleDataId = null, title = "Contact Information", isEnable = true, indexPosition = 0),
    SectionHeadAdded(id = 2, profileId = 1, groupBaseId = 1, headBaseId = 2, sampleDataId = null, title = "Work Experience", isEnable = true, indexPosition = 1),
    SectionHeadAdded(id = 3, profileId = 1, groupBaseId = 1, headBaseId = 3, sampleDataId = null, title = "Education", isEnable = true, indexPosition = 2),
    SectionHeadAdded(id = 4, profileId = 1, groupBaseId = 1, headBaseId = 4, sampleDataId = null, title = "Skills", isEnable = false, indexPosition = 3),
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Reorder Sections Screen")
@Composable
private fun ReorderSectionsScreenPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reorder Sections") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                previewSectionItems.forEachIndexed { index, item ->
                    ReorderRow(item = item, isPinned = index == 0)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Reorder Row - Pinned")
@Composable
private fun ReorderRowPinnedPreview() {
    SmartResumeTheme {
        ReorderRow(item = previewSectionItems[0], isPinned = true)
    }
}

@Preview(showBackground = true, name = "Reorder Row - Draggable")
@Composable
private fun ReorderRowDraggablePreview() {
    SmartResumeTheme {
        ReorderRow(item = previewSectionItems[1])
    }
}

@Preview(showBackground = true, name = "Reorder Row - Disabled")
@Composable
private fun ReorderRowDisabledPreview() {
    SmartResumeTheme {
        ReorderRow(item = previewSectionItems[3])
    }
}
