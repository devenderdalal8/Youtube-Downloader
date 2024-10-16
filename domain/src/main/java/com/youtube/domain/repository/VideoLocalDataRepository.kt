package com.youtube.domain.repository

import com.youtube.domain.model.entity.LocalVideo
import kotlinx.coroutines.flow.Flow

interface VideoLocalDataRepository {
    suspend fun insert(video: LocalVideo)

    suspend fun update(video: LocalVideo)

    suspend fun delete(video: LocalVideo)

    suspend fun getVideos(): Flow<List<LocalVideo>>

    suspend fun isVideoAvailable(baseUrl: String): Boolean

    suspend fun videoByBaseUrl(baseUrl: String): LocalVideo
}