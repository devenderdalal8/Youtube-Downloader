package com.youtube.domain.repository

import com.youtube.domain.model.entity.LocalVideo
import kotlinx.coroutines.flow.Flow

interface VideoLocalDataRepository {
    suspend fun insert(video: LocalVideo)

    suspend fun update(video: LocalVideo)

    suspend fun delete(video: LocalVideo)

    suspend fun getAllVideo(): Flow<List<LocalVideo>>

    suspend fun isVideoAvailable(id: String): Boolean

    suspend fun videoById(id: String): LocalVideo
}