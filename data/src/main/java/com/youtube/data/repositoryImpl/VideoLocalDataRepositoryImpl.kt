package com.youtube.data.repositoryImpl

import com.youtube.data.dao.VideoDao
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.repository.VideoLocalDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoLocalDataRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
) : VideoLocalDataRepository {
    override suspend fun insert(video: LocalVideo) {
        withContext(Dispatchers.IO) {
            videoDao.insert(video)
        }
    }

    override suspend fun update(video: LocalVideo) {
        withContext(Dispatchers.IO) {
            videoDao.update(video)
        }
    }

    override suspend fun delete(video: LocalVideo) {
        withContext(Dispatchers.IO) {
            videoDao.delete(video)
        }
    }

    override suspend fun getAllVideo(): Flow<List<LocalVideo>> {
        return withContext(Dispatchers.IO) {
            flowOf(videoDao.getAllVideo())
        }
    }

    override suspend fun isVideoAvailable(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            val count = videoDao.isVideoAvailable(id)
            (count > 0)
        }
    }

    override suspend fun videoById(id: String): LocalVideo {
        return withContext(Dispatchers.IO) {
            videoDao.videoById(id)
        }
    }
}