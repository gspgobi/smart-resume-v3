package com.nithra.nithraresume.ui.section.child

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import com.nithra.nithraresume.utils.LargeBannerAdBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderChildScreen(
    navController: NavController,
    viewModel: ReorderChildViewModel = hiltViewModel()
) {
    val sourceItems by viewModel.items.collectAsStateWithLifecycle()
    val mutableItems = remember { mutableStateListOf<ReorderableItem>() }

    LaunchedEffect(sourceItems) {
        val localIds  = mutableItems.map { it.id }.toSet()
        val sourceIds = sourceItems.map { it.id }.toSet()
        if (localIds != sourceIds) {
            mutableItems.clear()
            mutableItems.addAll(sourceItems.sortedBy { it.indexPosition })
        } else {
            val byId = sourceItems.associateBy { it.id }
            mutableItems.forEachIndexed { idx, item ->
                byId[item.id]?.let { updated ->
                    if (mutableItems[idx] != updated) mutableItems[idx] = updated
                }
            }
        }
    }

    var draggingItemId by remember { mutableIntStateOf(-1) }
    var dragOffsetY    by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx  = remember { mutableStateMapOf<Int, Float>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reorder Entries") },
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
                        ReorderChildRow(
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
                                        if (curIdx > 0) {
                                            val h = itemHeightsPx[mutableItems[curIdx - 1].id]
                                                ?: return@detectDragGestures
                                            if (dragOffsetY < -(h / 2f)) {
                                                mutableItems.add(curIdx - 1, mutableItems.removeAt(curIdx))
                                                dragOffsetY += h
                                            }
                                        }
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
                                        viewModel.persistOrder(mutableItems.toList())
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
private fun ReorderChildRow(
    item: ReorderableItem,
    dragHandleModifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            modifier = dragHandleModifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = item.displayText.ifEmpty { "(no title)" },
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = if (item.displayText.isEmpty()) FontStyle.Italic else FontStyle.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewChildItems = listOf(
    ReorderableItem(id = 1, indexPosition = 0, displayText = "Software Engineer at Google"),
    ReorderableItem(id = 2, indexPosition = 1, displayText = "Product Manager at Meta"),
    ReorderableItem(id = 3, indexPosition = 2, displayText = ""),
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Reorder Child Screen")
@Composable
private fun ReorderChildScreenPreview() {
    SmartResumeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reorder Entries") },
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
                previewChildItems.forEach { item ->
                    ReorderChildRow(item = item)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Reorder Child Row")
@Composable
private fun ReorderChildRowPreview() {
    SmartResumeTheme {
        ReorderChildRow(item = previewChildItems[0])
    }
}

@Preview(showBackground = true, name = "Reorder Child Row - Empty")
@Composable
private fun ReorderChildRowEmptyPreview() {
    SmartResumeTheme {
        ReorderChildRow(item = previewChildItems[2])
    }
}
