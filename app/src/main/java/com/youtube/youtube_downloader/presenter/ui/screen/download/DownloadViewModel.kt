package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.Video
import com.youtube.domain.model.entity.LocalVideo
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val getVideoResolutionUseCase: GetVideoResolutionUseCase,
    private val workerRepository: DownloadWorkerRepository,
    private val localDataRepository: VideoLocalDataRepository
) : ViewModel() {

    private val _requestID = MutableStateFlow<UUID?>(null)
    val requestId = _requestID.asStateFlow()

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

    fun storeVideoLocally(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            val localVideo = LocalVideo(
                title = video.title,
                thumbnailUrl = video.thumbnailUrl,
                baseUrl = video.baseUrl,
                downloadedPath = video.downloadedPath,
                videoUrl = video.videoUrl,
                duration = video.duration,
                isDownloaded = false,
                size = video.size
            )
            localDataRepository.insert(localVideo)
        }
    }

    fun startDownload(url: String, downloadedBytes: Long = 0L, fileName: String? = "") {
        viewModelScope.launch(Dispatchers.IO) {
            workerRepository.startDownload(
                fileName = fileName, url = url, downloadedBytes = downloadedBytes
            )
            _requestID.value = workerRepository.getRequestId()
            Log.d("TAG", "startDownload: ${_requestID.value}")
        }
    }

    fun pauseDownload() {
        viewModelScope.launch(Dispatchers.IO) {
            workerRepository.pauseDownload()
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
