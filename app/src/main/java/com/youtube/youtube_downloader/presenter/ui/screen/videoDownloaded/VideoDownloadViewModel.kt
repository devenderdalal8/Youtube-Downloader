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
import com.youtube.domain.model.Video
import com.youtube.domain.repository.DownloadWorkerRepository
import com.youtube.domain.repository.VideoLocalDataRepository
import com.youtube.domain.usecase.GetVideoResolutionUseCase
import com.youtube.domain.utils.Constant.DOWNLOADED_BYTES
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.FILE_PATH
import com.youtube.domain.utils.Constant.FILE_SIZE
import com.youtube.domain.utils.Constant.LAST_PROGRESS
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.domain.utils.Constant.URI
import com.youtube.domain.utils.Constant.VIDEO_ID
import com.youtube.domain.utils.Constant.ZERO
import com.youtube.domain.utils.Constant.ZERO_LONG
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
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
    private val downloadWorkerRepository: DownloadWorkerRepository
) : ViewModel() {

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
                    Log.i("TAG", "getAllVideos: $video")
                }
            } catch (e: Exception) {
                _videos.update { UiState.Error(e.message.toString()) }
            }
        }
    }

    fun resumeDownload(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadWorkerRepository.pauseDownload()
            pauseAllDownloads()
            if (video.selectedVideoUrl.toString().isUrlExpired()) {
                updateExpireUrl(
                    video.baseUrl.toString(),
                    video.selectedResolution.toString(),
                    id = video.id.toString()
                )
            } else {
                val videoById = async { localDataRepository.videoById(video.id.toString()) }.await()
                val workId = downloadWorkerRepository.startDownload(video = videoById)
                val updatedVideo = video.copy(workId = workId)
                updateVideo(
                    video = videoById,
                    newState = DownloadState.DOWNLOADING,
                    url = videoById.selectedVideoUrl.toString(),
                )
                localDataRepository.update(updatedVideo)
            }
        }
    }

    private fun pauseAllDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            allVideos.forEach { video ->
                if (video.state == DownloadState.DOWNLOADING) {
                    pauseVideoService(video)
                }
            }
        }
    }

    private fun updateExpireUrl(baseUrl: String, resolution: String, id: String) {
        viewModelScope.launch {
            val url = async {
                this@VideoDownloadViewModel.getVideoResolutionUseCase(baseUrl, resolution)
            }.await()
            when (url) {
                is Resource.Error -> {
                    Log.e("TAG", "updateExpireUrl: ${url.message.toString()}")
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    val video = async { localDataRepository.videoById(id) }.await()
                    val workId = downloadWorkerRepository.startDownload(video = video)
                    val updatedVideo = video.copy(workId = workId)
                    updateVideo(
                        video = updatedVideo,
                        newState = DownloadState.DOWNLOADING,
                        url = url.data.toString()
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

    fun pauseVideoService(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            updateVideo(video, DownloadState.PAUSED, video.selectedVideoUrl.toString())
            Log.e("TAG", "pauseVideoService: pause")
            downloadWorkerRepository.pauseDownload()
        }
    }

    fun deleteVideo(video: Video, position: Int) {
        viewModelScope.launch {
            localDataRepository.delete(video = video)
            val videos = allVideos.apply {
                removeAt(position)
            }
            _videos.update { UiState.Success(videos) }
        }
    }

    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getStringExtra(VIDEO_ID) ?: return
            val videoDeferred = viewModelScope.async(Dispatchers.IO) {
                localDataRepository.videoById(id)
            }
            when (intent.action) {
                PROGRESS_DATA -> {
                    val progress = intent.getIntExtra(LAST_PROGRESS, ZERO)
                    val downloadByte = intent.getLongExtra(DOWNLOADED_BYTES, ZERO_LONG)
                    val fileSize = intent.getLongExtra(FILE_SIZE, ZERO_LONG)
                    val uri = intent.getStringExtra(URI)
                    Log.d(
                        "TAG",
                        "onReceive() returned: progress: $progress \n downloadByte: $downloadByte \n fileSize: $fileSize"
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        updateProgress(
                            video = videoDeferred.await(),
                            progress = progress,
                            downloadByte = downloadByte,
                            fileSize = fileSize,
                            uri = uri
                        )
                    }
                }

                DOWNLOAD_COMPLETE -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val video = videoDeferred.await()
                        val filePath = intent.getStringExtra(FILE_PATH)
                        val updatedVideo = video.copy(
                            state = COMPLETED,
                            filePath = filePath
                        )
                        localDataRepository.update(updatedVideo)
                        getAllVideos()
                    }
                }
            }
        }
    }

    private fun updateProgress(
        progress: Int, downloadByte: Long, video: Video, fileSize: Long, uri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            video.copy(
                downloadProgress = video.downloadProgress.copy(
                    bytesDownloaded = downloadByte,
                    progress = progress,
                    megaBytesDownloaded = downloadByte.getFileSize(),
                    totalBytes = fileSize,
                    totalMegaBytes = fileSize.getFileSize(),
                    uri = uri.toString()
                ),
            ).also { updatedVideo ->
                localDataRepository.update(video = updatedVideo)
                allVideos.replaceAll { if (it.id == updatedVideo.id) updatedVideo else it }
                _videos.update { UiState.Success(allVideos.toList()) }
            }
        }
    }
}
