package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.data.service.VideoDownloadService
import com.youtube.data.util.getFileSize
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.DownloadState.COMPLETED
import com.youtube.domain.model.DownloadState.FAILED
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.utils.Constant.BASE_URL
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.DOWNLOAD_FAILED
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.pauseDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    private fun getAllVideos() {
        viewModelScope.launch {
            try {
                localDataRepository.getVideos().collect { video ->
                    allVideos.clear()
                    allVideos.addAll(video)
                    _videos.value = UiState.Success(video)
                }
            } catch (e: Exception) {
                _videos.value = UiState.Error(e.message.toString())
            }
        }
    }

    fun resumeDownload(
        context: Context,
        url: String,
        downloadedBytes: Long = 0L,
        fileName: String? = "",
        baseUrl: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val video = async { localDataRepository.videoByBaseUrl(baseUrl) }.await()

            val intent = Intent(context, VideoDownloadService::class.java).apply {
                putExtra("url", url)
                putExtra("baseUrl", baseUrl)
                putExtra("fileName", fileName)
                putExtra("downloadedBytes", downloadedBytes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            updateLocalVideo(video, DownloadState.DOWNLOADING)
        }
    }

    private fun updateLocalVideo(video: LocalVideo, newState: DownloadState) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedVideo = video.copy(
                downloadProgress = video.downloadProgress.copy(
                    state = newState
                )
            )
            localDataRepository.update(updatedVideo)
            val index = allVideos.indexOfFirst { local -> local.baseUrl == video.baseUrl }
            if (index >= 0) {
                allVideos[index] = updatedVideo
                _videos.value = UiState.Success(allVideos)
            }
        }
    }

    fun pauseVideoService(context: Context, baseUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val video = async { localDataRepository.videoByBaseUrl(baseUrl) }.await()
            updateLocalVideo(video, DownloadState.PAUSED)
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
            val baseUrl = intent.getStringExtra(BASE_URL) ?: return
            val videoDeferred = viewModelScope.async(Dispatchers.IO) {
                localDataRepository.videoByBaseUrl(baseUrl)
            }

            when (intent.action) {
                PROGRESS_DATA -> {
                    val progress = intent.getIntExtra(LAST_PROGRESS, 0)
                    val downloadByte = intent.getLongExtra(DOWNLOADED_BYTES, 0L)
                    val fileSize = intent.getStringExtra(FILE_SIZE)
                    _progress.value =
                        Triple(progress.toFloat(), downloadByte, fileSize.toString())
                    viewModelScope.launch(Dispatchers.IO) {
                        updateProgress(
                            video = videoDeferred.await(),
                            progress = progress,
                            downloadByte = downloadByte
                        )
                    }
                }

                DOWNLOAD_COMPLETE -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val video = videoDeferred.await()
                        val updatedVideo = video.copy(
                            downloadProgress = video.downloadProgress.copy(
                                state = COMPLETED
                            )
                        )
                        localDataRepository.update(updatedVideo)
                        getAllVideos()
                    }
                }

                DOWNLOAD_FAILED -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val video = videoDeferred.await()
                        val updatedVideo = video.copy(
                            downloadProgress = video.downloadProgress.copy(
                                state = FAILED
                            )
                        )
                        localDataRepository.update(updatedVideo)
                        getAllVideos()
                    }
                }
            }
        }
    }

    private fun updateProgress(
        progress: Int,
        downloadByte: Long,
        video: LocalVideo
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    bytesDownloaded = downloadByte,
                    progress = progress,
                    megaBytesDownloaded = downloadByte.getFileSize()
                )
            ).also { updatedVideo ->
                localDataRepository.update(video = updatedVideo)
            }
        }
    }
}
