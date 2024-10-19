package com.youtube.domain.repository

import com.youtube.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoLocalDataRepository {
    suspend fun insert(video: Video)

    suspend fun update(video: Video)

    suspend fun delete(video: Video)

    suspend fun getVideos(): Flow<List<Video>>

    suspend fun isVideoAvailable(baseUrl: String): Boolean

    suspend fun videoByBaseUrl(baseUrl: String): Video

    suspend fun videoById(id: String): Video
}