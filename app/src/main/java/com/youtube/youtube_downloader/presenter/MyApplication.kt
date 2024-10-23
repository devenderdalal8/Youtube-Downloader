package com.youtube.youtube_downloader.presenter

import android.util.Log
import androidx.work.Configuration
import com.chaquo.python.android.PyApplication
import com.youtube.data.service.workManager.VideoWorkerFactory
import com.youtube.domain.repository.VideoLocalDataRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
internal class MyApplication : PyApplication(), Configuration.Provider {

    @Inject
    lateinit var localDataRepository: VideoLocalDataRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(VideoWorkerFactory(localDataRepository))
            .build()

}