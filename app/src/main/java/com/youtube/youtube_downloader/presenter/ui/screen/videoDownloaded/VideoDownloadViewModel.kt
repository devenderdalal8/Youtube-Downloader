package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoDownloadViewModel @Inject constructor(
    private val workerRepository: DownloadWorkerRepository,
    private val localDataRepository: VideoLocalDataRepository
) : ViewModel() {

    private val _videos = MutableStateFlow<UiState>(UiState.Loading)
    val videos = _videos.asStateFlow()

    private var allVideos: MutableList<LocalVideo> = mutableListOf()
    init {
        getAllVideos()
    }

    private fun getAllVideos() {
        viewModelScope.launch {
            try {
                localDataRepository.getVideos().collect() { video ->
                    allVideos.addAll(video)
                    _videos.value = UiState.Success(video)
                }

            } catch (e: Exception) {
                _videos.value = UiState.Error(e.message.toString())
            }
        }
    }

    fun deleteVideo(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoToDelete = allVideos[position]
            localDataRepository.delete(video = videoToDelete)
            allVideos.removeAt(position)
            _videos.value = UiState.Success(allVideos.toList())
        }
    }
}
