package com.youtube.domain.repository

import java.util.UUID

interface DownloadWorkerRepository {
    suspend fun startDownload(
        baseUrl:String,
        url: String,
        downloadedBytes: Long,
        fileName: String?
    )

    suspend fun pauseDownload()
    suspend fun getRequestId(): UUID?
}