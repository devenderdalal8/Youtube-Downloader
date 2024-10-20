package com.youtube.youtube_downloader.presenter.ui.screen.player

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log.e
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.youtube.domain.utils.Constant.NOTHING
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val exoPlayer: ExoPlayer,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _progressBarVisibility = MutableStateFlow(true)
    val progressBar = _progressBarVisibility.asStateFlow()

    init {
        handleExoPlayerListener()
    }

    @OptIn(UnstableApi::class)
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

                    Player.STATE_READY -> {
                        _progressBarVisibility.value = false
                    }

                    Player.STATE_IDLE -> {
                        _progressBarVisibility.value = false
                    }

                    Player.STATE_ENDED -> {
                        _progressBarVisibility.value = false
                    }
                }
            }
        })
    }

    fun setMediaItem(videoUrl: String?, title: String? = NOTHING, mp3: Boolean = false) {
        val mediaItem = MediaItem.Builder().setUri(videoUrl).setMediaId(videoUrl.toString())
            .setMediaMetadata(MediaMetadata.Builder().setDisplayTitle(title).build()).build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setURIMediaItem(videoUri: Uri, audioOnly: Boolean = false) {
        val resolver: ContentResolver = context.contentResolver
        resolver.openInputStream(videoUri)?.use {
            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        } ?: run {
            e("ExoPlayer", "playVideoWithExoPlayer: Unable to open content URI ")
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}