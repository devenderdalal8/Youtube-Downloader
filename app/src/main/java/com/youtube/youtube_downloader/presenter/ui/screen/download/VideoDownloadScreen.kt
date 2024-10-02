package com.youtube.youtube_downloader.presenter.ui.screen.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.youtube.youtube_downloader.data.model.Video

@Composable
fun VideoDownloadScreen(videos: List<Video>, onPauseResumeClick: (Video) -> Unit) {

}

@Composable
fun DownloadProgress(downloaded: Float, total: Float) {
    LinearProgressIndicator(
        progress = { downloaded / total },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "${downloaded}MB")
        Text(text = "${total}MB")
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    DownloadProgress(downloaded = 10f, total = 100f)
}