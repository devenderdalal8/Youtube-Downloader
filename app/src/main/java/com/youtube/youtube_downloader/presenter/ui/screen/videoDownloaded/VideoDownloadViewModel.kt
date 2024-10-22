package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.usecase.GetVideoResolutionUseCase
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.util.isUrlExpired
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoDownloadViewModel @Inject constructor(
    private val localDataRepository: VideoLocalDataRepository,
    private val getVideoResolutionUseCase: GetVideoResolutionUseCase,
    private val downloadWorkerRepository: DownloadWorkerRepository,
) : ViewModel() {

    private val _videos = MutableStateFlow<UiState>(UiState.Loading)
    val videos = _videos.asStateFlow()

    private var allVideos: MutableList<Video> = mutableListOf()

    private val _videoUpdates = MutableSharedFlow<Video>(replay = 1)
    val videoUpdates = _videoUpdates

    init {
        getAllVideos()
        initialization()
    }

    private fun initialization() {
        viewModelScope.launch(Dispatchers.IO) {
            _videoUpdates.collectLatest { video ->
                allVideos.replaceAll { if (it.id == video.id) video else it }
                _videos.update { UiState.Success(allVideos.toList()) }
            }
        }
    }

    fun getAllVideos() {
        viewModelScope.launch {
            try {
                localDataRepository.getVideos().collect { video ->
                    allVideos.clear()
                    allVideos.addAll(video)
                    _videos.value = UiState.Success(video)
                    Log.i("TAG", "getAllVideos: $video")
                }
            } catch (e: Exception) {
                _videos.update { UiState.Error(e.message.toString()) }
            }
        }
    }

    fun resumeDownload(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadWorkerRepository.pauseDownload()
            pauseAllDownloads()
            if (video.selectedVideoUrl.toString().isUrlExpired()) {
                updateExpireUrl(
                    video.baseUrl.toString(),
                    video.selectedResolution.toString(),
                    id = video.id.toString()
                )
            } else {
                val videoById = async { localDataRepository.videoById(video.id.toString()) }.await()
                val workId = downloadWorkerRepository.startDownload(video = videoById)
                val updatedVideo = video.copy(workId = workId)
                updateVideo(
                    video = videoById,
                    newState = DownloadState.DOWNLOADING,
                    url = videoById.selectedVideoUrl.toString(),
                )
                localDataRepository.update(updatedVideo)
            }
        }
    }

    private fun pauseAllDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            allVideos.forEach { video ->
                if (video.state == DownloadState.DOWNLOADING) {
                    pauseVideoService(video)
                }
            }
        }
    }

    private fun updateExpireUrl(baseUrl: String, resolution: String, id: String) {
        viewModelScope.launch {
            val url = async {
                this@VideoDownloadViewModel.getVideoResolutionUseCase(baseUrl, resolution)
            }.await()
            when (url) {
                is Resource.Error -> {
                    Log.e("TAG", "updateExpireUrl: ${url.message.toString()}")
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    val video = async { localDataRepository.videoById(id) }.await()
                    val workId = downloadWorkerRepository.startDownload(video = video)
                    val updatedVideo = video.copy(workId = workId)
                    updateVideo(
                        video = updatedVideo,
                        newState = DownloadState.DOWNLOADING,
                        url = url.data.toString()
                    )
                }
            }
        }
    }

    private fun updateVideo(video: Video, newState: DownloadState, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedVideo = video.copy(
                state = newState,
                selectedVideoUrl = url
            )
            localDataRepository.update(updatedVideo)
            allVideos.replaceAll { if (it.id == updatedVideo.id) updatedVideo else it }
            _videos.update { UiState.Success(allVideos.toList()) }
        }
    }

    fun pauseVideoService(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            updateVideo(video, DownloadState.PAUSED, video.selectedVideoUrl.toString())
            downloadWorkerRepository.pauseDownload()
        }
    }

    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            localDataRepository.delete(video = video)
            val updatedVideos = allVideos.filterNot { it == video }
            _videos.update { UiState.Success(updatedVideos) }
        }
    }
}
