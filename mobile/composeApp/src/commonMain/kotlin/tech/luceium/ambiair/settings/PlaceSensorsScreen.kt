package tech.luceium.ambiair.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.components.DeviceItem
import tech.luceium.ambiair.components.SensorGridCanvas
import tech.luceium.ambiair.data.DeviceType
import tech.luceium.ambiair.ui.theme.AmbiAirTypography


@Composable
fun PlaceSensorsScreen(
    config: Config,
    onNext: () -> Unit
) {
    val homeLayout = remember { config.homeLayout }
    val horizontalWindowLayout = remember { config.horizontalWindowLayout }
    val verticalWindowLayout = remember { config.verticalWindowLayout }
    val devices = remember { config.devices.toMutableStateList() }

    val rows = 5
    val cols = 5
    val cellSizeDp = 60.dp

    var selectedDevice by remember { mutableStateOf<Device?>(null) }

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
                text = "Set up",
                style = AmbiAirTypography().displayLarge,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )
            Text(
                "Place Sensors\nTap a sensor, then tap a location",
                style = AmbiAirTypography().titleLarge,
                modifier = Modifier.padding(bottom = 16.dp))

            SensorGridCanvas(
                homeLayout = homeLayout,
                horizontalWindowLayout = horizontalWindowLayout,
                verticalWindowLayout = verticalWindowLayout,
                devices = devices,
                rows = rows,
                cols = cols,
                cellSizeDp = cellSizeDp,
                selectedDevice = selectedDevice,
                onCellClick = { index ->
                    selectedDevice?.let { device ->
                        // Check if any other device exists in this index, and set its location to -1
                        val existingDeviceIndex = devices.indexOfFirst { it.location == index }
                        if (existingDeviceIndex != -1) {
                            devices[existingDeviceIndex] = devices[existingDeviceIndex].copy(location = -1)
                        }

                        // Remove the device from its previous location
                        if(device.location != -1) {
                            val prevDeviceIndex = devices.indexOfFirst { it.location == device.location }
                            if (prevDeviceIndex != -1) {
                                devices[prevDeviceIndex] = devices[prevDeviceIndex].copy(location = -1)
                            }
                        }

                        // Update the location of the selected device
                        val deviceIndex = devices.indexOfFirst { it.id == device.id }
                        if (deviceIndex != -1) {
                            devices[deviceIndex] = devices[deviceIndex].copy(location = index)
                        }

                        selectedDevice = null // Deselect after placing
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.height(150.dp)
            ) {
                items(devices) { device ->
                    if (device.type == DeviceType.TEMPERATURE) {
                        DeviceItem(
                            device = device,
                            isSelected = selectedDevice == device,
                            onDeviceSelected = {
                                selectedDevice = if (selectedDevice == it) null else it
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                // Update the original config with our changes
                config.devices.clear()
                config.devices.addAll(devices)

                // Now navigate to next screen
                onNext()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(140.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9DD88F) // Green color
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Next", color = Color.Black, style = AmbiAirTypography().bodyLarge)
        }
    }

    // Linear Progress Indicator
    LinearProgressIndicator(
        progress = { 5f / 6f },
        modifier = Modifier.fillMaxWidth()
    )
}