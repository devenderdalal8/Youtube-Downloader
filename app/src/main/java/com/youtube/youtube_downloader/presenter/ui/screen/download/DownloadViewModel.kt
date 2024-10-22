package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.DownloadProgress
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val localDataRepository: VideoLocalDataRepository,
    private val downloadWorkerRepository: DownloadWorkerRepository
) : ViewModel() {

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

}