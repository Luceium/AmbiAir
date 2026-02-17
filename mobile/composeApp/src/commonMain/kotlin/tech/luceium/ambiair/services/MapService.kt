package tech.luceium.ambiair.services

import kotlin.math.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import dev.icerock.moko.geo.LatLng
import io.ktor.http.encodeURLPathPart
import tech.luceium.ambiair.data.AppSecrets

val json = Json { encodeDefaults = true }

fun geofenceURLBuilder(latLng: LatLng, radius: Float, width: Int = 300, height: Int = 400): String {
    val style = "satellite-streets-v12"
    val geoJson = generateGeoJsonCircle(latLng.latitude, latLng.longitude, radius.toDouble()).let {
        json.encodeToString(it).encodeURLPathPart()
    }
    val secret = AppSecrets.mapbox
    val url = "https://api.mapbox.com/styles/v1/mapbox/$style/static/geojson($geoJson)/auto/${width}x$height?access_token=$secret"
    return url
}


@Serializable
data class GeoJsonFeatureCollection(
    val features: List<GeoJsonFeature>,
    val type: String = "FeatureCollection"
)

@Serializable
data class GeoJsonFeature(
    val geometry: GeoJsonGeometry,
    val properties: GeoJsonProperties,
    val type: String = "Feature"
)

@Serializable
data class GeoJsonGeometry(
    val coordinates: List<List<List<Double>>>,
    val type: String = "Polygon"
)

@Serializable
data class GeoJsonProperties(val name: String = "Geofence", val fill: String = "#33aaff")

private const val EARTH_RADIUS_METERS = 6371000

/**
 * Generates a GeoJSON FeatureCollection representing a circle as a Polygon.
 *
 * @param latitude The latitude of the circle's center.
 * @param longitude The longitude of the circle's center.
 * @param radiusInFeet The radius of the circle in feet.
 * @param points The number of points to use for the circle approximation (default: 32).
 * @return A GeoJsonFeatureCollection.
 */
fun generateGeoJsonCircle(
    latitude: Double,
    longitude: Double,
    radiusInFeet: Double,
    points: Int = 32,
): GeoJsonFeatureCollection {
    val radius = radiusInFeet * 0.3048 // Convert feet to meters
    val centerLatRad = latitude * PI / 180.0
    val centerLonRad = longitude * PI / 180.0

    val coordinates = (points downTo 0).map { i ->
        val angle = i * 2 * PI / points
        val latRad = asin(sin(centerLatRad) * cos(radius / EARTH_RADIUS_METERS) +
                cos(centerLatRad) * sin(radius / EARTH_RADIUS_METERS) * cos(angle))
        val lonRad = centerLonRad + atan2(sin(angle) * sin(radius / EARTH_RADIUS_METERS) * cos(centerLatRad),
            cos(radius / EARTH_RADIUS_METERS) - sin(centerLatRad) * sin(latRad))
        listOf(lonRad * 180.0 / PI, latRad * 180.0 / PI).map { it.toXDecimals(6) }
    }

    val geometry = GeoJsonGeometry(listOf(coordinates))
    val properties = GeoJsonProperties()
    val feature = GeoJsonFeature(geometry, properties)
    return GeoJsonFeatureCollection(listOf(feature))
}

fun Double.toXDecimals(n: Int): Double {
    var multiplier = 1.0
    repeat(n) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}