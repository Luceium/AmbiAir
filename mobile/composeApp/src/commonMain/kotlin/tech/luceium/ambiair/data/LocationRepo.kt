package tech.luceium.ambiair.data

import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class LocationRepository(
    private val permissionsController: PermissionsController,
    private val locationTracker: LocationTracker
) {
    private val _permissionState = MutableStateFlow(PermissionState.NotDetermined)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    val location: Flow<LatLng> = locationTracker.getLocationsFlow()
        .distinctUntilChanged()

    suspend fun initialize() {
        _permissionState.value = permissionsController.getPermissionState(Permission.LOCATION)
        if (_permissionState.value == PermissionState.Granted) {
            locationTracker.startTracking()
        }
    }

    suspend fun requestLocation() {
        try {
            permissionsController.providePermission(Permission.LOCATION)
            _permissionState.value = PermissionState.Granted
            locationTracker.startTracking()
        } catch (e: DeniedAlwaysException) {
            _permissionState.value = PermissionState.DeniedAlways
        } catch (e: DeniedException) {
            _permissionState.value = PermissionState.Denied
        }
    }

    suspend fun stopTracking() {
        locationTracker.stopTracking()
    }

    companion object {
        private var instance: LocationRepository? = null

        fun getInstance(
            permissionsController: PermissionsController,
            locationTracker: LocationTracker
        ): LocationRepository {
            return instance ?: LocationRepository(permissionsController, locationTracker).also {
                instance = it
            }
        }
    }
}
