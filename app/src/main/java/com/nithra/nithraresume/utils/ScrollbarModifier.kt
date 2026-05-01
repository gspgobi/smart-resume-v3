package com.nithra.nithraresume.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    minThumbHeight: Dp = 40.dp,
    color: Color? = null,
): Modifier = composed {
    val resolvedColor = color ?: MaterialTheme.colorScheme.primary
    val alpha by animateFloatAsState(
        targetValue = if (state.isScrollInProgress) 0.7f else 0f,
        animationSpec = tween(durationMillis = if (state.isScrollInProgress) 0 else 800),
        label = "scrollbar"
    )
    drawWithContent {
        drawContent()
        if (alpha == 0f) return@drawWithContent
        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return@drawWithContent
        val totalItems = layoutInfo.totalItemsCount
        if (totalItems <= visibleItems.size) return@drawWithContent

        val avgItemHeight = visibleItems.map { it.size }.average().toFloat()
        val totalContentHeight = avgItemHeight * totalItems
        val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
        val thumbHeight = (viewportHeight / totalContentHeight * viewportHeight)
            .coerceAtLeast(minThumbHeight.toPx())
            .coerceAtMost(viewportHeight)
        val scrollableRange = totalContentHeight - viewportHeight
        val scrolled = visibleItems.first().index * avgItemHeight + state.firstVisibleItemScrollOffset
        val fraction = if (scrollableRange > 0f) (scrolled / scrollableRange).coerceIn(0f, 1f) else 0f
        val thumbY = layoutInfo.viewportStartOffset + fraction * (viewportHeight - thumbHeight)

        drawRoundRect(
            color = resolvedColor,
            topLeft = Offset(size.width - width.toPx(), thumbY),
            size = Size(width.toPx(), thumbHeight),
            cornerRadius = CornerRadius(width.toPx() / 2),
            alpha = alpha
        )
    }
}
