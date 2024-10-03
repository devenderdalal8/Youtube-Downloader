package com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.youtube.youtube_downloader.R
import com.youtube.domain.model.Video
import com.youtube.youtube_downloader.presenter.MainViewModel
import com.youtube.youtube_downloader.presenter.UiState
import com.youtube.youtube_downloader.presenter.ui.screen.player.PlayerScreen
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.util.Constant
import com.youtube.youtube_downloader.util.convertIntoNumber
import com.youtube.youtube_downloader.util.getTimeDifference

@Composable
fun HomeScreen(
    videoUrl: String = "",
    viewModel: MainViewModel,
    isSearchable: Boolean,
    onDownloadClicked: (Video) -> Unit
) {
    LaunchedEffect(key1 = videoUrl) {
        viewModel.getVideoDetails(videoUrl)
    }
    val uiState = viewModel.videoDetails.collectAsState().value
    val fileSize = viewModel.size.collectAsState().value
    val query = remember { mutableStateOf("") }
    val context = LocalContext.current
    when (uiState) {
        is UiState.Success -> {
            val data = uiState.data as Video
            Scaffold(floatingActionButton = {
                if (data.videoUrl != null) {
                    FloatingActionButton(
                        onClick = { onDownloadClicked(data) },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_file_download_icon),
                            contentDescription = "",
                        )
                    }
                }
            }) { paddingValue ->
                MainHomeScreen(video = data,
                    modifier = Modifier.padding(paddingValue),
                    size = fileSize,
                    isSearchable = isSearchable,
                    query = query.value,
                    onValueListener = { search ->
                        query.value = search
                    })
            }
        }

        is UiState.Error -> {
            Toast.makeText(context, "Error : ${uiState.message}", Toast.LENGTH_SHORT).show()
        }

        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainHomeScreen(
    video: Video,
    modifier: Modifier = Modifier,
    size: String,
    onValueListener: (String) -> Unit,
    query: String,
    isSearchable: Boolean
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = modifier) {
            DefaultSearchBar(
                modifier = modifier,
                query = query,
                onValueListener = onValueListener,
                isSearchable = isSearchable
            )
            PlayerScreen(video = video, isDownloaded = true)
            Spacer(modifier = modifier.padding(size_8))
            Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                Title(title = video.title.toString(), thumbnailUrl = video.thumbnailUrl.toString())
                Spacer(modifier = modifier.padding(size_8))
                HorizontalDivider(thickness = 1.dp)
                Spacer(modifier = modifier.padding(size_8))
                ShowVideoDetails(video = video, size = size)
                HorizontalDivider(thickness = 1.dp, modifier = modifier.padding(vertical = size_8))
                VideoDescription(description = video.description, modifier = modifier)
            }
        }
    }
}

@Composable
fun VideoDescription(description: String?, modifier: Modifier) {
    val expanded = remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(size_8),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                expanded.value = !expanded.value
            }) {
        Column(modifier = modifier.padding(size_8)) {
            Row(
                modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Constant.DESCRIPTION, style = YoutubeTypography.titleSmall.copy(
                        fontWeight = FontWeight(700)
                    ), modifier = Modifier.padding(vertical = size_8)
                )
                Icon(
                    imageVector = if (!expanded.value) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = "",
                    modifier = modifier.padding(vertical = size_8)
                )
            }
            if (expanded.value) {
                Text(
                    text = description.toString().trim(),
                    style = YoutubeTypography.titleMedium.copy(
                        fontSize = font_12
                    )
                )
            }
        }
    }
}

@Composable
fun DefaultSearchBar(
    modifier: Modifier,
    query: String,
    onValueListener: (String) -> Unit,
    isSearchable: Boolean
) {
    if(isSearchable){
        TextField(
            value = query,
            onValueChange = { onValueListener(it) },
            modifier = modifier
                .fillMaxWidth()
                .padding(size_16)
                .background(
                    Color.Transparent
                ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "search icon",
                    modifier = modifier.padding(start = size_16, end = size_8)
                )
            },
            shape = RoundedCornerShape(size_8),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
    }
}

@Composable
fun BoxScope.DownloadButton(video: Video, onButtonClicked: () -> Unit) {
    OutlinedButton(
        onClick = { onButtonClicked() },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = size_8)
    ) {
        Text(
            text = Constant.DOWNLOAD,
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif,
        )
    }
}

@Composable
fun ShowVideoDetails(video: Video, size: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (video.likes != null) {
            VideoDetails(video.likes.toString(), "Likes")
        }
        if (video.views != null) {
            VideoDetails(video.views.convertIntoNumber(), "Views")
        }
        if (size.isNotEmpty()) {
            VideoDetails(size, "Size")
        }
        if (video.uploadDate.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VideoDetails(title = video.uploadDate.getTimeDifference(), desc = "Upload Date")
            }
        }
    }
}

@Composable
fun VideoDetails(title: String, desc: String) {
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title, fontSize = 16.sp, fontFamily = FontFamily.SansSerif
        )
        Text(
            text = desc, fontSize = 12.sp, fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun Title(title: String, thumbnailUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(size_8),
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
            text = title, style = YoutubeTypography.titleSmall, maxLines = 2
        )
    }
}
