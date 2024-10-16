package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import com.youtube.data.service.VideoDownloadService
import com.youtube.domain.utils.Constant.BASE_URL
import com.youtube.domain.utils.Constant.START_BYTE
import com.youtube.domain.utils.Constant.TITLE
import com.youtube.domain.utils.Constant.VIDEO_URL

fun Context.startDownloadService(
    url: String,
    baseUrl: String,
    fileName: String,
    downloadedBytes: Long
) {
    pauseDownloadService()
    val intent = Intent(this, VideoDownloadService::class.java).apply {
        putExtra(VIDEO_URL, url)
        putExtra(BASE_URL, baseUrl)
        putExtra(TITLE, fileName)
        putExtra(START_BYTE, downloadedBytes)
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