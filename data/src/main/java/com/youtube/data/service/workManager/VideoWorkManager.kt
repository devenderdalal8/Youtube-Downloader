package com.youtube.data.service.workManager

import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.youtube.data.service.notification.VideoNotificationManager
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
import com.youtube.domain.utils.Constant.URI
import com.youtube.domain.utils.Constant.VIDEO_ID
import com.youtube.domain.utils.Constant.VIDEO_NOTIFICATION_ID
import com.youtube.domain.utils.Constant.VIDEO_URL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.random.Random

@HiltWorker
class VideoWorkManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        private val broadcastIntent = Intent()
        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder().build()
        }
    }

    override suspend fun doWork(): Result {
        val notificationId = inputData.getInt(VIDEO_NOTIFICATION_ID, Random.nextInt())
        val title = inputData.getString(TITLE) ?: Result.failure()
        val url = inputData.getString(VIDEO_URL) ?: return Result.failure()
        val baseUrl = inputData.getString(BASE_URL) ?: return Result.failure()
        val downloadBytes = inputData.getLong(START_BYTE, 0)
        val videoId = inputData.getString(VIDEO_ID) ?: return Result.failure()

        val notification = VideoNotificationManager.showNotification(
            context = applicationContext,
            title = title.toString(),
            message = DOWNLOAD_START,
            notificationId = notificationId,
            notificationManager = notificationManager
        )

        setForeground(notification)

        return try {
            downloadVideo(
                title = title.toString(),
                url = url,
                startByte = downloadBytes,
                notificationId = notificationId,
                baseUrl = baseUrl,
                videoId = videoId
            )
            Result.success()
        } catch (e: Exception) {
            Log.e("VideoDownloadService", "Download failed", e)
            downloadingFailed(baseUrl)
            Result.retry()
        }
    }

    private suspend fun downloadVideo(
        title: String,
        url: String,
        startByte: Long,
        baseUrl: String,
        notificationId: Int,
        videoId: String
    ) = withContext(Dispatchers.IO) {
        broadcastIntent.action = PROGRESS_DATA
        var fullPath: String? = null
        val resolver = applicationContext.contentResolver
        var uri: Uri? = null
        try {
            val contentValue = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$title.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValue)
            } else {
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, title)

                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                Uri.fromFile(file)
            }
            uris?.let { videoUri ->
                uri = videoUri
                Log.e("TAG", "downloadVideo:Path ${videoUri.path} \n URI : $videoUri \n ${videoUri.isAbsolute}", )
                resolver.openOutputStream(videoUri)?.use { outputStream ->
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
                                        fileSize = totalBytes,
                                        baseUrl = baseUrl,
                                        videoId = videoId,
                                        uri = videoUri
                                    )
                                    updateProgress(
                                        title = title,
                                        progress = progress,
                                        notificationId = notificationId,
                                    )
                                }
                            }
                        }
                        fullPath =
                            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$title.mp4"
                        onDownloadComplete(
                            title,
                            baseUrl,
                            notificationId,
                            filePath = fullPath.toString()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VideoDownloadService", "Error during download ${e.message}")
        } finally {
            if (fullPath == null) {
                uri?.let { resolver.delete(it, null, null) }
            }
        }
    }

    private suspend fun broadcastProgress(
        totalBytesRead: Long,
        lastProgress: Int,
        fileSize: Long,
        baseUrl: String,
        videoId: String,
        uri: Uri
    ) {
        withContext(Dispatchers.Main) {
            broadcastIntent.putExtra(LAST_PROGRESS, lastProgress)
            broadcastIntent.putExtra(DOWNLOADED_BYTES, totalBytesRead)
            broadcastIntent.putExtra(FILE_SIZE, fileSize)
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            broadcastIntent.putExtra(VIDEO_ID, videoId)
            broadcastIntent.putExtra(URI, uri.toString())
            applicationContext.sendBroadcast(broadcastIntent)
        }
    }

    private suspend fun updateProgress(
        title: String, progress: Int, notificationId: Int
    ) {
        val notification = VideoNotificationManager.showProgressNotification(
            context = applicationContext,
            title = title,
            message = NOTHING,
            notificationId = notificationId,
            progress = progress,
            notificationManager = notificationManager
        )
        setForeground(notification)
    }

    private suspend fun onDownloadComplete(
        title: String,
        baseUrl: String,
        notificationId: Int,
        filePath: String
    ) {
        showCompleteNotification(title, notificationId)
        broadcastIntent.action = DOWNLOAD_COMPLETE
        broadcastIntent.putExtra(BASE_URL, baseUrl)
        broadcastIntent.putExtra(FILE_PATH, filePath)
        applicationContext.sendBroadcast(broadcastIntent)

    }

    private suspend fun showCompleteNotification(title: String, notificationId: Int) {
        val notification = VideoNotificationManager.showNotification(
            context = applicationContext,
            title = title,
            message = DOWNLOAD_TEXT,
            notificationId = notificationId,
            notificationManager = notificationManager
        )
        setForeground(notification)
    }

    private fun downloadingFailed(baseUrl: String) {
        broadcastIntent.action = DOWNLOAD_FAILED
        broadcastIntent.putExtra(BASE_URL, baseUrl)
        applicationContext.sendBroadcast(broadcastIntent)
    }

}
