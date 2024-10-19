package com.youtube.youtube_downloader.presenter.ui.screen.mainActivity

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.youtube.domain.model.Video
import com.youtube.domain.usecase.GetVideoDetailsUseCase
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.util.getFileSize
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoDetailsUseCase: GetVideoDetailsUseCase,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _size = MutableStateFlow(Pair("", 0L))
    val size = _size.asStateFlow()

    private val _videoDetails = MutableStateFlow<UiState>(UiState.Loading)
    val videoDetails = _videoDetails.asStateFlow()

    fun getVideoDetails(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = getVideoDetailsUseCase(url = url)) {
                    is Resource.Success -> {
                        val data = Gson().fromJson(result.data.toString(), Video::class.java)
                        Log.d("TAG", "getVideoDetails: $data")
                        if (data.error?.isNotEmpty() == true) {
                            _videoDetails.value = UiState.Error(data.error.toString())
                        } else {
                            data.videoUrl?.getSize()
                            _videoDetails.value = UiState.Success(
                                data.copy(
                                    size = _size.value.first,
                                    length = _size.value.second
                                )
                            )
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