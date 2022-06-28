package com.criticalgnome.notificationspermissiondemo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.criticalgnome.notificationspermissiondemo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow.
            Snackbar
                .make(binding.root, "Notification allowed", Snackbar.LENGTH_SHORT)
                .show()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
            Snackbar
                .make(binding.root, "Notification blocked", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        createChannel(CHANNEL_1_ID, CHANNEL_1_NAME, CHANNEL_1_DESCRIPTION)
        createChannel(CHANNEL_2_ID, CHANNEL_2_NAME, CHANNEL_2_DESCRIPTION)

        binding.notificationTriggerButton.setOnClickListener {
            sendNotification(
                notificationId = NOTIFICATION_1_ID,
                channelId = CHANNEL_1_ID,
                title = NOTIFICATION_1_TITLE,
                message = NOTIFICATION_MESSAGE
            )
            sendNotification(
                notificationId = NOTIFICATION_2_ID,
                channelId = CHANNEL_2_ID,
                title = NOTIFICATION_2_TITLE,
                message = NOTIFICATION_MESSAGE
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("NotificationsPermissionDemo", "Notifications allowed")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Log.d("NotificationsPermissionDemo", "Notifications blocked")
                Snackbar.make(
                    binding.root,
                    "Notification blocked",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Settings") {
                    // Responds to click on the action
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.show()
            }
            else -> {
                // The registered ActivityResultCallback gets the result of this request
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createChannel(
        id: String,
        name: String,
        description: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        notificationManager.createNotificationChannel(
            NotificationChannel(id, name, importance).apply { this.description = description }
        )
    }

    private fun Context.sendNotification(
        notificationId: Int,
        channelId: String,
        @DrawableRes iconId: Int = R.drawable.ic_baseline_chat_bubble_24,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) = with(NotificationManagerCompat.from(this)) {
        notify(
            notificationId,
            NotificationCompat.Builder(this@sendNotification, channelId)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .build())
    }

    companion object {
        private const val CHANNEL_1_ID = "MyChannel1"
        private const val CHANNEL_2_ID = "MyChannel2"
        private const val CHANNEL_1_NAME = "Notification channel 1"
        private const val CHANNEL_2_NAME = "Notification channel 2"
        private const val CHANNEL_1_DESCRIPTION = "Description for first notification channel"
        private const val CHANNEL_2_DESCRIPTION = "Description for second notification channel"
        private const val NOTIFICATION_1_ID = 1001
        private const val NOTIFICATION_2_ID = 1002
        private const val NOTIFICATION_1_TITLE = "Notification 1 title"
        private const val NOTIFICATION_2_TITLE = "Notification 2 title"
        private const val NOTIFICATION_MESSAGE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer vehicula, ante id luctus fringilla, nunc velit condimentum massa, non finibus."
    }
}
