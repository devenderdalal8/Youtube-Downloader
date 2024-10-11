package com.youtube.data.di

import android.content.Context
import androidx.room.Room
import com.youtube.data.dao.VideoDao
import com.youtube.data.service.database.VideoDatabase
import com.youtube.domain.utils.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideVideoDatabase(@ApplicationContext context: Context): VideoDatabase {
        return Room.databaseBuilder(
            context = context,
            VideoDatabase::class.java,
            name = Constant.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideVideoDao(videoDatabase: VideoDatabase): VideoDao = videoDatabase.videoDao()
}