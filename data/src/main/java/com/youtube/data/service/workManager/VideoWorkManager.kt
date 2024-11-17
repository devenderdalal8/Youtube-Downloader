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
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.youtube.data.service.notification.VideoNotificationManager
import com.youtube.data.util.getFileSize
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.repository.VideoLocalDataRepository
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

@HiltWorker
class VideoWorkManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private var localDataRepository: VideoLocalDataRepository
) : CoroutineWorker(context, workerParams) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var downloadedVideo: Video? = null
    private val updateScope = CoroutineScope(Dispatchers.IO)
    private var updateJob: Job? = null
    private var downloadBytes: Long = 0L

    companion object {
        private val broadcastIntent = Intent()
        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder().build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

    @RequiresApi(Build.VERSION_CODES.Q)
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
            val existingUri = resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                null,
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                arrayOf("$title.mp4"),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                    if (idIndex != -1) {
                        Uri.withAppendedPath(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            cursor.getString(idIndex)
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            uri = existingUri ?: resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValue
            )
            uri?.let { videoUri ->
                Log.e(
                    "TAG",
                    "downloadVideo:Path ${videoUri.path} \n URI : $videoUri \n ${videoUri.isAbsolute}"
                )
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
                                        uri = uri
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
            updateProgressInLocal(
                baseUrl = baseUrl,
                progress = lastProgress,
                downloadByte = totalBytesRead,
                fileSize = fileSize,
                uri = uri.toString()
            )
            broadcastIntent.putExtra(LAST_PROGRESS, lastProgress)
            broadcastIntent.putExtra(DOWNLOADED_BYTES, totalBytesRead)
            broadcastIntent.putExtra(FILE_SIZE, fileSize)
            broadcastIntent.putExtra(BASE_URL, baseUrl)
            broadcastIntent.putExtra(VIDEO_ID, videoId)
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
        updateDownloadedInLocal(baseUrl = baseUrl)
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

    private suspend fun updateDownloadedInLocal(baseUrl: String) {
        updateJob?.cancelAndJoin()
        updateJob = updateScope.launch {
            val video = localDataRepository.videoByBaseUrl(baseUrl)
            video.copy(
                state = DownloadState.COMPLETED
            ).also { updatedVideo ->
                localDataRepository.update(updatedVideo)
            }
        }
    }

    private suspend fun updateProgressInLocal(
        progress: Int, downloadByte: Long, fileSize: Long, uri: String?, baseUrl: String
    ) {
        updateJob?.cancelAndJoin()
        updateJob = updateScope.launch {
            val video = localDataRepository.videoByBaseUrl(baseUrl)
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    bytesDownloaded = downloadByte,
                    progress = progress,
                    megaBytesDownloaded = downloadByte.getFileSize(),
                    totalBytes = fileSize,
                    totalMegaBytes = fileSize.getFileSize(),
                    uri = uri.toString()
                ),
            ).also { updatedVideo ->
                localDataRepository.update(video = updatedVideo)
            }
        }
    }

}
