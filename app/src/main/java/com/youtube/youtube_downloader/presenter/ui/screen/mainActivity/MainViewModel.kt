package com.youtube.youtube_downloader.presenter.ui.screen.mainActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.youtube.domain.model.Video
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.usecase.GetVideoDetailsUseCase
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.util.getFileSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoDetailsUseCase: GetVideoDetailsUseCase,
    private val localDataRepository: VideoLocalDataRepository,
) : ViewModel() {

    private val _size = MutableStateFlow(Pair(NOTHING, 0L))
    val size = _size.asStateFlow()
    private val _progress = MutableStateFlow(false)
    val progress = _progress.asStateFlow()

    private val _videoDetails = MutableStateFlow<UiState>(UiState.Loading)
    val videoDetails = _videoDetails.asStateFlow()

    private var currentVideo: Video = Video()

    fun getVideoDetails(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = getVideoDetailsUseCase(url = url)) {
                    is Resource.Success -> {
                        val video = Gson().fromJson(result.data.toString(), Video::class.java)
                        if (video.error?.isNotEmpty() == true) {
                            _videoDetails.value = UiState.Error(video.error.toString())
                        } else {
                            video.videoUrl?.getSize()
                            currentVideo = video.copy(
                                size = _size.value.first,
                                length = _size.value.second
                            )
                            _videoDetails.value = UiState.Success(currentVideo)
                        }
                    }

                    is Resource.Error -> {
                        _videoDetails.value = UiState.Error(result.message.toString())
                    }

                    is Resource.Loading -> {
                        _videoDetails.value = UiState.Loading
                    }
                }
            } catch (ex: Exception) {
                _videoDetails.value = UiState.Error(ex.message.toString())
            }
        }
    }

    fun getDownloadedVideo(videoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val video = async { localDataRepository.videoById(videoId) }.await()
            _videoDetails.update { UiState.Success(video) }
        }
    }

    private fun String.getSize() {
        viewModelScope.launch(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL(this@getSize)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                _size.value = Pair(conn.contentLengthLong.getFileSize(), conn.contentLengthLong)
            } catch (e: IOException) {
                Log.e("TAG", "getFileSizeFromUrl: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
    }
}

sealed class UiState {
    data class Success(val data: Any) : UiState()
    data object Loading : UiState()
    data object Nothing : UiState()
    data class Error(val message: String) : UiState()
}