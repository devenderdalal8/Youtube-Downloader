package com.youtube.data.repositoryImpl

import android.util.Log
import com.youtube.data.dao.VideoDao
import com.youtube.domain.model.Video
import com.youtube.domain.repository.VideoLocalDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class VideoLocalDataRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
) : VideoLocalDataRepository {
    override suspend fun insert(video: Video) {
        withContext(Dispatchers.IO) {
            videoDao.insert(video)
        }
    }

    override suspend fun update(video: Video) {
        withContext(Dispatchers.IO) {
            videoDao.update(video)
            Log.d("TAG", "update: ${video}")
        }
    }

    override suspend fun delete(video: Video) {
        withContext(Dispatchers.IO) {
            videoDao.delete(video)
            Log.e("TAG", "delete: $video", )
        }
    }

    override suspend fun getVideos(): Flow<List<Video>> = videoDao.getVideos()

    override suspend fun isVideoAvailable(baseUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            val count = videoDao.isVideoAvailable(baseUrl)
            (count > 0)
        }
    }

    override suspend fun videoByBaseUrl(baseUrl: String): Video {
        return withContext(Dispatchers.IO) {
            videoDao.videoByBaseUrl(baseUrl = baseUrl)
        }
    }

    override suspend fun videoById(id: String): Video {
        return withContext(Dispatchers.IO) {
            videoDao.videoById(id = UUID.fromString(id))
        }
    }
}