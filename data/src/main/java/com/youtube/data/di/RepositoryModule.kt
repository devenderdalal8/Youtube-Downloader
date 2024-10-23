package com.youtube.data.di

import com.youtube.data.repositoryImpl.DownloadWorkerRepositoryImpl
import com.youtube.data.repositoryImpl.VideoLocalDataRepositoryImpl
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideLocalDataRepository(repository: VideoLocalDataRepositoryImpl): VideoLocalDataRepository

    @Binds
    @Singleton
    abstract fun provideWorkerRepository(downloadWorkerRepositoryImpl: DownloadWorkerRepositoryImpl): DownloadWorkerRepository
}