package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.dark_onPrimaryContainer
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.size_0
import com.youtube.youtube_downloader.presenter.ui.theme.size_14
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_24
import com.youtube.youtube_downloader.presenter.ui.theme.size_30
import com.youtube.youtube_downloader.presenter.ui.theme.size_32
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_80
import com.youtube.youtube_downloader.presenter.ui.theme.size_84
import com.youtube.youtube_downloader.util.getFileSize

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun VideoDownloadScreen(
    modifier: Modifier = Modifier, viewModel: VideoDownloadViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction(PROGRESS_DATA)
        mIntentFilter.addAction(DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(viewModel.mReceiver, mIntentFilter, Context.RECEIVER_NOT_EXPORTED)
        }else{
            context.registerReceiver(viewModel.mReceiver, mIntentFilter)

        }
    }

    DisposableEffect(Unit) {
        onDispose {
            context.unregisterReceiver(viewModel.mReceiver)
        }
    }

    val progress = viewModel.progress.collectAsState().value
    when (val result = viewModel.videos.collectAsState().value) {
        is UiState.Error -> {}
        UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val videos = result.data as List<LocalVideo>
            VideoScreen(modifier = modifier, videos = videos, onRemove = { position ->
                viewModel.deleteVideo(position)
            }, onLike = {}, progress = progress, viewModel = viewModel
            )
        }
        else -> {}
    }
}

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    videos: List<LocalVideo>,
    onRemove: (Int) -> Unit,
    onLike: (Int) -> Unit,
    progress: Triple<Float, Long, String>,
    viewModel: VideoDownloadViewModel,
) {
    LazyColumn {
        itemsIndexed(videos) { index, video ->
            val context = LocalContext.current
            val currentItem by rememberUpdatedState(index)
            val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onRemove(currentItem)
                        Toast.makeText(context, "StartToEnd", Toast.LENGTH_SHORT).show()
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        onLike(currentItem)
                        Toast.makeText(context, "EndToStart", Toast.LENGTH_SHORT).show()
                    }

                    SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
                }
                return@rememberSwipeToDismissBoxState true
            }, positionalThreshold = { it * .25f })

            SwipeToDismissBox(state = dismissState,
                modifier = modifier,
                backgroundContent = { DismissBackground(dismissState) },
                content = {
                    VideoItemView(
                        video = video,
                        modifier = modifier, progress = progress, viewModel = viewModel
                    )
                })
        }
    }
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFF1744)
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF1DE9B6)
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = size_14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(Icons.Default.Delete, contentDescription = "delete")
        Spacer(modifier = Modifier)
        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Archive")
    }
}

@Composable
fun VideoItemView(
    modifier: Modifier = Modifier,
    video: LocalVideo,
    progress: Triple<Float, Long, String>,
    viewModel: VideoDownloadViewModel,
) {
    val context = LocalContext.current
    val downloadState = remember {
        mutableStateOf(video.downloadProgress.state)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = size_16, vertical = size_8),
        shape = RoundedCornerShape(size_16)
    ) {
        Row(modifier = modifier.padding(vertical = size_8)) {
            ShowImage(
                modifier = modifier.padding(horizontal = size_16),
                imageUrl = video.thumbnailUrl.toString(),
                shouldVisible = video.downloadProgress.progress == 100
            )
            ShowTitle(
                modifier = modifier
                    .weight(1f)
                    .padding(top = size_8, end = size_8),
                video = video,
                progress = progress
            )
            if (video.downloadProgress.progress != 100) {
                ShowPlayPauseIcon(modifier = modifier, state = video.downloadProgress.state) {
                    when (downloadState.value) {
                        DownloadState.PAUSED -> {
                            viewModel.resumeDownload(
                                context = context,
                                url = video.videoUrl.toString(),
                                downloadedBytes = video.downloadProgress.bytesDownloaded,
                                fileName = video.title,
                                baseUrl = video.baseUrl
                            )
                            downloadState.value = DownloadState.DOWNLOADING // Update the state
                        }

                        DownloadState.DOWNLOADING -> {
                            viewModel.pauseVideoService(
                                context = context, baseUrl = video.baseUrl
                            )
                            downloadState.value = DownloadState.PAUSED // Update the state
                        }

                        DownloadState.FAILED -> {
                            viewModel.resumeDownload(
                                context = context,
                                url = video.videoUrl.toString(),
                                downloadedBytes = video.downloadProgress.bytesDownloaded,
                                fileName = video.title,
                                baseUrl = video.baseUrl
                            )
                            downloadState.value = DownloadState.DOWNLOADING // Update the state
                        }

                        else -> {}

                    }
                }
            }
        }
    }
}

@Composable
fun ShowPlayPauseIcon(
    modifier: Modifier, state: DownloadState, onPauseResumeClick: (DownloadState) -> Unit
) {
    Box(
        modifier = modifier
            .height(size_84)
            .padding(end = size_8),
        contentAlignment = Alignment.BottomStart
    ) {
        Row {
            if (state != DownloadState.COMPLETED) {
                IconButton(
                    modifier = modifier.size(size_30),
                    onClick = { onPauseResumeClick(state) }) {
                    Icon(
                        painter = painterResource(
                            id = when (state) {
                                DownloadState.DOWNLOADING -> R.drawable.ic_play_arrow_icon
                                DownloadState.PAUSED -> R.drawable.ic_pause_icon
                                DownloadState.FAILED -> R.drawable.ic_refresh_icon
                                else -> R.drawable.ic_refresh_icon
                            }
                        ),
                        contentDescription = "Play Icon",
                    )
                }
            }
        }
    }
}

@Composable
fun ShowTitle(modifier: Modifier, video: LocalVideo, progress: Triple<Float, Long, String>) {
    Column(modifier = modifier) {
        Text(
            text = video.title.toString(), style = YoutubeTypography.titleSmall.copy(
                fontSize = font_12,
                fontWeight = FontWeight.W700,
            ), maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        if (video.downloadProgress.progress != 100) {
            if (progress.third.isNotEmpty() && progress.second != 0L) {
                Spacer(modifier = modifier.height(size_8))
                Text(
                    text = "${progress.second.getFileSize()} / ${progress.third}",
                    style = YoutubeTypography.titleSmall.copy(
                        fontSize = font_12, fontWeight = FontWeight.W700
                    )
                )
                Spacer(modifier = modifier.height(size_8))
                if (video.downloadProgress.state == DownloadState.COMPLETED) {
                    // Hide the progress indicator if the download is complete
                    Spacer(modifier = modifier.height(size_32)) // Or keep it as it is
                } else {
                    DownloadProgress(downloaded = progress.first / 100)
                }
            } else {
                Spacer(modifier = modifier.height(size_32))
                LinearProgressIndicator()
            }
        }
    }
}

@Composable
fun ShowImage(
    modifier: Modifier = Modifier, imageUrl: String, shouldVisible: Boolean
) {
    Card(
        modifier = modifier.size(size_80), shape = RoundedCornerShape(size_8)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "image",
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (shouldVisible) {
                    Card(
                        shape = RoundedCornerShape(size_8),
                        modifier = Modifier
                            .size(size_24)
                            .align(Alignment.Center),
                        colors = CardColors(
                            contentColor = Color.White,
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            disabledContentColor = Color.Gray,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_play_arrow_icon),
                                contentDescription = "Play Icon",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DownloadProgress(downloaded: Float) {
    LinearProgressIndicator(
        progress = { downloaded },
        strokeCap = StrokeCap.Square,
        gapSize = size_0,
        color = dark_onPrimaryContainer
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    ShowImage(modifier = Modifier, imageUrl = "", shouldVisible = true)
}