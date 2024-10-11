package com.youtube.youtube_downloader.presenter

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.chaquo.python.android.PyApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
internal class MyApplication() : PyApplication(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}