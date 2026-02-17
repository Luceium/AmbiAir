package tech.luceium.ambiair.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.CurrentData
import tech.luceium.ambiair.data.DeviceType
import tech.luceium.ambiair.services.ConnectionHealth
import androidx.compose.ui.platform.LocalDensity

@Composable
fun HomeLayoutComposable(
    config: Config,
    connectionHealth: ConnectionHealth,
    currentStatus: CurrentData
) {
    val homeLayout = remember { config.homeLayout }
    val horizontalWindowLayout = remember { config.horizontalWindowLayout }
    val verticalWindowLayout = remember { config.verticalWindowLayout }
    val devices = remember { config.devices.toMutableStateList() }

    val rows = 5
    val cols = 5
    val cellSizeDp = 60.dp

    val isDimmed = connectionHealth != ConnectionHealth.STABLE
    val statusMessage = when (connectionHealth) {
        ConnectionHealth.OFFLINE -> "Connection is offline"
        ConnectionHealth.SPOTTY  -> "Connection is unstable"
        else                    -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val density = LocalDensity.current
            val cellSizePx = with(density) { cellSizeDp.toPx() }
            val edgeTapSize = 12.dp
            val edgeTapSizePx = with(density) { edgeTapSize.toPx() }

            Canvas(
                modifier = Modifier
                    .size(width = cellSizeDp * cols, height = cellSizeDp * rows)
            ) {
                // Draw cells
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        val index = row * cols + col
                        val topLeft = Offset(col * cellSizePx, row * cellSizePx)

                        drawRect(
                            color = if (homeLayout.getOrNull(index) == true) Color(0xFFD6C3AF) else Color.Transparent,
                            topLeft = topLeft,
                            size = Size(cellSizePx, cellSizePx)
                        )

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

                // Draw horizontal windows (between rows)
                for (row in 0..rows) {
                    for (col in 0 until cols) {
                        val index = row * cols + col
                        if (horizontalWindowLayout.getOrNull(index) == true) {
                            // pick color based on current status
                            val isOpen = currentStatus.windowOpen
                            val windowColor = if (isOpen) Color(0xFF9DD88F) else Color(0xFF91A6D9)
                            drawRect(
                                color = windowColor,
                                topLeft = Offset(col * cellSizePx, row * cellSizePx - edgeTapSizePx),
                                size = Size(cellSizePx, edgeTapSizePx * 2)
                            )
                        }
                    }
                }

                // Draw vertical windows (between columns)
                for (row in 0 until rows) {
                    for (col in 0..cols) {
                        val index = row * (cols + 1) + col
                        if (verticalWindowLayout.getOrNull(index) == true) {
                            // pick color based on current status
                            val isOpen = currentStatus.windowOpen
                            val windowColor = if (isOpen) Color(0xFF9DD88F) else Color(0xFF91A6D9)
                            drawRect(
                                color = windowColor,
                                topLeft = Offset(col * cellSizePx - edgeTapSizePx, row * cellSizePx),
                                size = Size(edgeTapSizePx * 2, cellSizePx)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Legend:", style = MaterialTheme.typography.bodyMedium)
            LegendItem(color = Color(0xFF9DD88F),    label = "Window open")
            LegendItem(color = Color(0xFF91A6D9),    label = "Window closed")
            LegendItem(color = Color.Yellow,  label = "Temperature Sensor")

        }

        if (isDimmed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                statusMessage?.let { Text(it, color = Color.White) }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color = color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
