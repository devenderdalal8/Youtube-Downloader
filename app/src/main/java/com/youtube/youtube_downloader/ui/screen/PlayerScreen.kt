package com.youtube.youtube_downloader.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.youtube.youtube_downloader.MainViewModel
import com.youtube.youtube_downloader.data.model.Video
import com.youtube.youtube_downloader.ui.theme.onPrimary

@SuppressLint("OpaqueUnitKey")
@ExperimentalAnimationApi
@Composable
fun PlayerScreen(
    video: Video, modifier: Modifier = Modifier, viewModel: MainViewModel, isDownloaded: Boolean
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val progressBarVisibility = viewModel.progressBar.collectAsState().value
    val released = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = video.videoId) {
        viewModel.setMediaItem(videoUrl = video.videoUrl, title = video.title)
    }

    if (isDownloaded) {
        OnlineVideoPlayer(
            video = video,
            modifier = modifier,
            viewModel = viewModel,
            context = context,
            progressBarVisibility = progressBarVisibility
        )
    } else {
        OnlineVideoPlayer(
            video = video,
            modifier = modifier,
            viewModel = viewModel,
            context = context,
            progressBarVisibility = progressBarVisibility
        )
    }
}

@Composable
fun DownloadedVideoPlayer(viewModel: MainViewModel) {
    Box(modifier = Modifier) {
        AndroidView(factory = { context ->
            PlayerView(context).apply {
                setFullscreenButtonClickListener { isFullscreen -> }

                showController()

                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                player = viewModel.exoPlayer

                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        })
    }
}

@Composable
fun OnlineVideoPlayer(
    video: Video,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    context: Context,
    progressBarVisibility: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black)
    ) {

        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = viewModel.exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    setFullscreenButtonClickListener { isFullScreen ->
                        if (isFullScreen) {
                            resizeMode = AspectRatioFrameLayout.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        } else {
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setControllerAnimationEnabled(true)
                    setShowFastForwardButton(true)
                    setShowRewindButton(true)
                    hideController()
                }
            }, modifier = Modifier.fillMaxSize()
        )
        ProgressBarWithImage(
            video = video, visibility = progressBarVisibility, isReleased = false
        )
    }
}

@Composable
fun ProgressBarWithImage(video: Video, visibility: Boolean, isReleased: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(video.thumbnailUrl)
                .crossfade(true).build(),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize()
                .alpha(if (isReleased) 1f else 0f)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .fillMaxSize()
                .alpha(if (visibility) 1f else 0f),
            color = onPrimary
        )
    }
}
