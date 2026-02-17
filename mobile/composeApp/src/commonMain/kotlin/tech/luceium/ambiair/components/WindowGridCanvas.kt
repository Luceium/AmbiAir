package tech.luceium.ambiair.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun WindowGridCanvas(
    homeLayout: MutableList<Boolean>,
    horizontalWindowLayout: SnapshotStateList<Boolean>,
    verticalWindowLayout: SnapshotStateList<Boolean>,
    rows: Int,
    cols: Int,
    cellSizeDp: Dp
) {
    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSizeDp.toPx() }
    val edgeTapSize = 12.dp
    val edgeTapSizePx = with(density) { edgeTapSize.toPx() }

    Canvas(
        modifier = Modifier
            .size(width = cellSizeDp * cols, height = cellSizeDp * rows)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val col = (offset.x / cellSizePx).toInt().coerceIn(0, cols - 1)
                    val row = (offset.y / cellSizePx).toInt().coerceIn(0, rows - 1)
                    val dx = offset.x % cellSizePx
                    val dy = offset.y % cellSizePx

                    // Horizontal Windows (between rows)
                    when {
                        // Top edge of cell (upper perimeter)
                        dy < edgeTapSizePx -> {
                            val index = row * cols + col
                            horizontalWindowLayout[index] = !horizontalWindowLayout[index]
                        }
                        // Bottom edge of cell (lower perimeter)
                        dy > cellSizePx - edgeTapSizePx -> {
                            val index = (row + 1) * cols + col
                            horizontalWindowLayout[index] = !horizontalWindowLayout[index]
                        }
                        // Left edge of cell (left perimeter)
                        dx < edgeTapSizePx -> {
                            val index = row * (cols + 1) + col
                            verticalWindowLayout[index] = !verticalWindowLayout[index]
                        }
                        // Right edge of cell (right perimeter)
                        dx > cellSizePx - edgeTapSizePx -> {
                            val index = row * (cols + 1) + col + 1
                            verticalWindowLayout[index] = !verticalWindowLayout[index]
                        }
                    }
                }
            }
    ) {
        // Draw cells
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val topLeft = Offset(col * cellSizePx, row * cellSizePx)

                drawRect(
                    color = if (homeLayout.getOrNull(index) == true) Color(0xFFD6C3AF) else Color.White,
                    topLeft = topLeft,
                    size = Size(cellSizePx, cellSizePx)
                )

                drawRect(
                    color = Color.Black,
                    topLeft = topLeft,
                    size = Size(cellSizePx, cellSizePx),
                    style = Stroke(width = 1f)
                )
            }
        }

        /// Draw horizontal windows (between rows)
        for (row in 0..rows) {  // 0-5 (6 lines)
            for (col in 0 until cols) {
                val index = row * cols + col
                if (horizontalWindowLayout.getOrNull(index) == true) {
                    drawRect(
                        color = Color(0xFF91A6D9),
                        topLeft = Offset(col * cellSizePx, row * cellSizePx - edgeTapSizePx),
                        size = Size(cellSizePx, edgeTapSizePx * 2)
                    )
                }
            }
        }

        // Draw vertical windows (between columns)
        for (row in 0 until rows) {
            for (col in 0..cols) {  // 0-5 (6 lines)
                val index = row * (cols + 1) + col
                if (verticalWindowLayout.getOrNull(index) == true) {
                    drawRect(
                        color = Color(0xFF91A6D9),
                        topLeft = Offset(col * cellSizePx - edgeTapSizePx, row * cellSizePx),
                        size = Size(edgeTapSizePx * 2, cellSizePx)
                    )
                }
            }
        }
    }
}
