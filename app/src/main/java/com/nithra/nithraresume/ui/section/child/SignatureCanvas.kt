package com.nithra.nithraresume.ui.section.child

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.nithra.nithraresume.ui.theme.SmartResumeTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.nithra.nithraresume.ui.preview.AppPreview

class SignatureCaptureController {
    internal val completedPaths = mutableStateListOf<Path>()
    internal var widthPx = 0
    internal var heightPx = 0
    internal var strokePx = 18f

    val hasStrokes: Boolean get() = completedPaths.isNotEmpty()

    fun captureBitmap(): Bitmap? {
        if (widthPx <= 0 || heightPx <= 0) return null
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            strokeWidth = strokePx
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
        }
        for (path in completedPaths) {
            canvas.drawPath(path.asAndroidPath(), paint)
        }
        return bmp
    }

    internal fun clear() { completedPaths.clear() }
}

@Composable
fun rememberSignatureCaptureController() = remember { SignatureCaptureController() }

@Composable
fun SignatureCanvas(
    clearKey: Int,
    captureController: SignatureCaptureController,
    modifier: Modifier = Modifier
) {
    var revision by remember { mutableIntStateOf(0) }
    val currentPath = remember { Path() }

    LaunchedEffect(clearKey) {
        captureController.clear()
        currentPath.reset()
        revision++
    }

    Canvas(
        modifier = modifier
            .background(Color.White)
            .onSizeChanged { size ->
                captureController.widthPx = size.width
                captureController.heightPx = size.height
            }
            .pointerInput(Unit) {
                captureController.strokePx = 3.dp.toPx()
                awaitEachGesture {
                    val w = captureController.widthPx.toFloat()
                    val h = captureController.heightPx.toFloat()
                    val down = awaitFirstDown()
                    currentPath.reset()
                    currentPath.moveTo(
                        down.position.x.coerceIn(0f, w),
                        down.position.y.coerceIn(0f, h)
                    )
                    revision++
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.pressed }) break
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                currentPath.lineTo(
                                    change.position.x.coerceIn(0f, w),
                                    change.position.y.coerceIn(0f, h)
                                )
                                revision++
                                change.consume()
                            }
                        }
                    }
                    captureController.completedPaths.add(Path().apply { addPath(currentPath) })
                    currentPath.reset()
                    revision++
                }
            }
    ) {
        @Suppress("UNUSED_VARIABLE")
        val v = revision
        val style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        clipRect(0f, 0f, size.width, size.height) {
            for (path in captureController.completedPaths) {
                drawPath(path, color = Color.DarkGray, style = style)
            }
            if (!currentPath.isEmpty) {
                drawPath(currentPath, color = Color.DarkGray, style = style)
            }
        }
    }
}

@AppPreview
@Composable
private fun SignatureCanvasPreview() {
    SmartResumeTheme {
        SignatureCanvas(
            clearKey = 0,
            captureController = remember { SignatureCaptureController() },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
