package com.youtube.data.service.workManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.youtube.data.R
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@AndroidEntryPoint
class VideoDownloadService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var localDataRepository: VideoLocalDataRepository

    private val broadcastIntent = Intent()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { startDownload(it) }
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startDownload(intent: Intent) {
        val url = intent.getStringExtra("url") ?: return
        val baseUrl = intent.getStringExtra("baseUrl") ?: return
        val fileName = intent.getStringExtra("fileName") ?: return
        val downloadBytes = intent.getLongExtra("downloadedBytes", 0)
        startForeground(1, createForegroundInfo(0, fileName).notification)
        scope.launch {
            val video = async { localDataRepository.videoByBaseUrl(baseUrl) }.await()
            try {
                downloadVideo(fileName, url, downloadBytes, baseUrl, video)
                onDownloadComplete(fileName, video)
            } catch (ex: Exception) {
                Log.e("TAG", "Download failed: ${ex.message}")
                downloadingFailed(video)
            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun downloadingFailed(video: LocalVideo) {
        withContext(Dispatchers.IO) {
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    state = DownloadState.FAILED
                )
            )
            localDataRepository.update(video = video)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun downloadVideo(
        fileName: String,
        url: String,
        startByte: Long,
        baseUrl: String,
        video: LocalVideo,
    ) {
        withContext(Dispatchers.IO) {
            broadcastIntent.setAction(PROGRESS_DATA)
            val resolver = contentResolver
            val contentValue = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uris = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValue)
            var fullPath: String? = null

            uris?.let { uri ->
                try {
                    resolver.openOutputStream(uri).use { outputStream ->
                        val request = Request.Builder().url(url).apply {
                            if (startByte > 0) header("Range", "bytes=$startByte-")
                        }.build()

                        val response = okHttpClient.newCall(request).execute()
                        if (!response.isSuccessful) {
                            throw Exception("Failed to download video")
                        }

                        response.body?.let { body ->
                            val inputStream = body.byteStream()
                            val buffer = ByteArray(1024 * 8)
                            var byteRead: Int
                            var totalByteRead = startByte
                            val totalBytes = body.contentLength().takeIf { it > 0 }?.plus(startByte)
                                ?: return@withContext
                            video.copy(
                                downloadProgress = video.downloadProgress.copy(
                                    totalBytes = totalBytes,
                                    uri = uri.toString(),
                                    totalMegaBytes = totalBytes.getFileSize()
                                )
                            )
                            localDataRepository.update(video)
                            var lastProgress = 0
                            while (inputStream.read(buffer).also { byteRead = it } != -1) {
                                outputStream?.write(buffer, 0, byteRead)
                                totalByteRead += byteRead
                                val progress = ((totalByteRead * 100) / totalBytes).toInt()
                                if (progress - lastProgress >= 5) {
                                    lastProgress = progress
                                    broadcastProgress(
                                        broadcastIntent,
                                        totalByteRead,
                                        lastProgress,
                                        totalBytes.getFileSize()
                                    )
                                    updateProgress(progress, video, totalByteRead)
                                    startForeground(
                                        1, createForegroundInfo(progress, fileName).notification
                                    )
                                }
                            }
                            outputStream?.close()
                            inputStream.close()
                            fullPath =
                                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName.mp4"
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("DownloadError", "Failed to download video: ${ex.message}")
                    throw ex
                } finally {
                    if (fullPath == null) {
                        resolver.delete(uri, null, null)
                        launch(Dispatchers.IO) { localDataRepository.delete(video = video) }
                    }
                }
            }
        }
    }

    private fun broadcastProgress(
        broadcastIntent: Intent,
        totalByteRead: Long,
        lastProgress: Int,
        fileSize: String
    ) {
        broadcastIntent.putExtra(LAST_PROGRESS, lastProgress.toString())
        broadcastIntent.putExtra(DOWNLOADED_BYTES, totalByteRead)
        broadcastIntent.putExtra(FILE_SIZE, fileSize)
        sendBroadcast(broadcastIntent)
    }

    private suspend fun updateProgress(percentage: Int, video: LocalVideo, totalByteRead: Long) {
        withContext(Dispatchers.IO) {
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    bytesDownloaded = totalByteRead,
                    percentage = percentage,
                    megaBytesDownloaded = totalByteRead.getFileSize()
                )
            ).also { updatedVideo ->
                localDataRepository.update(video = updatedVideo)
            }
        }
    }

    private fun createForegroundInfo(progress: Int, fileName: String = ""): ForegroundInfo {
        val notificationManager =
            getSystemService(NotificationManager::class.java) ?: return ForegroundInfo(
                0, NotificationCompat.Builder(this, "").build()
            )
        val channelId = "download_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Video Download", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId).setContentTitle(fileName)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setProgress(100, progress, false)
            .build()

        return ForegroundInfo(1, notification)
    }

    private suspend fun onDownloadComplete(
        fileName: String,
        video: LocalVideo
    ) {
        withContext(Dispatchers.IO) {
            showCompleteNotification(fileName)
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    state = DownloadState.COMPLETED
                )
            )
            localDataRepository.update(video)

            broadcastIntent.setAction(DOWNLOAD_COMPLETE)
            broadcastIntent.putExtra(DOWNLOAD_COMPLETE, DOWNLOAD_COMPLETE)
            sendBroadcast(broadcastIntent)
        }
    }

    private fun showCompleteNotification(fileName: String) {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        val channelId = "download_complete_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Download Complete", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification =
            NotificationCompat.Builder(this, channelId).setContentTitle("$fileName Downloaded")
                .setContentText("The file has been downloaded successfully.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()

        notificationManager.notify(2, notification) // Display the notification
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?) = null

    data class ForegroundInfo(val id: Int, val notification: Notification)

    private suspend fun Long.getFileSize(): String {
        return withContext(Dispatchers.IO) {
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var fileSize = this@getFileSize.toDouble()
            var unitIndex = 0
            while (fileSize >= 1024 && unitIndex < units.size - 1) {
                fileSize /= 1024
                unitIndex++
            }
            "%.2f %s".format(fileSize, units[unitIndex])
        }
    }
}
