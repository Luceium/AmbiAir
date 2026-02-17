package tech.luceium.ambiair.services
import com.plusmobileapps.konnectivity.Konnectivity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay

enum class ConnectionHealth {
    STABLE,
    SPOTTY,
    OFFLINE
}

@OptIn(FlowPreview::class)
fun monitorConnectionHealth(
    konnectivity: Konnectivity,
    windowSize: Int = 5,
    intervalMs: Long = 1000
): Flow<ConnectionHealth> {
    return konnectivity.isConnectedState
        .sample(intervalMs)
        .runningFold(emptyList<Boolean>()) { acc, isConnected ->
            (acc + isConnected).takeLast(windowSize)
        }
        .map { recent ->
            val connectedCount = recent.count { it }
            val disconnectedCount = recent.size - connectedCount

            val health = when {
                connectedCount == recent.size -> ConnectionHealth.STABLE
                disconnectedCount >= (recent.size * 0.8) -> ConnectionHealth.OFFLINE
                else -> ConnectionHealth.SPOTTY
            }

            println("Connection health computed: $health")
            health
        }
}
