package com.youtube.youtube_downloader.util.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.youtube.data.util.getFileSize
import com.youtube.domain.model.DownloadState.COMPLETED
import com.youtube.domain.model.Video
import com.youtube.domain.repository.VideoLocalDataRepository
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VideoBroadCastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var localDataRepository: VideoLocalDataRepository

    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private lateinit var videoUpdatesFlow: MutableSharedFlow<Video>

    fun setVideoUpdateFlow(flow: MutableSharedFlow<Video>) {
        this.videoUpdatesFlow = flow
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(VIDEO_ID)
        val videoDeferred = scope.async(Dispatchers.IO) {
            localDataRepository.videoById(id.toString())
        }
        when (intent.action) {
            PROGRESS_DATA -> {
                val progress = intent.getIntExtra(LAST_PROGRESS, ZERO)
                val downloadByte = intent.getLongExtra(DOWNLOADED_BYTES, ZERO_LONG)
                val fileSize = intent.getLongExtra(FILE_SIZE, ZERO_LONG)
                val uri = intent.getStringExtra(URI)
                scope.launch {
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
                scope.launch {
                    val video = videoDeferred.await()
                    val filePath = intent.getStringExtra(FILE_PATH)
                    val updatedVideo = video.copy(
                        state = COMPLETED, filePath = filePath
                    )
                    videoUpdatesFlow.emit(updatedVideo)
                    localDataRepository.update(updatedVideo)
                }
            }
        }
    }

    private fun updateProgress(
        progress: Int, downloadByte: Long, video: Video, fileSize: Long, uri: String?
    ) {
        scope.launch {
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
                videoUpdatesFlow.emit(updatedVideo)
            }
        }
    }
}