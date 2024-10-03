package com.youtube.youtube_downloader.presenter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.usecase.GetVideoDetailsUseCase
import com.youtube.youtube_downloader.util.getFileSize
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    private val _size = MutableStateFlow("")
    val size = _size.asStateFlow()

    private val _videoDetails = MutableStateFlow<UiState>(UiState.Loading)
    val videoDetails = _videoDetails.asStateFlow()


    fun getVideoDetails(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getVideoDetailsUseCase(url = url)
                result?.videoUrl?.getSize()
                if (result != null) {
                    _videoDetails.value = UiState.Success(result.copy(size = _size.value))
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
                _size.value = conn.contentLengthLong.getFileSize()
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
    data class Error(val message: String) : UiState()
}