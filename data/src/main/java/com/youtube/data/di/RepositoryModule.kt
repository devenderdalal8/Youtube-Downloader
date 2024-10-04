package com.youtube.data.di

import com.youtube.data.repositoryImpl.VideoLocalDataRepositoryImpl
import com.youtube.domain.repository.VideoLocalDataRepository
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
    fun provideLocalDataRepository(repository: VideoLocalDataRepositoryImpl): VideoLocalDataRepository {
        return repository
    }
}