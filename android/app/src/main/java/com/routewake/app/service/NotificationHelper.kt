package com.routewake.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.routewake.app.MainActivity

object NotificationHelper {
    const val CHANNEL_NAVIGATION_ID = "routewake_navigation_channel"
    const val CHANNEL_ALARM_ID = "routewake_alarm_channel"
    const val NOTIFICATION_NAVIGATION_ID = 1001
    const val NOTIFICATION_ALARM_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val navChannel = NotificationChannel(
                CHANNEL_NAVIGATION_ID,
                "RouteWake Active Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live remaining distance and travel progress during active trips"
                setShowBadge(false)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "RouteWake Arrival Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alert when your destination threshold is reached"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(navChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    fun buildNavigationNotification(
        context: Context,
        destinationName: String,
        distanceRemaining: String,
        eta: String
    ): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingLaunch = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_STOP_SERVICE
        }
        val pendingStop = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_NAVIGATION_ID)
            .setContentTitle("RouteWake — Monitoring $destinationName")
            .setContentText("$distanceRemaining remaining • ETA: $eta")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingLaunch)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Monitoring", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun buildArrivalNotification(
        context: Context,
        destinationName: String,
        reason: String
    ): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingLaunch = PendingIntent.getActivity(
            context,
            2,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_DISMISS_ALARM
        }
        val pendingStop = PendingIntent.getService(
            context,
            3,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ALARM_ID)
            .setContentTitle("RouteWake: Approaching $destinationName!")
            .setContentText(reason)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingLaunch)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm", pendingStop)
            .build()
    }
}
