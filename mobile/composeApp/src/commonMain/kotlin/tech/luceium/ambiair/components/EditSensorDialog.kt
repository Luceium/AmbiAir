package tech.luceium.ambiair.components

import Window
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.data.DeviceType
import tech.luceium.ambiair.ui.theme.AmbiAirTypography

@Composable
fun EditSensorDialog(
    device: Device,
    onNameChange: (String) -> Unit,
    onTypeChange: (DeviceType) -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    canDelete: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                if (canDelete) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
                    ) { Text("Delete") }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9DD88F))
                ) { Text("Save") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Edit Sensor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = device.name,
                    onValueChange = onNameChange,
                    label = { Text("Sensor Name") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Type:", style = AmbiAirTypography().bodyMedium)
                    DeviceType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .background(
                                    if (device.type == type) Color(0xFFAFD3E9) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { onTypeChange(type) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (type) {
                                    DeviceType.TEMPERATURE -> Thermometer
                                    DeviceType.WINDOW -> Window
                                },
                                contentDescription = type.name,
                                tint = if (type == DeviceType.TEMPERATURE) Color(0xFFFFC107) else Color(0xFF91A6D9)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
