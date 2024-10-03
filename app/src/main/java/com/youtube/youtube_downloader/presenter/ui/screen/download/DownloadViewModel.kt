package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.youtube.domain.usecase.GetVideoResolutionUseCase
import com.youtube.data.service.workManager.VideoDownloadService
import com.youtube.youtube_downloader.util.getFileSize
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val getVideoResolutionUseCase: GetVideoResolutionUseCase,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _downloadVideoUiState =
        MutableStateFlow<DownloadVideoUiState>(DownloadVideoUiState.Loading)
    val downloadVideoUiState = _downloadVideoUiState.asStateFlow()

    fun getVideoDetails(resolutions: List<String>, videoUrl: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = arrayListOf<VideoDetails>()
            resolutions.sortedBy { it.length }.forEach { resolution ->
                val url =
                    async { getVideoResolutionUseCase(videoUrl.toString(), resolution) }.await()
                val size = url.getSize()
                list.add(VideoDetails(resolution = resolution, url = url, size = size))
            }
            _downloadVideoUiState.value = DownloadVideoUiState.Success(list)
        }
    }

    fun startDownload(url: String, filePath: String, downloadedBytes: Long = 0L) {
        val intent = Intent(context, VideoDownloadService::class.java).apply {
            putExtra("url", url)
            putExtra("filePath", filePath)
            putExtra("downloadedBytes", downloadedBytes) // Start from the last downloaded byte
        }
        context.startService(intent)
    }

    fun pauseDownload() {
        val intent = Intent(context, VideoDownloadService::class.java)
        context.stopService(intent)
    }

    fun resumeDownload(url: String, filePath: String, downloadedBytes: Long) {
        startDownload(url, filePath, downloadedBytes) // Restart the service from the saved byte
    }

    private suspend fun String.getSize(): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(this@getSize)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.contentLengthLong.getFileSize()
            } catch (e: IOException) {
                "Error"
            }
        }
    }
}

sealed class DownloadVideoUiState {
    data class Success(val data: List<VideoDetails>) : DownloadVideoUiState()
    data object Loading : DownloadVideoUiState()
}
