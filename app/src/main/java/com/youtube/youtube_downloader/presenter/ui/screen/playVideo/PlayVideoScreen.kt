package com.youtube.youtube_downloader.presenter.ui.screen.playVideo

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.youtube.domain.model.Video
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.MainViewModel
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.screen.player.PlayerScreen
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.util.Constant
import com.youtube.youtube_downloader.util.convertIntoNumber
import com.youtube.youtube_downloader.util.getTimeDifference

@Composable
fun PlayVideoScreen(
    navController: NavController,
    videoUrl: String = NOTHING,
    videoId: String = NOTHING,
    isDownloaded: Boolean = false,
    viewModel: MainViewModel = hiltViewModel(),
    onDownloadClicked: (Video) -> Unit
) {
    LaunchedEffect(key1 = videoUrl) {
        if (!isDownloaded) {
            viewModel.getVideoDetails(videoUrl)
        } else {
            viewModel.getDownloadedVideo(videoId)
        }
    }
    val uiState = viewModel.videoDetails.collectAsState().value
    val fileSize = viewModel.size.collectAsState().value.first
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var previousFullScreenState by rememberSaveable { mutableStateOf(isFullScreen) }

    LaunchedEffect(isFullScreen) {
        if (isFullScreen != previousFullScreenState) {
            previousFullScreenState = isFullScreen

            val activity = context as? Activity
            val window = activity?.window
            val decorView = window?.decorView
            val windowInsetsController =
                window?.let { WindowInsetsControllerCompat(it, decorView!!) }

            if (isFullScreen) {
                window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    when (uiState) {
        is UiState.Success -> {
            val data = uiState.data as Video
            Scaffold(floatingActionButton = {
                if (!isFullScreen && !isDownloaded) {
                    FloatingActionButton(
                        onClick = { onDownloadClicked(data) },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_file_download_icon),
                            contentDescription = NOTHING,
                        )
                    }
                }
            }) { paddingValue ->
                MainHomeScreen(video = data,
                    modifier = Modifier.padding(paddingValue),
                    size = fileSize,
                    viewModel = viewModel,
                    isFullScreen = isFullScreen,
                    isDownloaded = isDownloaded,
                    onFullScreenChangeListener = { fullScreen ->
                        if (fullScreen != isFullScreen) {
                            isFullScreen = fullScreen
                        }
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

        is UiState.Nothing -> {}
    }

    BackHandler(isFullScreen) {
        isFullScreen = false
        (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        navController.popBackStack()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainHomeScreen(
    video: Video,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    size: String,
    isFullScreen: Boolean,
    isDownloaded: Boolean,
    onFullScreenChangeListener: (Boolean) -> Unit,
) {
    LaunchedEffect(video.id) {
        if(video.resolutionDetails?.isEmpty() == true){
            viewModel.getVideoDetails(video.resolution, video.baseUrl)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = modifier) {
            PlayerScreen(video = video, isDownloaded = isDownloaded) { fullScreen ->
                onFullScreenChangeListener(fullScreen)
            }
            if (!isFullScreen) {
                Spacer(modifier = modifier.padding(size_8))
                Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                    Title(
                        title = video.title.toString(), thumbnailUrl = video.thumbnailUrl.toString()
                    )
                    Spacer(modifier = modifier.padding(size_8))
                    HorizontalDivider(thickness = 1.dp)
                    Spacer(modifier = modifier.padding(size_8))
                    ShowVideoDetails(video = video, size = size)
                    HorizontalDivider(
                        thickness = 1.dp, modifier = modifier.padding(vertical = size_8)
                    )
                    VideoDescription(description = video.description, modifier = modifier)
                }
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
                    contentDescription = NOTHING,
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
fun ShowVideoDetails(video: Video, size: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (video.likes != null) {
            VideoDetails(video.likes.toString(), "Likes")
        }
        if (video.views.isNotEmpty()) {
            VideoDetails(video.views.toLong().convertIntoNumber(), "Views")
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
