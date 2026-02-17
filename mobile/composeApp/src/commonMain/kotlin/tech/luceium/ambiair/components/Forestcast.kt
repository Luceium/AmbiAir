package tech.luceium.ambiair.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.SensorDataEntry
import tech.luceium.ambiair.services.ConnectionHealth

@Composable
fun ForecastComposable(
    place: String,
    high: Float,
    low: Float,
    connectionHealth: ConnectionHealth,
    hourlyData: List<SensorDataEntry>
) {
    var showTable by remember { mutableStateOf(false) }

    val isDimmed = connectionHealth != ConnectionHealth.STABLE
    val backgroundAlpha = 0.4f
    val statusMessage = when (connectionHealth) {
        ConnectionHealth.OFFLINE -> "Connection is offline"
        ConnectionHealth.SPOTTY  -> "Connection is unstable"
        else                     -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color.White.copy(alpha = backgroundAlpha),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Today's $place forecast")
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HI")
                    Text("$high")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LO")
                    Text("$low")
                }
            }

            Spacer(Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                if (hourlyData.isEmpty()) return@Canvas

                val temps = if (place.equals("outdoor", true))
                    hourlyData.map { it.outdoorTemp }
                else
                    hourlyData.map { it.indoorTemp }
                val range = high - low
                val stepX = size.width / 24
                val points =
                    temps.mapIndexed { i, t ->
                        Offset(
                            x = i * stepX,
                         y = size.height - ((t - low) / range * size.height)
                        )
                    }

                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val mid = Offset((prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                            quadraticTo(prev.x, prev.y, mid.x, mid.y)
                        }
                        lineTo(points.last().x, points.last().y)
                    }

                drawPath(
                    path = path,
                    color = Color(0xFFD9A2F0),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12am"); Text("12pm"); Text("12am")
            }

            Spacer(Modifier.height(4.dp))

            TextButton(onClick = { showTable = !showTable }) {
                Text(if (showTable) "Show less" else "Show more")
            }

            if (showTable) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Time", fontWeight = FontWeight.Bold)
                    Text("Temp (°F)", fontWeight = FontWeight.Bold)
                }
                hourlyData.forEach { entry ->
                    val hour = entry.minute / 60
                    val minutePart = entry.minute % 60
                    val timeFormatted = "${hour.toString().padStart(2, '0')}:${minutePart.toString().padStart(2, '0')}"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(timeFormatted)
                        val tempText = if (place.equals("outdoor", true))
                            "${entry.outdoorTemp}" else "${entry.indoorTemp}"
                        Text(tempText)
                    }
                }
            }
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
