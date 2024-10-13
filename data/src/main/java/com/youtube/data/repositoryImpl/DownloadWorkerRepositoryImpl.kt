package com.youtube.data.repositoryImpl

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.youtube.data.service.workManager.VideoWorkManager
import com.youtube.domain.repository.DownloadWorkerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class DownloadWorkerRepositoryImpl @Inject constructor(
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : DownloadWorkerRepository {

    private var requestId: UUID? = null

    override suspend fun startDownload(
        baseUrl:String,
        url: String,
        downloadedBytes: Long,
        fileName: String?
    ) {
        val inputData = Data.Builder()
            .putString("url", url)
            .putString("baseUrl", baseUrl)
            .putString("fileName", fileName)
            .putLong("downloadedBytes", downloadedBytes)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<VideoWorkManager>()
            .setInputData(inputData)
            .build()

        requestId = downloadWorkRequest.id

        WorkManager.getInstance(context).enqueueUniqueWork(
            "VideoDownloadWork", // Unique work name
            ExistingWorkPolicy.REPLACE, // Replace any existing work with the same name
            downloadWorkRequest
        )

        workManager.enqueue(downloadWorkRequest)
    }

    override suspend fun pauseDownload() {
        workManager.cancelAllWork()
    }

    override suspend fun getRequestId() = requestId

}