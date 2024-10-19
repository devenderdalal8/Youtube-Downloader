package com.youtube.data.service

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.youtube.data.service.notification.VideoNotificationManager
import com.youtube.data.util.getFileSize
import com.youtube.data.util.getUri
import com.youtube.domain.utils.Constant.BASE_URL
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.DOWNLOAD_FAILED
import com.youtube.domain.utils.Constant.DOWNLOAD_START
import com.youtube.domain.utils.Constant.DOWNLOAD_TEXT
import com.youtube.domain.utils.Constant.FILE_PATH
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.domain.utils.Constant.START_BYTE
import com.youtube.domain.utils.Constant.TITLE
import com.youtube.domain.utils.Constant.VIDEO_ID
import com.youtube.domain.utils.Constant.VIDEO_NOTIFICATION_ID
import com.youtube.domain.utils.Constant.VIDEO_URL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

@AndroidEntryPoint
class VideoDownloadService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job) // Start on Main
    private var isForegroundStarted = false

    private var downloadJob: Job? = null

    companion object {
        val NOTIFICATION_ID = Random.nextInt()
        private val broadcastIntent = Intent()
        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder().build()
        }
    }


    override fun onCreate() {
        super.onCreate()
        Log.d("VideoDownloadService", "Service created")
        val notification = VideoNotificationManager.showNotification(
            context = this,
            title = "",
            message = DOWNLOAD_START,
            notificationId = NOTIFICATION_ID
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(TITLE) ?: "Unknown"
        val notificationId =
            intent?.getIntExtra(VIDEO_NOTIFICATION_ID, NOTIFICATION_ID) ?: NOTIFICATION_ID
        val notification = VideoNotificationManager.showNotification(
            context = this,
            title = title,
            message = DOWNLOAD_START,
            notificationId = NOTIFICATION_ID
        )
        startForeground(notificationId, notification)
        intent?.let { startDownload(it, title, notificationId) }
        return super.onStartCommand(intent, flags, startId);
    }

    private fun startDownload(intent: Intent, title: String?, notificationId: Int) {
        val url = intent.getStringExtra(VIDEO_URL) ?: return
        val baseUrl = intent.getStringExtra(BASE_URL) ?: return
        val downloadBytes = intent.getLongExtra(START_BYTE, 0)
        val videoId = intent.getStringExtra(VIDEO_ID)

        downloadJob?.cancel() // Cancel previous job if any

        downloadJob = scope.launch {
            try {
                downloadVideo(
                    fileName = title ?: "default_name",
                    url = url,
                    startByte = downloadBytes,
                    notificationId = notificationId,
                    baseUrl = baseUrl,
                    videoId = videoId.toString()
                )
                onDownloadComplete(title ?: "Unknown", baseUrl, notificationId)
            } catch (e: Exception) {
                Log.e("VideoDownloadService", "Download failed", e)
                downloadingFailed(baseUrl)
            } finally {
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }
    }

    private fun downloadingFailed(baseUrl: String) {
        broadcastIntent.action = DOWNLOAD_FAILED
        broadcastIntent.putExtra(BASE_URL, baseUrl)
        sendBroadcast(broadcastIntent)
    }

    private suspend fun downloadVideo(
        fileName: String,
        url: String,
        startByte: Long,
        baseUrl: String,
        notificationId: Int,
        videoId:String
    ) = withContext(Dispatchers.IO) {
        broadcastIntent.action = PROGRESS_DATA

        val resolver = contentResolver
        val uri = resolver.getUri(fileName) ?: return@withContext
        var filePath: String? = null
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val request = Request.Builder().url(url).apply {
                    if (startByte > 0) header("Range", "bytes=$startByte-")
                }.build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    response.close()
                    downloadingFailed(baseUrl)
                    return@withContext
                }

                response.body()?.use { body ->
                    val inputStream = body.byteStream()
                    val buffer = ByteArray(1024 * 8)
                    var bytesRead: Int
                    var totalBytesRead = startByte
                    val totalBytes = body.contentLength() + startByte
                    var lastProgress = 0

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        val progress = ((totalBytesRead * 100) / totalBytes).toInt()
                        if (progress - lastProgress >= 5) {
                            lastProgress = progress
                            withContext(Dispatchers.Main) {
                                broadcastProgress(
                                    totalBytesRead = totalBytesRead,
                                    lastProgress = progress,
                                    fileSize = totalBytes.getFileSize(),
                                    baseUrl = baseUrl,
                                    filePath = filePath.toString(),
                                    videoId = videoId
                                )
                                updateProgress(
                                    title = fileName,
                                    progress = progress,
                                    notificationId = notificationId,
                                )
                            }
                        }
                    }
                    filePath =
                        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName.mp4"
                }
            }
        } catch (e: Exception) {
            Log.e("VideoDownloadService", "Error during download", e)
            throw e
        } finally {
            if (filePath == null) {
                resolver.delete(uri, null, null)
            }
        }
    }

    private suspend fun broadcastProgress(
        totalBytesRead: Long,
        lastProgress: Int,
        fileSize: String,
        baseUrl: String,
        filePath: String,
        videoId: String
    ) {
        withContext(Dispatchers.Main) {
            broadcastIntent.putExtra(LAST_PROGRESS, lastProgress)
            broadcastIntent.putExtra(DOWNLOADED_BYTES, totalBytesRead)
            broadcastIntent.putExtra(FILE_SIZE, fileSize)
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            broadcastIntent.putExtra(FILE_PATH, filePath)
            broadcastIntent.putExtra(VIDEO_ID, videoId)
            sendBroadcast(broadcastIntent)
        }
    }

    private suspend fun updateProgress(
        title: String,
        progress: Int,
        notificationId: Int
    ) {
        withContext(Dispatchers.Main) {
            val notification = VideoNotificationManager.showProgressNotification(
                context = this@VideoDownloadService,
                title = title,
                message = NOTHING,
                notificationId = notificationId,
                progress = progress
            )
            startForeground(notificationId, notification)
        }
    }

    private suspend fun onDownloadComplete(title: String, baseUrl: String, notificationId: Int) {
        withContext(Dispatchers.Main) {
            showCompleteNotification(title, notificationId)
            broadcastIntent.action = DOWNLOAD_COMPLETE
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            sendBroadcast(broadcastIntent)
        }
    }

    private fun showCompleteNotification(title: String, notificationId: Int) {
        val notification = VideoNotificationManager.showNotification(
            context = this, title = title, message = DOWNLOAD_TEXT, notificationId = NOTIFICATION_ID
        )
        startForeground(notificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("VideoDownloadService", "Service Destroy")
        stopForeground(STOP_FOREGROUND_DETACH)
        downloadJob?.cancel()
        job.cancel() // Cancel the parent job to prevent leaks
    }

    override fun onBind(intent: Intent?) = null
}
