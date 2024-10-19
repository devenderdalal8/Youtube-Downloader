package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtube.data.util.getFileSize
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.DownloadState.COMPLETED
import com.youtube.domain.model.DownloadState.FAILED
import com.youtube.domain.model.Video
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.usecase.GetVideoResolutionUseCase
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.DOWNLOAD_FAILED
import com.youtube.domain.utils.Constant.FILE_PATH
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.domain.utils.Constant.VIDEO_ID
import com.youtube.domain.utils.Constant.ZERO
import com.youtube.domain.utils.Constant.ZERO_FLOAT
import com.youtube.domain.utils.Constant.ZERO_LONG
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.pauseDownloadService
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.startDownloadService
import com.youtube.youtube_downloader.util.isUrlExpired
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoDownloadViewModel @Inject constructor(
    private val localDataRepository: VideoLocalDataRepository,
    private val getVideoResolutionUseCase: GetVideoResolutionUseCase,
) : ViewModel() {

    private val _progress = MutableStateFlow(Triple(ZERO_FLOAT, ZERO_LONG, NOTHING))
    val progress = _progress.asStateFlow()

    private val _videoId = MutableStateFlow(NOTHING)
    val videoId = _videoId.asStateFlow()

    private val _videos = MutableStateFlow<UiState>(UiState.Loading)
    val videos = _videos.asStateFlow()

    private var allVideos: MutableList<Video> = mutableListOf()

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
                _videos.update { UiState.Error(e.message.toString()) }
            }
        }
    }

    fun resumeDownload(
        context: Context, baseUrl: String, videoUrl: String, resolution: String, id: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _videoId.update { id }
            if (videoUrl.isUrlExpired()) {
                context.updateExpireUrl(baseUrl, resolution, id = id)
            } else {
                val video = async { localDataRepository.videoById(id) }.await()
                updateVideo(
                    video = video,
                    newState = DownloadState.DOWNLOADING,
                    url = video.selectedVideoUrl.toString()
                )
                context.startDownloadService(
                    video = video
                )
            }
        }
    }

    private fun Context.updateExpireUrl(baseUrl: String, resolution: String, id: String) {
        viewModelScope.launch {
            val url = async { getVideoResolutionUseCase(baseUrl, resolution) }.await()
            when (url) {
                is Resource.Error -> {
                    Log.e("TAG", "updateExpireUrl: ${url.message.toString()}")
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    val video = async { localDataRepository.videoById(id) }.await()
                    updateVideo(
                        video = video,
                        newState = DownloadState.DOWNLOADING,
                        url = url.data.toString()
                    )
                    this@updateExpireUrl.startDownloadService(
                        video = video
                    )
                }
            }
        }
    }

    private fun updateVideo(video: Video, newState: DownloadState, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedVideo = video.copy(
                state = newState,
                selectedVideoUrl = url
            )
            localDataRepository.update(updatedVideo)
            allVideos.replaceAll { if (it.id == updatedVideo.id) updatedVideo else it }
            _videos.update { UiState.Success(allVideos.toList()) }
        }
    }

    fun pauseVideoService(context: Context, baseUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val video = async { localDataRepository.videoByBaseUrl(baseUrl) }.await()
            updateVideo(video, DownloadState.PAUSED, video.selectedVideoUrl.toString())
            context.pauseDownloadService()
        }
    }

    fun deleteVideo(position: Int) {
        viewModelScope.launch {
            val videoToDelete = allVideos[position]
            localDataRepository.delete(video = videoToDelete)
            allVideos.removeAt(position)
            _videos.update { UiState.Success(allVideos.toList()) }

        }
    }

    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getStringExtra(VIDEO_ID) ?: return
            Log.i("TAG", "onReceive: video_id $id")
            _videoId.update { id }
            val videoDeferred = viewModelScope.async(Dispatchers.IO) {
                localDataRepository.videoById(id)
            }

            when (intent.action) {
                PROGRESS_DATA -> {
                    val progress = intent.getIntExtra(LAST_PROGRESS, ZERO)
                    val downloadByte = intent.getLongExtra(DOWNLOADED_BYTES, ZERO_LONG)
                    val fileSize = intent.getStringExtra(FILE_SIZE)
                    val filePath = intent.getStringExtra(FILE_PATH)
                    _progress.value =
                        Triple(progress.toFloat(), downloadByte, fileSize.toString())
                    viewModelScope.launch(Dispatchers.IO) {
                        updateProgress(
                            video = videoDeferred.await(),
                            progress = progress,
                            downloadByte = downloadByte,
                            filePath = filePath.toString(),
                        )
                    }
                }

                DOWNLOAD_COMPLETE -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val video = videoDeferred.await()
                        val updatedVideo = video.copy(
                            state = COMPLETED
                        )
                        localDataRepository.update(updatedVideo)
                        getAllVideos()
                    }
                }

                DOWNLOAD_FAILED -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val video = videoDeferred.await()
                        val updatedVideo = video.copy(
                            state = FAILED
                        )
                        localDataRepository.update(updatedVideo)
                    }
                }
            }
        }
    }

    private fun updateProgress(
        progress: Int, downloadByte: Long, video: Video, filePath: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    bytesDownloaded = downloadByte,
                    progress = progress,
                    megaBytesDownloaded = downloadByte.getFileSize()
                ),
                filePath = filePath
            ).also { updatedVideo ->
                localDataRepository.update(video = updatedVideo)
            }
        }
    }
}
