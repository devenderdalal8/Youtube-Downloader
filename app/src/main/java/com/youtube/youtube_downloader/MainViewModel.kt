package com.youtube.youtube_downloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.youtube.youtube_downloader.domain.usecase.GetVideoDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoDetailsUseCase: GetVideoDetailsUseCase, val exoPlayer: ExoPlayer
) : ViewModel() {

    private val _progressBarVisibility = MutableStateFlow(true)
    val progressBar = _progressBarVisibility.asStateFlow()

    init {
        handleExoPlayerListener()
    }

    private fun handleExoPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {

                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _progressBarVisibility.value = true

                    }

                    Player.EVENT_PLAY_WHEN_READY_CHANGED -> {}
                    Player.STATE_READY -> {
                        _progressBarVisibility.value = false
                    }

                    Player.STATE_IDLE -> {
                        _progressBarVisibility.value = false
                    }
                }
            }
        })
    }

    fun setMediaItem(videoUrl: String?, title: String? = "") {
        val mediaItem = MediaItem.Builder().setUri(videoUrl).setMediaId(videoUrl.toString())
            .setMediaMetadata(MediaMetadata.Builder().setDisplayTitle(title).build()).build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    private val _videoDetails = MutableStateFlow<UiState>(UiState.Loading)
    val videoDetails = _videoDetails.asStateFlow()

    fun getVideoDetails(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getVideoDetailsUseCase(url = url)
                if (result != null) {
                    _videoDetails.value = UiState.Success(result)
                }
            } catch (ex: Exception) {
                _videoDetails.value = UiState.Error(ex.message.toString())
            }
        }
    }
}

sealed class UiState {
    data class Success(val data: Any) : UiState()
    data object Loading : UiState()
    data class Error(val message: String) : UiState()
}