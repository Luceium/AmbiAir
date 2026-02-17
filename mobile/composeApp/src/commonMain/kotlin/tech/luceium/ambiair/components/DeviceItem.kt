package tech.luceium.ambiair.components

import Window
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Device
import tech.luceium.ambiair.data.DeviceType



@Composable
fun DeviceItem(device: Device, isSelected: Boolean, onDeviceSelected: (Device) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onDeviceSelected(device) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for device icon
            Icon(
                imageVector = when (device.type) {
                    DeviceType.TEMPERATURE -> Thermometer
                    DeviceType.WINDOW -> Window
                },
                contentDescription = device.type.name
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = device.name)
        }
    }
}