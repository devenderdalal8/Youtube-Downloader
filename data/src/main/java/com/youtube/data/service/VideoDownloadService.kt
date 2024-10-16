package com.youtube.data.service

import android.app.Service
import android.content.Intent
import android.os.Environment
import com.youtube.data.service.notification.VideoNotificationManager
import com.youtube.data.util.getFileSize
import com.youtube.data.util.getUri
import com.youtube.domain.utils.Constant.BASE_URL
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.DOWNLOAD_FAILED
import com.youtube.domain.utils.Constant.DOWNLOAD_START
import com.youtube.domain.utils.Constant.DOWNLOAD_TEXT
import com.youtube.domain.utils.Constant.FAILED_TO_DOWNLOAD_VIDEO
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.domain.utils.Constant.START_BYTE
import com.youtube.domain.utils.Constant.TITLE
import com.youtube.domain.utils.Constant.VIDEO_URL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

@AndroidEntryPoint
class VideoDownloadService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        val NOTIFICATION_ID = Random.nextInt()
        private val broadcastIntent = Intent()
        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder().build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(TITLE)
        val notification = VideoNotificationManager.showNotification(
            context = this,
            title = title.toString(),
            message = DOWNLOAD_START,
            notificationId = NOTIFICATION_ID
        )
        startForeground(NOTIFICATION_ID, notification)
        intent?.let { startDownload(it, title) }
        return START_NOT_STICKY
    }

    private fun startDownload(intent: Intent, title: String?) {
        val url = intent.getStringExtra(VIDEO_URL) ?: return
        val baseUrl = intent.getStringExtra(BASE_URL) ?: return
        val downloadBytes = intent.getLongExtra(START_BYTE, 0)
        scope.launch {
            try {
                downloadVideo(
                    fileName = title.toString(),
                    url = url,
                    startByte = downloadBytes,
                    notificationId = NOTIFICATION_ID, baseUrl = baseUrl
                )
                onDownloadComplete(title.toString(), baseUrl)
            } catch (ex: Exception) {
                downloadingFailed(baseUrl)
            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun downloadingFailed(baseUrl: String) {
        withContext(Dispatchers.Main) {
            broadcastIntent.setAction(DOWNLOAD_FAILED)
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            sendBroadcast(broadcastIntent)
        }
    }

    private suspend fun downloadVideo(
        fileName: String,
        url: String,
        startByte: Long,
        baseUrl: String,
        notificationId: Int,
    ) {
        withContext(Dispatchers.IO) {
            broadcastIntent.setAction(PROGRESS_DATA)
            val resolver = contentResolver
            val uris = resolver.getUri(fileName)
            var fullPath: String? = null
            uris?.let { uri ->
                try {
                    resolver.openOutputStream(uri).use { outputStream ->
                        val request = Request.Builder().url(url).apply {
                            if (startByte > 0)
                                header("Range", "bytes=$startByte-")
                        }.build()

                        val response = okHttpClient.newCall(request).execute()
                        if (!response.isSuccessful) {
                            response.close()
                            throw Exception(FAILED_TO_DOWNLOAD_VIDEO)
                        }

                        response.body()?.let { body ->
                            val inputStream = body.byteStream()
                            val buffer = ByteArray(1024 * 8)
                            var byteRead: Int
                            var totalByteRead = startByte
                            val totalBytes = body.contentLength().takeIf { it > 0 }?.plus(startByte)
                                ?: return@withContext
                            var lastProgress = 0
                            while (inputStream.read(buffer).also { byteRead = it } != -1) {
                                outputStream?.write(buffer, 0, byteRead)
                                totalByteRead += byteRead
                                val progress = ((totalByteRead * 100) / totalBytes).toInt()
                                if (progress - lastProgress >= 5) {
                                    lastProgress = progress
                                    withContext(Dispatchers.Main) {
                                        broadcastProgress(
                                            broadcastIntent,
                                            totalByteRead,
                                            lastProgress,
                                            totalBytes.getFileSize(),
                                            baseUrl = baseUrl

                                        )
                                        updateProgress(
                                            title = fileName,
                                            notificationId = notificationId,
                                            progress = progress,
                                        )
                                    }
                                }
                            }
                            fullPath =
                                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName.mp4"
                            inputStream.close()
                        }
                        outputStream?.close()
                        response.close()
                    }
                } catch (ex: Exception) {
                    throw ex
                } finally {
                    if (fullPath == null) {
                        resolver.delete(uri, null, null)
                    }
                }
            }
        }
    }

    private suspend fun broadcastProgress(
        broadcastIntent: Intent,
        totalByteRead: Long,
        lastProgress: Int,
        fileSize: String,
        baseUrl: String
    ) {
        withContext(Dispatchers.Main) {
            broadcastIntent.putExtra(LAST_PROGRESS, lastProgress)
            broadcastIntent.putExtra(DOWNLOADED_BYTES, totalByteRead)
            broadcastIntent.putExtra(FILE_SIZE, fileSize)
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            sendBroadcast(broadcastIntent)
        }
    }

    private suspend fun updateProgress(
        progress: Int,
        title: String,
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

    private suspend fun onDownloadComplete(
        fileName: String,
        baseUrl: String
    ) {
        withContext(Dispatchers.Main) {
            showCompleteNotification(fileName)
            broadcastIntent.setAction(DOWNLOAD_COMPLETE)
            broadcastIntent.putExtra(DOWNLOAD_COMPLETE, DOWNLOAD_COMPLETE)
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            sendBroadcast(broadcastIntent)
        }
    }

    private fun showCompleteNotification(title: String) {
        val notification = VideoNotificationManager.showNotification(
            context = this,
            title = title,
            message = DOWNLOAD_TEXT,
            notificationId = NOTIFICATION_ID
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        job.cancel()
    }

}
