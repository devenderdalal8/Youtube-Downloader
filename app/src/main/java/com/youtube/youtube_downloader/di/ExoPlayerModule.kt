package com.youtube.youtube_downloader.di

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ExoPlayerModule {

    @Provides
    fun provideExoPlayer(application: Application): ExoPlayer {
        return ExoPlayer.Builder(application).build()
    }

}