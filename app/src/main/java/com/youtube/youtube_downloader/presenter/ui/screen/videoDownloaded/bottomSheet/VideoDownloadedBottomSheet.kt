package com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded.bottomSheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.youtube.domain.model.Video
import com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded.VideoDownloadViewModel
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.size_10
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_8


@Composable
fun VideoDownloadedBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: VideoDownloadViewModel,
    video: Video,
    onDismiss: () -> Unit
) {
    MainVideoDownloadedBottomSheet(
        modifier = modifier,
        onDismiss = onDismiss,
        onDeleteClick = {
            viewModel.deleteVideo(video = video)
            onDismiss()
        },
        onAddToFavourite = {})
}

@Composable
fun MainVideoDownloadedBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddToFavourite: () -> Unit
) {

    Column {
        Spacer(modifier = modifier.padding(size_8))
        ShowButtons(
            modifier.clickable { onAddToFavourite() },
            imageVector = Icons.Default.Favorite,
            text = "Add To Favourite"
        )
        HorizontalDivider(
            modifier = modifier.padding(horizontal = size_16, vertical = size_8),
            color = Color.Gray
        )
        ShowButtons(
            modifier.clickable { onDeleteClick() },
            imageVector = Icons.Default.Delete,
            text = "Delete"
        )
        HorizontalDivider(
            modifier = modifier.padding(horizontal = size_16, vertical = size_8),
            color = Color.Gray
        )
        ShowButtons(
            modifier.clickable { onDismiss() },
            imageVector = Icons.Default.Clear,
            text = "Close"
        )
        Spacer(modifier = modifier.padding(size_8))
    }
}

@Composable
fun ShowButtons(modifier: Modifier = Modifier, imageVector: ImageVector, text: String) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row {
            Icon(
                imageVector = imageVector,
                contentDescription = "Button",
                modifier = modifier.padding(end = size_10)
            )
            Text(text = text, style = YoutubeTypography.titleMedium)
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    MainVideoDownloadedBottomSheet(onDismiss = {}, onAddToFavourite = {}, onDeleteClick = {})
}