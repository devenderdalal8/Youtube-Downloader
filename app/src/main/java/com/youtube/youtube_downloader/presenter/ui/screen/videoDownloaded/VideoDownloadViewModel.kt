package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.pauseDownloadService
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.startDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoDownloadViewModel @Inject constructor(
    private val localDataRepository: VideoLocalDataRepository,
) : ViewModel() {

    private val _progress = MutableStateFlow(Triple(0F, 0L, ""))
    val progress = _progress.asStateFlow()

    private val _videos = MutableStateFlow<UiState>(UiState.Loading)
    val videos = _videos.asStateFlow()
    private var allVideos: MutableList<LocalVideo> = mutableListOf()
    init {
        getAllVideos()
    }

    fun updateVideo(video: LocalVideo) {
        viewModelScope.launch(Dispatchers.IO) {
            localDataRepository.update(video)
            getAllVideos()
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun resumeDownload(
        context: Context,
        url: String,
        downloadedBytes: Long = 0L,
        fileName: String? = "",
        baseUrl: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            context.startDownloadService(
                url = url,
                downloadedBytes = downloadedBytes,
                fileName = fileName.toString(),
                baseUrl = baseUrl
            )
        }
    }

    fun pauseVideoService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            context.pauseDownloadService()
        }
    }

    fun deleteVideo(position: Int) {
        viewModelScope.launch {
            val videoToDelete = allVideos[position]
            localDataRepository.delete(video = videoToDelete)
            allVideos.removeAt(position)
            _videos.value = UiState.Success(allVideos.toList())
        }
    }

    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                PROGRESS_DATA -> {
                    val progress = intent.getStringExtra(LAST_PROGRESS)
                    val downloadByte = intent.getLongExtra(DOWNLOADED_BYTES, 0L)
                    val fileSize = intent.getStringExtra(FILE_SIZE)
                    _progress.value =
                        Triple(progress?.toFloat() ?: 0F, downloadByte, fileSize.toString())
                    Log.e("TAG", "onReceive: ${_progress.value.first}")
                }

                DOWNLOAD_COMPLETE -> {
                    Log.e("TAG", "onReceive: completed")
                }
            }
        }
    }
}
