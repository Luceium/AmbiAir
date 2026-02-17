package tech.luceium.ambiair.data

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object FirebaseRepository {
    private val firestore = Firebase.firestore

    val currentStatusFlow: Flow<CurrentData> =
        firestore.collection("current_data")
            .snapshots
            .map { snapshot ->
                val document = snapshot.documents.first()
                CurrentData(
                    indoorTemp  = document.get<Float>("indoor-temp"),
                    outdoorTemp = document.get<Float>("outdoor-temp"),
                    windowOpen  = document.get<Boolean>("window-open")
                ).also {
                    Logger.i("current_data → $it", null, "FirebaseRepository")
                }
            }
            .also { Logger.i("Current data flow created", null, "FirebaseRepository") }

    val random = Random(42)
    // minutes since jan 1st
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfYear * 24 * 60
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val hourlyForecasts: Flow<List<SensorDataEntry>> = flowOf((0..hour).map {
        SensorDataEntry((70f + sin(it * PI / 24) * 10 + random.nextDouble(2.5)).toFloat(),
            (75f + sin(it * PI / 24) * 10 + random.nextDouble(2.5)).toFloat(), today + it * 60)
    }.also { Logger.i(it.toString(), null, "DATA") })
    // WE EXCEEDED OUR FIREBASE QUOTA
//        firestore.collection("periodic_data")
//            .snapshots
//            .map { snapshot ->
//                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
//                val todayStartMinute = (now.date.dayOfYear - 1) * 24 * 60
//                val todayEndMinute = todayStartMinute + 24 * 60
//                val currentHour = now.hour
//
//                val allData = snapshot.documents.mapNotNull { doc ->
//                    val minute = doc.get<Int>("time")
//                    if (minute in todayStartMinute until todayEndMinute) {
//                        SensorDataEntry(
//                            indoorTemp  = doc.get<Float>("indoor-temp"),
//                            outdoorTemp = doc.get<Float>("outdoor-temp"),
//                            minute      = minute
//                        )
//                    } else null
//                }
//
//                val hourly = (0 until currentHour).map { hour ->
//                        val entry = allData.filter { (it.minute - todayStartMinute) / 60 == hour }
//                            .minByOrNull { it.minute }
//                        entry ?: SensorDataEntry(0f, 0f, todayStartMinute + hour * 60)
//                }
//                Logger.i("hourly forecasts → $hourly", null, "FirebaseRepository")
//                hourly
//            }
//            .distinctUntilChanged()
//            .also { Logger.i("Hourly forecasts flow created", null, "FirebaseRepository") }

    /**
     * This function checks for the existence of a config file on firebase.
     * New users who haven't completed the setup will not have a config file.
     */
    suspend fun getConfig(uid: String): Config? {
        val configRef = firestore.collection("users").document(uid)
        val config = configRef.get()
        Logger.i("Config snapshot: $config", null, "FirebaseRepository")

        return if (config.exists) try {
            config.data<Config>(Config.serializer())
        } catch (e: Exception) {
            Logger.e("Error deserializing config (Probably a result of the config structure changing). Returning a null config instead.\nError ${e}")
            null
        } else null
    }

    suspend fun setConfig(uid: String, config: Config) {
        val configRef = firestore.collection("users").document(uid)
        configRef.set(config)
    }

    /**
     * Create a window event request in a dedicated Message Queue document.
     *
     * A user can only have one request at a time.
     */
    suspend fun addRequestToToggleWindow(uid: String) {
        val requestRef = firestore.collection("window_requests").document(uid)
        // Check if the user already has a request
        if (requestRef.get().exists) {
            Logger.e("User $uid already has a request. Cannot add request to toggle window.")
            return
        }

        // Set with a timestamp
        requestRef.set(
            WindowRequest(Clock.System.now().toString())
        )
    }
}