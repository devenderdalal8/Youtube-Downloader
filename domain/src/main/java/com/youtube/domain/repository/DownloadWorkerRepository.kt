package com.youtube.domain.repository

import com.youtube.domain.model.Video
import java.util.UUID

interface DownloadWorkerRepository {
    suspend fun startDownload(video: Video): UUID

    suspend fun pauseDownload()
}