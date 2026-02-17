package tech.luceium.ambiair.data

/**
 * Data class representing sensor data stored in Firebase Firestore.
 *
 */
abstract class SensorData(val indoorTemp: Float, val outdoorTemp: Float)

class SensorDataEntry(indoorTemp: Float, outdoorTemp: Float, var minute: Int) :
    SensorData(indoorTemp, outdoorTemp) {
    init {
        // Convert from minutes of year to minutes of day
        minute %= (60 * 24)
    }
}

class CurrentData(indoorTemp: Float, outdoorTemp: Float, val windowOpen: Boolean) :
    SensorData(indoorTemp, outdoorTemp)