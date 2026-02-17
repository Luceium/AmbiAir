package tech.luceium.ambiair.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.LocationRepository
import tech.luceium.ambiair.services.geofenceURLBuilder
import tech.luceium.ambiair.ui.theme.AmbiAirTypography

@Composable
fun HomeLocationScreen(config: Config, scope: CoroutineScope, onDone: () -> Unit) {

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

    var geofenceRadius by remember { mutableStateOf(75f) }
    val location by locationRepo.location.collectAsState(initial = null)
    val permissionState by locationRepo.permissionState.collectAsState()

    var imageURL by remember { mutableStateOf("") }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    // Initialize location tracking when the screen is first shown
    LaunchedEffect(Unit) {
        locationRepo.initialize()
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Set up",
            style = AmbiAirTypography().displayLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
        )
        Text(
            "Geofence\nConfirm your location and set the geofence radius",
            style = AmbiAirTypography().titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        when (permissionState) {
            PermissionState.NotDetermined -> {
                Text("Requesting location permission...")
                scope.launch { locationRepo.requestLocation() }
            }

            PermissionState.Granted -> {
                Box (Modifier.fillMaxWidth().weight(1f).onSizeChanged { imageSize = it }.background(
                    Color.LightGray), contentAlignment = Alignment.Center) {
                    Text("Please preview your geofence before continuing", modifier = Modifier.padding(16.dp))
                    AsyncImage(
                        model = imageURL,
                        contentDescription = "Geofence preview"
                    )
                }
                Button(
                    onClick = { imageURL = geofenceURLBuilder(location!!, geofenceRadius, imageSize.width, imageSize.height) },
                    enabled = location != null && geofenceRadius > 0f
                ) { Text("Preview Geofence") }

                Text("Geofence Radius: ${geofenceRadius.toInt()}ft")
                Slider(
                    value = geofenceRadius,
                    onValueChange = { geofenceRadius = it },
                    valueRange = 50f..300f,
                    steps = 50
                )


                Spacer(modifier = Modifier.height(16.dp))
            }

            PermissionState.NotDetermined -> {
                TextButton({ scope.launch { locationRepo.requestLocation() } }) {
                    Text("Location permission is required. Click to enable.")
                }
            }

            PermissionState.DeniedAlways -> {
                Text("Location permission permanently denied. Please enable in settings.")
                Button({ permissionsController.openAppSettings() }) {
                    Text("Open Settings")
                }
            }

            else -> {
                TextButton({ scope.launch { locationRepo.requestLocation() } }) {
                    Text("Location permission is needed to set your geofence. Allow location usage")
                }
            }
        }

        Button(
            onClick = {
                config.homeLocation = location!!.toPair()
                config.geofenceRadius = geofenceRadius.toDouble()
                onDone()
            },
            enabled = location != null && imageURL != ""
        ) {
            Text("Done")
        }
    }

    LinearProgressIndicator(
        progress = { 6f / 6f },
        modifier = Modifier.fillMaxWidth()
    )
}

fun LatLng.toPair(): Pair<Double, Double> {
    return Pair(latitude, longitude)
}
