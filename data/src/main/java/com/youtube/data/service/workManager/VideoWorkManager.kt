package com.youtube.data.service.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import java.io.File


@HiltWorker
class VideoWorkManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val filePath = inputData.getString("filePath") ?: return Result.failure()
        val downloadBytes = inputData.getLong("downloadedBytes", 0)

        val outputFile = File(filePath)
//        setForeground(createForegroundInfo(0))
        return try {
//            downloadVideo(url, outputFile, downloadBytes)
            Result.success()
        } catch (ex: Exception) {
            Result.retry()
        }
    }

//    private suspend fun downloadVideo(url: String, outputFile: File, startByte: Long) {
//        withContext(Dispatchers.IO) {
//            val request = Request.Builder().url(url).header("Range", "bytes=$startByte-").build()
//            val response = okHttpClient.newCall(request).execute()
//
//            response.body()?.let { body ->
//                val inputStream = body.byteStream()
//                val outputStream = FileOutputStream(outputFile, true)
//
//                val buffer = ByteArray(1024 * 8)
//                var byteRead: Int
//
//                var totalByteRead = startByte
//                val totalBytes = body.contentLength().plus(startByte) ?: 1
//
//                while (inputStream.read(buffer).also { byteRead = it } != -1) {
//                    outputStream.write(buffer, 0, byteRead)
//                    totalByteRead += byteRead
//
//                    setForeground(createForegroundInfo((totalByteRead * 100 / totalBytes).toInt()))
//                }
//                outputStream.close()
//                inputStream.close()
//            }
//        }
//    }
//
//    private fun createForegroundInfo(progress: Int): ForegroundInfo {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
//        val channelId = "download_channel"
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            val channel =
//                NotificationChannel(channelId, "Download", NotificationManager.IMPORTANCE_LOW)
//            notificationManager?.createNotificationChannel(channel)
//        }
//
//        val notification = NotificationCompat.Builder(applicationContext, channelId)
//            .setContentTitle("Downloading Video")
//            .setProgress(100, progress, false)
////            .setSmallIcon(R.drawable.ic_file_download_icon)
//            .build()
//
//        return ForegroundInfo(1, notification)
//    }
}
