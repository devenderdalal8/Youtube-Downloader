package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.DownloadProgress
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.usecase.GetVideoResolutionUseCase
import com.youtube.domain.utils.Constant
import com.youtube.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val localDataRepository: VideoLocalDataRepository,
    private val downloadWorkerRepository: DownloadWorkerRepository,
    private val getVideoResolutionUseCase: GetVideoResolutionUseCase,
) : ViewModel() {

    private var updatedVideo: Video? = null

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
                    totalBytes = video.length ?: Constant.ZERO_LONG,
                    progress = Constant.ZERO,
                    uri = Uri.parse(video.videoUrl).toString()
                ),
                state = DownloadState.DOWNLOADING,
            )
            localDataRepository.insert(updatedVideo)
        }
    }

    private fun Video.getVideoResolution(videoUrl: String?, resolution: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = async { getVideoResolutionUseCase(videoUrl.toString(), resolution) }.await()
            when (url) {
                is Resource.Success -> {
                    val updatedUrl = url.data.toString()
                    val video = this@getVideoResolution.copy(
                        selectedResolution = resolution,
                        selectedVideoUrl = updatedUrl
                    )
                    Log.e("TAG", "getVideoResolution: ${video.selectedResolution} -> ${video.selectedVideoUrl}", )
                    localDataRepository.update(video)
                    updatedVideo = video
                }

                is Resource.Loading -> {}

                else -> {}
            }
        }
    }

    fun startDownload(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
//            downloadWorkerRepository.pauseDownload()
//            pauseAllDownloads()
            video.getVideoResolution(
                videoUrl = video.baseUrl,
                resolution = video.selectedResolution.toString()
            )
            Log.d("TAG", "startDownload: ${updatedVideo?.selectedVideoUrl} => ${updatedVideo?.selectedResolution}")
            updatedVideo?.let { downloadWorkerRepository.startDownload(video = it) }
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

}