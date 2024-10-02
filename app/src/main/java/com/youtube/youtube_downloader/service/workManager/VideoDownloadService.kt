package com.youtube.youtube_downloader.service.workManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class VideoDownloadService : Service() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val filePath = intent.getStringExtra("filePath") ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra("name") ?: return START_NOT_STICKY

        startForeground(1, createNotification(0, fileName))

        serviceScope.launch {
            downloadVideo(url, filePath,fileName)
        }

        return START_NOT_STICKY
    }

    private suspend fun downloadVideo(url: String, filePath: String, name: String) {
        withContext(Dispatchers.IO) {
            val outputFile = File(filePath)
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            response.body?.let { body ->
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(1024 * 8)
                var byteRead: Int

                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                while (inputStream.read(buffer).also { byteRead = it } != -1) {
                    outputStream.write(buffer, 0, byteRead)
                    downloadedBytes += byteRead
                    updateNotification((downloadedBytes * 100 / totalBytes).toInt() , name)
                }

                outputStream.close()
                inputStream.close()
            }

            stopSelf()
        }
    }

    private fun createNotification(progress: Int, fileName: String): Notification {
        val channelId = "download_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Download", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(fileName)
            .setProgress(100, progress, false)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }

    private fun updateNotification(progress: Int, name: String) {
        val notification = createNotification(progress, name)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancel()
    }
}
