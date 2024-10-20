package com.youtube.data.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.youtube.data.R

object VideoNotificationManager {
    private const val DEFAULT_CHANNEL_ID = "default_channel_id"
    private const val DEFAULT_CHANNEL_NAME = "General Notifications"

    fun showNotification(
        context: Context,
        title: String,
        message: String, notificationId: Int = 1, notificationManager: NotificationManager
    ): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        return ForegroundInfo(notificationId, notification)
    }

    fun showProgressNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 1,
        progress: Int, notificationManager: NotificationManager
    ): ForegroundInfo {
        Log.d("TAG", "showProgressNotification:  title: $title \n $notificationId")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_file_download_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setProgress(100, progress, false)
            .build()
        return ForegroundInfo(notificationId, notification)
    }

}