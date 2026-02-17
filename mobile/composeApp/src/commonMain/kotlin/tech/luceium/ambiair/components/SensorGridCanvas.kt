package tech.luceium.ambiair.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.data.DeviceType

@Composable
fun SensorGridCanvas(
    homeLayout: List<Boolean>,
    horizontalWindowLayout: List<Boolean>,
    verticalWindowLayout: List<Boolean>,
    devices: MutableList<Device>,
    rows: Int,
    cols: Int,
    cellSizeDp: Dp,
    selectedDevice: Device?,
    onCellClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSizeDp.toPx() }

    Canvas(
        modifier = Modifier
            .size(width = cellSizeDp * cols, height = cellSizeDp * rows)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val col = (offset.x / cellSizePx).toInt().coerceIn(0, cols - 1)
                    val row = (offset.y / cellSizePx).toInt().coerceIn(0, rows - 1)
                    val index = row * cols + col

                    if (homeLayout.getOrNull(index) == true) {
                        onCellClick(index)
                    }
                }
            }
    ) {
        // Draw cells
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val topLeft = Offset(col * cellSizePx, row * cellSizePx)

                val cellColor = if (homeLayout.getOrNull(index) == true) Color(0xFFD6C3AF) else Color.White

                drawRect(
                    color = cellColor,
                    topLeft = topLeft,
                    size = Size(cellSizePx, cellSizePx)
                )

                drawRect(
                    color = Color.Black,
                    topLeft = topLeft,
                    size = Size(cellSizePx, cellSizePx),
                    style = Stroke(width = 1f)
                )

                // Draw device if present
                val device = devices.find { it.location == index }
                if (device != null && device.type == DeviceType.TEMPERATURE) {
                    drawCircle(
                        color = Color.Yellow,
                        center = Offset(topLeft.x + cellSizePx / 2, topLeft.y + cellSizePx / 2),
                        radius = cellSizePx / 3
                    )
                }
            }
        }
    }
}