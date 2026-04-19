package com.example.finalemobile2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val appContext: Context) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ENVIRONMENT_ALERT_CHANNEL"
        private const val NOTIFICATION_CHANNEL_NAME = "Environmental Alerts"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for ecological thresholds exceeded"

        /**
         * Creates a notification channel for Android 8.0+ (API 26 and above).
         */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    importance
                ).apply {
                    description = NOTIFICATION_CHANNEL_DESCRIPTION
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        /**
         * Checks and requests notification permission (Android 13+).
         */
        fun requestNotificationPermissionIfNeeded(activity: android.app.Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    /**
     * Sends a notification if the necessary permissions are granted.
     */
    fun displayNotification(notificationTitle: String, notificationMessage: String) {
        // Check notification permissions for Android 13+ (API 33).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("POST_NOTIFICATIONS permission not granted. Notification not sent.")
            return
        }

        // Build the notification.
        val notificationBuilder = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists in res/drawable.
            .setContentTitle(notificationTitle)
            .setContentText(notificationMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationMessage)) // Allow for a longer message.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Display the notification.
        with(NotificationManagerCompat.from(appContext)) {
            notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}
