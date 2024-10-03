package com.youtube.youtube_downloader.presenter.ui.screen.download

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.youtube.domain.model.Video
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_30
import com.youtube.youtube_downloader.presenter.ui.theme.size_64
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_96

@Composable
fun VideoDownloadScreen(videos: List<Video>, onPauseResumeClick: (Video) -> Unit) {

}

@Composable
fun VideoItemView(modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = size_16, vertical = size_8)
    ) {
        Row(modifier = modifier.padding(vertical = size_8)) {
            ShowImage(
                modifier = modifier.padding(horizontal = size_8),
                imageUrl = "imageUrl"
            )
            ShowTitle(
                modifier = modifier,
                "Ram hsdaks asd asd dsa da asd asd dsa as asd asd asd asd"
            )
        }
    }
}

@Composable
fun ShowPlayPauseIcon(modifier: Modifier, isDownload: Boolean, onPauseResumeClick: () -> Unit) {
    Row {
        if (isDownload) {
            Card(modifier = modifier.size(size_30)) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
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
fun ShowTitle(modifier: Modifier, title: String) {
    Column(modifier = modifier.padding(end = size_8)) {
        Text(
            text = title,
            style = YoutubeTypography.titleSmall.copy(
                fontSize = font_12,
                fontWeight = FontWeight.W700,
            ),
            maxLines = 2
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "10.0MB",
                style = YoutubeTypography.titleSmall.copy(
                    fontSize = font_12,
                    fontWeight = FontWeight.W500,
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "10.0MB/20.0MB",
                style = YoutubeTypography.titleSmall.copy(
                    fontSize = font_12,
                    fontWeight = FontWeight.W500,
                ),
                modifier = modifier
            )
        }
        DownloadProgress(0f, 100.0f)
    }
}

@Composable
fun ShowImage(modifier: Modifier, imageUrl: String) {
    Card(
        modifier = modifier.size(width = size_96, height = size_64),
        shape = RoundedCornerShape(size_8)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "image",
            contentScale = ContentScale.Crop
        )
        ShowPlayPauseIcon(modifier = modifier, isDownload = true) {}
    }
}

@Composable
fun DownloadProgress(downloaded: Float, total: Float) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color.Green, Color.Blue, Color.Red)
    )

    LinearProgressIndicator(
        progress = { downloaded / total },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(gradientBrush) // Background for custom color
            .padding(horizontal = 4.dp)
    )
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    VideoItemView(modifier = Modifier)
}