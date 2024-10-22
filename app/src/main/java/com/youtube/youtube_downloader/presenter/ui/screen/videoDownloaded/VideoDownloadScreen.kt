package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.utils.Constant.CLICK_TO_WATCH
import com.youtube.domain.utils.Constant.TRY_AGAIN
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.dark_onPrimaryContainer
import com.youtube.youtube_downloader.presenter.ui.theme.dark_tertiary
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.size_0
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_24
import com.youtube.youtube_downloader.presenter.ui.theme.size_30
import com.youtube.youtube_downloader.presenter.ui.theme.size_32
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_80
import com.youtube.youtube_downloader.presenter.ui.theme.size_84

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun VideoDownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoDownloadViewModel,
    onMoreOptionClick: (Int, Video) -> Unit,
    onPlayVideoClickListener: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.getAllVideos()
    }
    when (val result = viewModel.videos.collectAsState().value) {
        is UiState.Error -> {}
        UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val videos = result.data
            VideoScreen(
                modifier = modifier, videos = videos as List<Video>,
                onMoreOptionClick = { position, video ->
                    onMoreOptionClick(position, video)
                },
                viewModel = viewModel,
                onPlayVideoClickListener = { id ->
                    onPlayVideoClickListener(id)
                }
            )
        }

        else -> {}
    }
}

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    videos: List<Video>,
    viewModel: VideoDownloadViewModel,
    onMoreOptionClick: (Int, Video) -> Unit,
    onPlayVideoClickListener: (String) -> Unit
) {
    LazyColumn {
        itemsIndexed(videos) { index, video ->
            VideoItemView(
                modifier = modifier,
                video = video,
                viewModel = viewModel,
                onMoreOptionClick = { onMoreOptionClick(index, video) },
                onPlayVideoClickListener = {
                    onPlayVideoClickListener(it)
                }
            )
        }
    }
}

@Composable
fun VideoItemView(
    modifier: Modifier = Modifier,
    video: Video,
    viewModel: VideoDownloadViewModel,
    onMoreOptionClick: () -> Unit,
    onPlayVideoClickListener: (String) -> Unit
) {
    val downloadState = remember {
        mutableStateOf(video.state)
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
                shouldVisible = video.downloadProgress.progress == 100,
                onPlayVideoClickListener = {
                    onPlayVideoClickListener(video.id.toString())
                }
            )
            ShowTitle(
                modifier = modifier
                    .weight(1f)
                    .padding(top = size_8, end = size_8),
                video = video
            )
            if (video.downloadProgress.progress != 100) {
                ShowPlayPauseIcon(
                    modifier = modifier,
                    state = video.state
                ) {
                    when (downloadState.value) {
                        DownloadState.PAUSED -> {
                            downloadState.value = DownloadState.DOWNLOADING // Update the state
                            viewModel.resumeDownload(video = video)
                        }

                        DownloadState.DOWNLOADING -> {
                            downloadState.value = DownloadState.PAUSED // Update the state
                            viewModel.pauseVideoService(
                                video = video
                            )
                        }

                        DownloadState.FAILED, DownloadState.PENDING -> {
                            downloadState.value = DownloadState.DOWNLOADING // Update the state
                            viewModel.resumeDownload(video = video)
                        }

                        else -> {}

                    }
                }
            } else {
                Box(
                    modifier = modifier
                        .height(size_84)
                        .padding(end = size_8),
                    contentAlignment = Alignment.BottomStart
                ) {
                    IconButton(
                        modifier = modifier.size(size_30),
                        onClick = onMoreOptionClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Option"
                        )
                    }
                }
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
fun ShowTitle(
    modifier: Modifier, video: Video
) {
    Column(modifier = modifier) {
        Text(
            text = video.title.toString(), style = YoutubeTypography.titleSmall.copy(
                fontSize = font_12,
                fontWeight = FontWeight.W700,
            ), maxLines = 2, overflow = TextOverflow.Ellipsis
        )

        ShowProgressBar(modifier = modifier, video = video)
    }
}

@Composable
fun ShowProgressBar(modifier: Modifier, video: Video) {
    when {
        video.downloadProgress.progress != 100 || video.state == DownloadState.DOWNLOADING -> {
            with(video.downloadProgress) {
                if (megaBytesDownloaded.isNotEmpty() && (this.progress != 0)) {
                    Spacer(modifier = modifier.height(size_8))
                    Text(
                        text = "$megaBytesDownloaded / $totalMegaBytes",
                        style = YoutubeTypography.titleSmall.copy(
                            fontSize = font_12, fontWeight = FontWeight.W700
                        )
                    )
                    Spacer(modifier = modifier.height(size_8))
                    if (video.state == DownloadState.COMPLETED) {
                        Spacer(modifier = modifier.height(size_32)) // Or keep it as it is
                    } else {
                        DownloadProgress(downloaded = this.progress.toFloat() / 100)
                    }
                }
            }
        }

        video.state == DownloadState.FAILED || video.state == DownloadState.PENDING -> {
            Text(
                text = TRY_AGAIN,
                modifier = modifier.padding(top = size_16),
                style = YoutubeTypography.titleSmall.copy(
                    fontSize = font_12, fontWeight = FontWeight.W700
                )
            )
        }

        video.state == DownloadState.COMPLETED -> {
            Box(contentAlignment = Alignment.BottomEnd) {
                Text(
                    text = CLICK_TO_WATCH, style = YoutubeTypography.titleSmall.copy(
                        fontSize = font_12, fontWeight = FontWeight.W700, color = dark_tertiary
                    )
                )
            }
        }

        else -> {
            Spacer(modifier = modifier.height(size_32))
            LinearProgressIndicator()
        }
    }
}

@Composable
fun ShowImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    shouldVisible: Boolean,
    onPlayVideoClickListener: () -> Unit
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
                        ),
                        onClick = {
                            onPlayVideoClickListener()
                        }
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
}