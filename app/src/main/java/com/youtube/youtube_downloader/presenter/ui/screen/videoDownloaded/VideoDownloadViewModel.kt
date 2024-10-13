package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        getAllVideos()
    }

    private fun getAllVideos() {
        viewModelScope.launch {
            try {
                localDataRepository.getVideos().collect() { videos ->
                    Log.d("TAG", "getAllVideos: $videos")
                    _videos.value = UiState.Success(videos)
                }

            } catch (e: Exception) {
                _videos.value = UiState.Error(e.message.toString())
            }
        }
    }
}
