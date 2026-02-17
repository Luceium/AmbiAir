package tech.luceium.ambiair.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.data.DeviceType
import tech.luceium.ambiair.components.WindowSensorGridCanvas
import tech.luceium.ambiair.components.DeviceItem
import tech.luceium.ambiair.ui.theme.AmbiAirTypography

@Composable
fun PlaceWindowSensorsScreen(
    config: Config,
    onNext: () -> Unit
) {
    val homeLayout = remember { config.homeLayout }
    val horizontalWindowLayout = remember { config.horizontalWindowLayout.toMutableStateList() }
    val verticalWindowLayout = remember { config.verticalWindowLayout.toMutableStateList() }
    val devices = remember { config.devices.toMutableStateList() }

    val rows = 5
    val cols = 5
    val cellSizeDp = 60.dp

    var selectedSensor by remember { mutableStateOf<Device?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Text(
                "Set up",
                style = AmbiAirTypography().displayLarge,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )
            Text(
                "Place Window Sensors\nTap a sensor, then tap a window",
                style = AmbiAirTypography().titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Show grid with windows and placed sensors
            WindowSensorGridCanvas(
                homeLayout = homeLayout,
                horizontalWindowLayout = horizontalWindowLayout,
                verticalWindowLayout = verticalWindowLayout,
                rows = rows,
                cols = cols,
                cellSizeDp = cellSizeDp,
                devices = devices,
                selectedSensor = selectedSensor,
                onWindowTap = { isHorizontal, index ->
                    selectedSensor?.let { sensor ->
                        // Remove from previous window if already placed
                        devices.find { it.id == sensor.id }?.let { dev ->
                            // dev.location = -1  // <-- This mutates property, no recomposition triggered
                            val sensorIndex = devices.indexOfFirst { it.id == sensor.id }
                            if (sensorIndex != -1) {
                                devices[sensorIndex] = devices[sensorIndex].copy(location = -1)
                            }
                        }
                        // Remove any other sensor from this window
                        val windowLocation = if (isHorizontal) index else index + 30
                        devices.filter { it.type == DeviceType.WINDOW && it.location == windowLocation }
                            .forEach { otherSensor ->
                                val otherIndex = devices.indexOfFirst { it.id == otherSensor.id }
                                if (otherIndex != -1) {
                                    devices[otherIndex] = devices[otherIndex].copy(location = -1)
                                }
                            }
                        // Assign this sensor to the tapped window
                        val loc = if (isHorizontal) index else index + 30
                        val sensorIndex = devices.indexOfFirst { it.id == sensor.id }
                        if (sensorIndex != -1) {
                            devices[sensorIndex] = devices[sensorIndex].copy(location = loc)
                        }
                        selectedSensor = null
                    }
                }

            )

            Spacer(modifier = Modifier.height(16.dp))

            // List of window sensors
            LazyColumn(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
            ) {
                items(devices.filter { it.type == DeviceType.WINDOW }) { device ->
                    DeviceItem(
                        device = device,
                        isSelected = selectedSensor == device,
                        onDeviceSelected = {
                            selectedSensor = if (selectedSensor == it) null else it
                        }
                    )
                }
            }
        }

        // "Next" button
        Button(
            onClick = {
                // Save back to config
                config.devices.clear()
                config.devices.addAll(devices)
                onNext()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(140.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9DD88F)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Next", color = Color.Black, style = AmbiAirTypography().bodyLarge)
        }
    }
    // Linear Progress Indicator
    LinearProgressIndicator(
        progress = { 4f / 6f },
        modifier = Modifier.fillMaxWidth()
    )
}
