package com.youtube.youtube_downloader.presenter

import com.chaquo.python.android.PyApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication() : PyApplication() {

    override fun onCreate() {
        super.onCreate()

    }
}