package tech.luceium.ambiair

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessaging
import tech.luceium.ambiair.MyFirebaseMessagingService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import tech.luceium.ambiair.ui.theme.AmbiAirTheme

class MainActivity : FragmentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // FCM SDK (and your app) can post notifications.
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
                // Subscribe to the topic when permission is granted
                val topic = "window_events"
                Logger.i { "Attempting to subscribe to FCM topic: $topic on Android." }
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Logger.i { "Successfully subscribed to '$topic' topic on Android." }
                        } else {
                            Logger.w(task.exception) { "Failed to subscribe to '$topic' topic on Android." }
                        }
                    }
            } else {
                // Inform user that that your app will not show notifications.
                Toast.makeText(this, "Notifications disabled. You can enable them in app settings.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel() // Create channel on app start
        askNotificationPermission() // Ask for permission on app start

        FileKit.init(this)

        setContent {
            AmbiAirTheme {
                App()
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only needed for API level 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Display an educational UI explaining to the user the features that will be enabled
                // by granting the POST_NOTIFICATION permission.
                AlertDialog.Builder(this)
                    .setTitle("Notification Permission Needed")
                    .setMessage("To receive important updates and alerts (e.g., window open, window closed), please grant notification permission.")
                    .setPositiveButton("Ask Again") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton("Not Now") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this, "Notifications will remain off unless enabled in settings.", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.default_notification_channel_name) // You need to add this string resource
            val descriptionText = getString(R.string.default_notification_channel_description) // You need to add this string resource
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(MyFirebaseMessagingService.DEFAULT_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AmbiAirTheme {
        App()
    }
}
