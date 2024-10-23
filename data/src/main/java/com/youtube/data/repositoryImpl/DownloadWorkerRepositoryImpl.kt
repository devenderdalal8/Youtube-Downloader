package com.youtube.data.repositoryImpl

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.youtube.data.service.workManager.VideoWorkManager
import com.youtube.domain.model.Video
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.utils.Constant.BASE_URL
import com.youtube.domain.utils.Constant.START_BYTE
import com.youtube.domain.utils.Constant.TITLE
import com.youtube.domain.utils.Constant.VIDEO_ID
import com.youtube.domain.utils.Constant.VIDEO_NOTIFICATION_ID
import com.youtube.domain.utils.Constant.VIDEO_URL
import java.util.UUID
import javax.inject.Inject

class DownloadWorkerRepositoryImpl @Inject constructor(
    private val workManager: WorkManager
) : DownloadWorkerRepository {

    override suspend fun startDownload(video: Video): UUID {
        val inputData = Data.Builder()
            .putString(VIDEO_URL, video.selectedVideoUrl)
            .putString(BASE_URL, video.baseUrl)
            .putString(TITLE, video.title)
            .putLong(START_BYTE, video.downloadProgress.bytesDownloaded)
            .putString(VIDEO_ID, video.id.toString())
            .putInt(VIDEO_NOTIFICATION_ID, video.notificationId)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<VideoWorkManager>()
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            video.title.toString(),
            ExistingWorkPolicy.REPLACE,
            downloadWorkRequest
        )

        return downloadWorkRequest.id
    }

    override suspend fun pauseDownload() {
        workManager.cancelAllWork()
    }

}