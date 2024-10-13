package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.youtube.domain.model.DownloadState
import com.youtube.domain.model.Video
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.dark_onPrimary
import com.youtube.youtube_downloader.presenter.ui.theme.dark_onPrimaryContainer
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.size_0
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_2
import com.youtube.youtube_downloader.presenter.ui.theme.size_30
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_80
import com.youtube.youtube_downloader.presenter.ui.theme.size_84

@Composable
fun VideoDownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoDownloadViewModel = hiltViewModel(),
    onPauseResumeClick: (Video) -> Unit
) {
    when (val result = viewModel.videos.collectAsState().value) {
        is UiState.Error -> {}
        UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val videos = (result.data as List<LocalVideo>)
            VideoScreen(videos = videos, modifier = modifier)
        }
    }
}

@Composable
fun VideoScreen(modifier: Modifier = Modifier, videos: List<LocalVideo>) {
    LazyColumn {
        itemsIndexed(videos) { index, video ->
            VideoItemView(video = video, modifier = modifier)
        }
    }
}

@Composable
fun VideoItemView(modifier: Modifier = Modifier, video: LocalVideo) {
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
                    .padding(top = size_8, end = size_8), video = video
            )
            ShowPlayPauseIcon(modifier = modifier, state = video.downloadProgress.state) {}
        }
    }
}

@Composable
fun ShowPlayPauseIcon(modifier: Modifier, state: DownloadState, onPauseResumeClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(size_84)
            .padding(end = size_8),
        contentAlignment = Alignment.BottomStart
    ) {
        if (state != DownloadState.COMPLETED) {
            IconButton(modifier = modifier.size(size_30), onClick = {}) {
                Icon(
                    painter = painterResource(
                        id =
                        when (state) {
                            DownloadState.DOWNLOADING -> R.drawable.ic_play_arrow_icon
                            DownloadState.PAUSED -> R.drawable.ic_pause_icon
                            DownloadState.FAILED -> R.drawable.ic_refresh_icon
                            else -> R.drawable.ic_refresh_icon
                        }
                    ),
                    contentDescription = "Play Icon",
                    modifier = modifier.clickable {
                        onPauseResumeClick()
                    }
                )
            }
        }
    }
}

@Composable
fun ShowTitle(modifier: Modifier, video: LocalVideo) {
    Column(modifier = modifier) {
        Text(
            text = video.title.toString(),
            style = YoutubeTypography.titleSmall.copy(
                fontSize = font_12,
                fontWeight = FontWeight.W700,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = modifier.height(size_8))

        DownloadProgress(video.downloadProgress.percentage.toFloat(), modifier = modifier)
    }
}

@Composable
fun ShowImage(modifier: Modifier, imageUrl: String, state: DownloadState) {
    Card(
        modifier = modifier.size(size_80),
        shape = RoundedCornerShape(size_8)
    ) {
        AsyncImage(
            model = imageUrl, contentDescription = "image", contentScale = ContentScale.Crop
        )
//        ShowPlayPauseIcon(modifier = modifier, state = state) {}
    }
}

@Composable
fun DownloadProgress(downloaded: Float, modifier: Modifier) {
    LinearProgressIndicator(
        progress = { downloaded },
        modifier = modifier.height(size_2),
        color = dark_onPrimary,
        trackColor = dark_onPrimaryContainer,
        strokeCap = StrokeCap.Square,
        gapSize = size_0,

    ){

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    VideoItemView(
        modifier = Modifier,
        LocalVideo(title = "[4K] Barsaat Ki Dhun Full Video Song | Jubin Nautiyal, Gurmeet Choudhary & Karishma Sharma")
    )
}