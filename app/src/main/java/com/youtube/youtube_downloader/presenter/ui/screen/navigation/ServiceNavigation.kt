package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import com.youtube.data.service.VideoDownloadService
import com.youtube.domain.model.Video
import com.youtube.domain.utils.Constant.BASE_URL
import com.youtube.domain.utils.Constant.START_BYTE
import com.youtube.domain.utils.Constant.TITLE
import com.youtube.domain.utils.Constant.VIDEO_ID
import com.youtube.domain.utils.Constant.VIDEO_NOTIFICATION_ID
import com.youtube.domain.utils.Constant.VIDEO_URL

fun Context.startDownloadService(
    video: Video
) {
    pauseDownloadService()
    val intent = Intent(this, VideoDownloadService::class.java).apply {
        putExtra(VIDEO_URL, video.selectedVideoUrl)
        putExtra(BASE_URL, video.baseUrl)
        putExtra(TITLE, video.title)
        putExtra(START_BYTE, video.downloadProgress.bytesDownloaded)
        putExtra(VIDEO_ID, video.id.toString())
        putExtra(VIDEO_NOTIFICATION_ID, video.notificationId)
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