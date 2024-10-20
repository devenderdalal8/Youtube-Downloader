package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.DownloadProgress
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.usecase.GetVideoResolutionUseCase
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.util.getFileSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val getVideoResolutionUseCase: GetVideoResolutionUseCase,
    private val localDataRepository: VideoLocalDataRepository,
    private val downloadWorkerRepository: DownloadWorkerRepository
) : ViewModel() {

    private val _downloadVideoUiState =
        MutableStateFlow<DownloadVideoUiState>(DownloadVideoUiState.Loading)
    val downloadVideoUiState = _downloadVideoUiState.asStateFlow()

    fun getVideoDetails(resolutions: List<String>, videoUrl: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = arrayListOf<VideoDetails>()
            resolutions.sortedBy { it.length }.forEach { resolution ->
                val url =
                    async { getVideoResolutionUseCase(videoUrl.toString(), resolution) }.await()
                when (url) {
                    is Resource.Success -> {
                        val size = getSize(url.data.toString())
                        list.add(
                            VideoDetails(
                                resolution = resolution, url = url.data.toString(), size = size
                            )
                        )
                    }

                    else -> {}
                }
            }
            _downloadVideoUiState.value = DownloadVideoUiState.Success(list)
        }
    }

    fun isVideoAvailable(baseUrl: String): Boolean {
        var result = false
        viewModelScope.launch(Dispatchers.IO) {
            result = localDataRepository.isVideoAvailable(baseUrl = baseUrl)
        }
        return result
    }

    fun storeVideoLocally(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedVideo = video.copy(
                downloadProgress = DownloadProgress(
                    totalMegaBytes = video.length.toString(),
                    totalBytes = video.length ?: 0L,
                    progress = 0,
                    uri = Uri.parse(video.videoUrl).toString()
                ),
                state = DownloadState.DOWNLOADING,
            )
            localDataRepository.insert(updatedVideo)
        }
    }

    fun startDownload(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadWorkerRepository.pauseDownload()
            pauseAllDownloads()
            val workId = downloadWorkerRepository.startDownload(video = video)
            val updatedVideo = video.copy(
                workId = workId
            )
            localDataRepository.update(updatedVideo)
        }
    }

    private fun pauseAllDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            val videos = async { localDataRepository.getVideos() }.await()
            videos.collect { video ->
                video.forEach { item ->
                    if (item.state == DownloadState.DOWNLOADING) {
                        pauseVideoService(item)
                    }
                }
            }
        }
    }

    private fun pauseVideoService(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedVideo = video.copy(
                state = DownloadState.PAUSED,
            )
            localDataRepository.update(updatedVideo)
        }
    }

    private suspend fun getSize(videoUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(videoUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.contentLengthLong.getFileSize()
            } catch (e: IOException) {
                e.message.toString()
            }
        }
    }
}

sealed class DownloadVideoUiState {
    data class Success(val data: List<VideoDetails>) : DownloadVideoUiState()
    data object Loading : DownloadVideoUiState()
}
