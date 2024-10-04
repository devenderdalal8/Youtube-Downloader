package com.youtube.data.service.workManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.youtube.data.R
import com.youtube.domain.utils.Constant.FILE_PATH
import com.youtube.domain.utils.Constant.PROGRESS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltWorker
class VideoWorkManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val fileName = inputData.getString("fileName") ?: return Result.failure()
        val downloadBytes = inputData.getLong("downloadedBytes", 0)
        setForeground(createForegroundInfo(0, fileName))
        return try {
            downloadVideo(fileName, url, downloadBytes)
            Result.success()
        } catch (ex: Exception) {
            Log.e("TAG", "doWork: ${ex.message}")
            Result.failure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun downloadVideo(
        fileName: String, url: String, startByte: Long
    ) {
        withContext(Dispatchers.IO) {
            val resolver = applicationContext.contentResolver
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
                        Log.d("TAG", "downloadVideo: ${uri.path}")
                        if (!response.isSuccessful) {
                            throw Exception("Failed to download video")
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
                                    setProgressAsync(
                                        workDataOf(
                                            PROGRESS to progress
                                        )
                                    )
                                    setForeground(createForegroundInfo(progress, fileName))
                                }
                            }
                            outputStream?.close()
                            inputStream.close()
                            fullPath =
                                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName.mp4"
                            setProgressAsync(
                                workDataOf(
                                    FILE_PATH to uri.path
                                )
                            )
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("DownloadError", "Failed to download video: ${ex.message}")
                    throw ex
                }finally {
                    if(fullPath ==null){
                        Log.d("Download", "Deleting incomplete file")
                        resolver.delete(uri, null, null)
                    }
                }
            }
        }
    }

    private fun createForegroundInfo(progress: Int, fileName: String = ""): ForegroundInfo {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "download_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, fileName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        val notification =
            NotificationCompat.Builder(applicationContext, channelId).setContentTitle(fileName)
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                ).setProgress(100, progress, false).build()

        return ForegroundInfo(1, notification)
    }
}