package com.youtube.youtube_downloader.presenter

import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadService
import com.chaquo.python.android.PyApplication
import com.youtube.data.service.VideoDownloadService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication() : PyApplication() {

}