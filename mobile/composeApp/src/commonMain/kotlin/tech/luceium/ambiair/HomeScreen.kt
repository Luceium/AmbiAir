package tech.luceium.ambiair

import Grass
import Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.plusmobileapps.konnectivity.Konnectivity
import dev.gitlive.firebase.auth.FirebaseUser
import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import tech.luceium.ambiair.components.ForecastComposable
import tech.luceium.ambiair.components.HomeLayoutComposable
import tech.luceium.ambiair.components.NavMenu
import tech.luceium.ambiair.components.NavMenuItem
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.CurrentData
import tech.luceium.ambiair.data.FirebaseRepository
import tech.luceium.ambiair.data.LocationRepository
import tech.luceium.ambiair.services.ConnectionHealth
import tech.luceium.ambiair.services.monitorConnectionHealth
import tech.luceium.ambiair.ui.theme.coldLight
import tech.luceium.ambiair.ui.theme.warmLight
import kotlin.math.pow

@Composable
fun HomeScreen(
    user: FirebaseUser,
    scope: CoroutineScope,
    signOut: () -> Unit,
    config: Config,
    goToConfig: () -> Unit,
    refreshHome: () -> Unit,
    toast: (Exception) -> Unit
) {
    val scrollState = rememberScrollState()

    val launcher = rememberFileSaverLauncher { file ->
        if (file != null) {
            scope.launch {
                val json = Json.encodeToString(config)
                try {
                    file.writeString(json)
                } catch (e: Exception) {
                    Logger.e("Error writing file", e)
                }
            }
        }
    }

    val currentStatus: CurrentData by FirebaseRepository.currentStatusFlow
        .collectAsState(initial = CurrentData(0f, 0f, false))
    val hourlyForecasts by FirebaseRepository.hourlyForecasts
        .collectAsState(initial = emptyList())

    val highIndoor = hourlyForecasts.maxOfOrNull { it.indoorTemp } ?: 0f
    val lowIndoor = hourlyForecasts.minOfOrNull { it.indoorTemp } ?: 0f
    val highOutdoor = hourlyForecasts.maxOfOrNull { it.outdoorTemp } ?: 0f
    val lowOutdoor = hourlyForecasts.minOfOrNull { it.outdoorTemp } ?: 0f

    val konnectivity = remember { Konnectivity() }
    var connectionHealth by remember { mutableStateOf(ConnectionHealth.STABLE) }
    var showDialog by remember { mutableStateOf(false) }

    val permissionsControllerFactory = rememberPermissionsControllerFactory()
    val permissionsController =
        remember(permissionsControllerFactory) { permissionsControllerFactory.createPermissionsController() }
    BindEffect(permissionsController)

    val locationTrackerFactory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker =
        remember {
            locationTrackerFactory.createLocationTracker(permissionsController)
        }

    BindLocationTrackerEffect(locationTracker)

    val locationRepo = remember {
        LocationRepository.getInstance(permissionsController, locationTracker)
    }
    val location by locationRepo.location.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        locationRepo.initialize()
        monitorConnectionHealth(konnectivity).collect { health ->
            connectionHealth = health
            showDialog = health != ConnectionHealth.STABLE
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFCDCD), // Top-left: Light pink
                        Color(0xFFACD3FF)  // Bottom-right: Light blue
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
    ) {
        NavMenu(
            listOf(
                NavMenuItem(
                    if (user.isAnonymous) "Link account" else "Sign Out",
                    { Icon(Icons.Filled.Lock, contentDescription = "Lock") },
                    signOut
                ),
                NavMenuItem(
                    "Config",
                    { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    goToConfig
                ),
                NavMenuItem(
                    "Refresh",
                    { Icon(Icons.Filled.Settings, contentDescription = "Refresh") },
                    refreshHome
                ),
                NavMenuItem(
                    "Download Config",
                    { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Download") },
                    { launcher.launch("config", "json")}
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isDimmed = connectionHealth != ConnectionHealth.STABLE

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Hi ${if (user.isAnonymous) "Guest" else user.displayName ?: user.email}!",
                style = TextStyle(fontSize = 24.sp)
            )
            Row {
                Icon(Icons.Filled.Home, contentDescription = "Home")
                Text(
                    "${currentStatus.indoorTemp}° F",
                    color = if (currentStatus.indoorTemp < currentStatus.outdoorTemp) coldLight else warmLight
                )
                Spacer(modifier = Modifier.width(20.dp))
                Icon(Grass, contentDescription = "Home")
                Text(
                    "${currentStatus.outdoorTemp}° F",
                    color = if (currentStatus.outdoorTemp < currentStatus.indoorTemp) coldLight else warmLight
                )
            }

            Row {
                Icon(Window, contentDescription = "Window")
                TextButton(
                    onClick = {
                        if (location != null && config.isInGeofence(
                                location!!.latitude,
                                location!!.longitude
                            )
                        ) toggleWindow(scope, user, config, location, toast) else {
                            toast(Exception("You're to far from home to open the window"))
                        }
                    },
                    enabled = connectionHealth == ConnectionHealth.STABLE
                ) {
                    Text(if (isDimmed) "Window Unavailable" else "Window ${if (currentStatus.windowOpen) "Open" else "Closed"}")
                }
            }

            HomeLayoutComposable(config, connectionHealth, currentStatus)
            ForecastComposable(
                place = "indoor",
                high = highIndoor,
                low = lowIndoor,
                connectionHealth = connectionHealth,
                hourlyData = hourlyForecasts
            )
            ForecastComposable(
                place = "outdoor",
                high = highOutdoor,
                low = lowOutdoor,
                connectionHealth = connectionHealth,
                hourlyData = hourlyForecasts
            )
        }
    }
}

fun toggleWindow(
    scope: CoroutineScope,
    user: FirebaseUser,
    config: Config,
    location: LatLng?,
    toast: (Exception) -> Unit
) {
    if (location == null) {
        toast(Exception("Still loading location. Can't determine if you're in the geofence."))
        return
    }
    if (config.isInGeofence(
            location.latitude,
            location.longitude
        )
    ) scope.launch { FirebaseRepository.addRequestToToggleWindow(user.uid) } else {
        toast(Exception("You're too far from home to open the window"))
    }
}

// Add this extension function to Config
fun Config.isInGeofence(currentLat: Double, currentLng: Double): Boolean {
    val homeLat = homeLocation.first
    val homeLng = homeLocation.second

    val distance = kotlin.math.sqrt(
        (currentLat - homeLat).pow(2) + (currentLng - homeLng).pow(2)
    )
    Logger.i("Home location: $homeLat, $homeLng, current location: $currentLat, $currentLng, distance: $distance, distance threshold: $geofenceRadius")

    return distance < this.geofenceRadius
}
