package com.youtube.youtube_downloader.di

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PythonModule {
    @Provides
    @Singleton
    fun providePythonService(): PyObject {
        val python = Python.getInstance()
        val pythonFile = python.getModule("script")
        return pythonFile
    }

}