package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import com.youtube.domain.model.Video
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
import com.youtube.youtube_downloader.presenter.ui.theme.size_18
import com.youtube.youtube_downloader.presenter.ui.theme.size_20
import com.youtube.youtube_downloader.presenter.ui.theme.size_30
import com.youtube.youtube_downloader.presenter.ui.theme.size_32
import com.youtube.youtube_downloader.presenter.ui.theme.size_5
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_80
import com.youtube.youtube_downloader.presenter.ui.theme.size_84
import com.youtube.youtube_downloader.util.getFileSize

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VideoDownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoDownloadViewModel = hiltViewModel(),
    onPauseResumeClick: (Video) -> Unit
) {
    val context = LocalContext.current
    val downloadState = remember {
        mutableStateOf(DownloadState.DOWNLOADING)
    }

    LaunchedEffect(Unit) {
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction(PROGRESS_DATA)
        mIntentFilter.addAction(DOWNLOAD_COMPLETE)
        context.registerReceiver(viewModel.mReceiver, mIntentFilter, Context.RECEIVER_NOT_EXPORTED)
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
            val videos = (result.data as List<LocalVideo>)
            VideoScreen(
                modifier = modifier,
                videos = videos,
                onRemove = { position ->
                    viewModel.deleteVideo(position)
                },
                onLike = { position -> },
                progress = progress,
                onPauseClickListener = { state, video ->
                    downloadState.value = state
                    viewModel.updateVideo(
                        video.copy(
                            downloadProgress = video.downloadProgress.copy(
                                state = state
                            )
                        )
                    )
                    when (state) {
                        DownloadState.PAUSED -> {
                            viewModel.resumeDownload(
                                context = context,
                                url = video.videoUrl.toString(),
                                downloadedBytes = video.downloadProgress.bytesDownloaded,
                                fileName = video.title,
                                baseUrl = video.baseUrl
                            )
                            viewModel.updateVideo(
                                video.copy(
                                    downloadProgress = video.downloadProgress.copy(
                                        state = DownloadState.DOWNLOADING
                                    )
                                )
                            )
                        }

                        DownloadState.COMPLETED -> {}
                        DownloadState.DOWNLOADING -> {
                            viewModel.pauseVideoService(context = context)
                            viewModel.updateVideo(
                                video.copy(
                                    downloadProgress = video.downloadProgress.copy(
                                        state = DownloadState.PAUSED
                                    )
                                )
                            )
                        }

                        else -> {}
                    }
                },
            )
        }
    }
}

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    videos: List<LocalVideo>,
    onRemove: (Int) -> Unit,
    onLike: (Int) -> Unit,
    progress: Triple<Float, Long, String>,
    onPauseClickListener: (DownloadState, LocalVideo) -> Unit,
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
                        modifier = modifier,
                        progress = progress,
                        onPauseClickListener = { state -> onPauseClickListener(state, video) })
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
        Icon(
            Icons.Default.Delete, contentDescription = "delete"
        )
        Spacer(modifier = Modifier)
        Icon(
            imageVector = Icons.Default.Favorite, contentDescription = "Archive"
        )
    }
}

@Composable
fun VideoItemView(
    modifier: Modifier = Modifier,
    video: LocalVideo,
    progress: Triple<Float, Long, String>,
    onPauseClickListener: (DownloadState) -> Unit,
) {
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
                state = video.downloadProgress.state
            )
            ShowTitle(
                modifier = modifier
                    .weight(1f)
                    .padding(top = size_8, end = size_8),
                video = video,
                progress = progress
            )
            ShowPlayPauseIcon(modifier = modifier, state = video.downloadProgress.state) {
                onPauseClickListener(it)
            }
        }
    }
}

@Composable
fun ShowPlayPauseIcon(
    modifier: Modifier,
    state: DownloadState,
    onPauseResumeClick: (DownloadState) -> Unit
) {
    Box(
        modifier = modifier
            .height(size_84)
            .padding(end = size_8),
        contentAlignment = Alignment.BottomStart
    ) {
        Row {
            if (state != DownloadState.COMPLETED) {
                IconButton(modifier = modifier.size(size_30), onClick = {}) {
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
                        modifier = modifier.clickable {
                            onPauseResumeClick(state)
                    })
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
        Spacer(modifier = modifier.height(size_8))
        if (progress.third.isNotEmpty() && progress.second != 0L) {
            Text(
                text = "${progress.second.getFileSize()} / ${progress.third}",
                style = YoutubeTypography.titleSmall.copy(
                    fontSize = font_12, fontWeight = FontWeight.W700
                )
            )
            Spacer(modifier = modifier.height(size_8))
            if (video.downloadProgress.state != DownloadState.COMPLETED && video.downloadProgress.state != DownloadState.FAILED) {
                DownloadProgress(
                    downloaded = progress.first / 100, modifier = modifier
                )
            }
        } else {
            Spacer(modifier = modifier.height(size_32))
            LinearProgressIndicator()
        }
    }
}

@Composable
fun ShowImage(modifier: Modifier, imageUrl: String, state: DownloadState) {
    Card(
        modifier = modifier.size(size_80), shape = RoundedCornerShape(size_8)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            AsyncImage(
                model = imageUrl, contentDescription = "image", contentScale = ContentScale.Crop
            )
            if (state == DownloadState.COMPLETED) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(size_5)
                        .size(size_20),
                    colors = CardColors(
                        contentColor = Color.Black,
                        containerColor = Color.White,
                        disabledContentColor = Color.Gray,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),

                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play_arrow_icon),
                        contentDescription = "Play Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(size_18)
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadProgress(downloaded: Float, modifier: Modifier) {
    LinearProgressIndicator(
        progress = { downloaded },
        strokeCap = StrokeCap.Square,
        gapSize = size_0,
        color = dark_onPrimaryContainer
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    VideoItemView(
        modifier = Modifier,
        LocalVideo(title = "[4K] Barsaat Ki Dhun Full Video Song | Jubin Nautiyal, Gurmeet Choudhary & Karishma Sharma"),
        Triple(0f, 0L, ""), {}
    )
}