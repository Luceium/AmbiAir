package tech.luceium.ambiair.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.components.EditSensorDialog
import tech.luceium.ambiair.components.SensorRow
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.data.DeviceType
import tech.luceium.ambiair.ui.theme.AmbiAirTypography

@Composable
fun AddDeviceScreen(
    config: Config,
    onNext: () -> Unit,
) {
    val devices = remember { config.devices.toMutableStateList() }
    var showEditDialog by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var isNewDevice by remember { mutableStateOf(false) }
    var tempDevice by remember { mutableStateOf(Device("", "", DeviceType.TEMPERATURE, -1)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = "Set up",
                style = AmbiAirTypography().displayLarge,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )


            // "Add sensor" button
            Button(
                onClick = {
                    tempDevice = Device(
                        id = "tmp${devices.size + 1}",
                        name = "",
                        type = DeviceType.TEMPERATURE,
                        location = -1
                    )
                    isNewDevice = true
                    showEditDialog = true
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAFD3E9)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add sensor", color = Color.Black, style = AmbiAirTypography().bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sensor rows (scrollable)
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .weight(1f, fill = false)
                    .fillMaxWidth()
            ) {
                itemsIndexed(devices) { index, device ->
                    SensorRow(
                        device = device,
                        onEdit = {
                            editIndex = index
                            tempDevice = device.copy()
                            isNewDevice = false
                            showEditDialog = true
                        }
                    )
                }
            }



        }

        // "Next" button
        Button(
            onClick = {
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

        // Edit/Add Dialog
        if (showEditDialog) {
            EditSensorDialog(
                device = tempDevice,
                onNameChange = { tempDevice = tempDevice.copy(name = it) },
                onTypeChange = { tempDevice = tempDevice.copy(type = it) },
                onDelete = {
                    if (!isNewDevice && editIndex != null) {
                        devices.removeAt(editIndex!!)
                    }
                    showEditDialog = false
                },
                onSave = {
                    if (tempDevice.name.isNotBlank()) {
                        if (isNewDevice) {
                            devices.add(tempDevice.copy(id = "sensor${devices.size + 1}"))
                        } else if (editIndex != null) {
                            devices[editIndex!!] = tempDevice
                        }
                        showEditDialog = false
                    }
                },
                onDismiss = { showEditDialog = false },
                canDelete = !isNewDevice
            )
        }
    }
    // Linear Progress Indicator
    LinearProgressIndicator(
        progress = { 1f / 6f },
        modifier = Modifier.fillMaxWidth()
    )
}

