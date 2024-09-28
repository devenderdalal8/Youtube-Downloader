package com.youtube.youtube_downloader.di

import com.chaquo.python.PyObject
import com.youtube.youtube_downloader.data.repository.PythonScriptRepository
import com.youtube.youtube_downloader.data.repositoryImpl.PythonScriptRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun providePythonRepository(pythonService: PyObject): PythonScriptRepository {
        return PythonScriptRepositoryImpl(pythonService)
    }
}