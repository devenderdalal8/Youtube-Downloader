package com.youtube.data.di

import com.youtube.data.repositoryImpl.DownloadWorkerRepositoryImpl
import com.youtube.domain.repository.DownloadWorkerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    @Singleton
    fun provideWorkerRepository(downloadWorkerRepositoryImpl: DownloadWorkerRepositoryImpl): DownloadWorkerRepository {
        return downloadWorkerRepositoryImpl
    }
}