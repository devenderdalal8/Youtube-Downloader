package com.youtube.data.di

import com.youtube.data.repositoryImpl.VideoLocalDataRepositoryImpl
import com.youtube.domain.repository.VideoLocalDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideLocalDataRepository(repository: VideoLocalDataRepositoryImpl): VideoLocalDataRepository

}