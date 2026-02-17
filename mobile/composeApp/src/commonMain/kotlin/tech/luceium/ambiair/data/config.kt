package tech.luceium.ambiair.data

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val devices: MutableList<Device> = mutableListOf(),
    val homeLayout: MutableList<Boolean> = MutableList(25) { false },
    val horizontalWindowLayout: MutableList<Boolean> = MutableList(30) { false },
    val verticalWindowLayout: MutableList<Boolean> = MutableList(30) { false },
    var homeLocation: Pair<Double, Double> = Pair(0.0, 0.0),
    var geofenceRadius: Double = 0.0,
) {
    fun deepCopy(): Config {
        return Config(
            // Get a copy of the list, then replace all elements with copies
            devices = devices.toMutableList().map { it.copy() }.toMutableList(),
            homeLayout = homeLayout.toMutableList(),
            horizontalWindowLayout = horizontalWindowLayout.toMutableList(),
            verticalWindowLayout = verticalWindowLayout.toMutableList(),
            homeLocation = homeLocation
        )
    }
}

@Serializable
data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    var location: Int
)

@Serializable
enum class DeviceType { TEMPERATURE, WINDOW }
