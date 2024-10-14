package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.youtube.data.service.workManager.VideoDownloadService

@RequiresApi(Build.VERSION_CODES.O)
fun Context.startDownloadService(
    url: String,
    baseUrl: String,
    fileName: String,
    downloadedBytes: Long
) {
    val intent = Intent(this, VideoDownloadService::class.java).apply {
        putExtra("url", url)
        putExtra("baseUrl", baseUrl)
        putExtra("fileName", fileName)
        putExtra("downloadedBytes", downloadedBytes)
    }
    startForegroundService(intent)
}

fun Context.pauseDownloadService() {
    val intent = Intent(this, VideoDownloadService::class.java)
    stopService(intent)
}