package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import com.youtube.data.service.VideoDownloadService

fun Context.startDownloadService(
    url: String,
    baseUrl: String,
    fileName: String,
    downloadedBytes: Long
) {
    pauseDownloadService()
    val intent = Intent(this, VideoDownloadService::class.java).apply {
        putExtra("url", url)
        putExtra("baseUrl", baseUrl)
        putExtra("fileName", fileName)
        putExtra("downloadedBytes", downloadedBytes)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)
    } else {
        this.startService(intent)
    }
}

fun Context.pauseDownloadService() {
    val intent = Intent(this, VideoDownloadService::class.java)
    stopService(intent)
}