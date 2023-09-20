package com.example.afinal.services

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.afinal.MainActivity
import com.example.afinal.R

class BackgroundService : Service()  {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Create a notification to run the service in the foreground
        val notification = createNotification()
//       startForeground(FOREGROUND_SERVICE_ID, notification)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "location_tracking_channel"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE  // Remove the FLAG_MUTABLE flag
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Service")
            .setContentText("Tracking your location in the background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(
                NotificationCompat.PRIORITY_HIGH)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Location Tracking Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                return notificationBuilder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val FOREGROUND_SERVICE_ID = 1588
    }
}

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}