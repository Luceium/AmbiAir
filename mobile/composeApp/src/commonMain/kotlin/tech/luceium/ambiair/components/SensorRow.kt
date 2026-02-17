package tech.luceium.ambiair.components

import Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.data.DeviceType
import tech.luceium.ambiair.ui.theme.AmbiAirTypography

@Composable
fun SensorRow(
    device: Device,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFF6F6F6), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = when (device.type) {
                DeviceType.TEMPERATURE -> Thermometer
                DeviceType.WINDOW -> Window
            },
            contentDescription = device.type.name,
            tint = if (device.type == DeviceType.TEMPERATURE) Color(0xFFFFC107) else Color(0xFF91A6D9),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(28.dp)
        )
        Text(
            text = device.name,
            style = AmbiAirTypography().bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
}