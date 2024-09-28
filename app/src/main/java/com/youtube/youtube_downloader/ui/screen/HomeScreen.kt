package com.youtube.youtube_downloader.ui.screen

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.youtube.youtube_downloader.MainViewModel
import com.youtube.youtube_downloader.UiState
import com.youtube.youtube_downloader.data.model.Video
import com.youtube.youtube_downloader.ui.theme.onPrimary

@Composable
fun HomeScreen(videoUrl: String = "", viewModel: MainViewModel) {
    LaunchedEffect(key1 = videoUrl) {
        viewModel.getVideoDetails(videoUrl)
    }
    val uiState = viewModel.videoDetails.collectAsState().value
    val context = LocalContext.current
    when (uiState) {
        is UiState.Success -> {
            MainHomeScreen((uiState.data as Video), viewModel)
        }

        is UiState.Error -> {
            Toast.makeText(context, "Error : ${uiState.message}", Toast.LENGTH_SHORT).show()
        }

        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = onPrimary)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainHomeScreen(video: Video, viewModel: MainViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier) {
            PlayerScreen(
                video = video,
                viewModel = viewModel,
                isDownloaded = true
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Title(title = video.title.toString(), thumbnailUrl = video.thumbnailUrl.toString())
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                startIndent = 1.dp,
                thickness = 1.dp,
                color = onPrimary
            )
            ShowVideoDetails(video = video)
        }
    }
}

@Composable
fun ShowVideoDetails(video: Video) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Card(shape = RoundedCornerShape(16.dp)) {

        }
    }
}


@Composable
fun Title(title: String, thumbnailUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = thumbnailUrl,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.FillBounds
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .weight(7f),
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
            color = onPrimary,
            maxLines = 2
        )
    }
}