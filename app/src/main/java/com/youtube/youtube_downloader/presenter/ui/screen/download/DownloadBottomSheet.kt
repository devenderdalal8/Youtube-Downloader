package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.youtube.domain.model.Video
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.dark_primary
import com.youtube.youtube_downloader.presenter.ui.theme.dark_primaryContainer
import com.youtube.youtube_downloader.presenter.ui.theme.dark_selectedBorder
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.font_14
import com.youtube.youtube_downloader.presenter.ui.theme.font_16
import com.youtube.youtube_downloader.presenter.ui.theme.gray_10
import com.youtube.youtube_downloader.presenter.ui.theme.gray_75
import com.youtube.youtube_downloader.presenter.ui.theme.size_1
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_2
import com.youtube.youtube_downloader.presenter.ui.theme.size_24
import com.youtube.youtube_downloader.presenter.ui.theme.size_64
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_96
import com.youtube.youtube_downloader.util.Constant

@Composable
fun DownloadBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    video: Video,
    viewModel: DownloadViewModel = hiltViewModel(),
    onButtonClickListener: () -> Unit
) {
    LaunchedEffect(key1 = video.videoId) {
        viewModel.getVideoDetails(video.resolution, video.baseUrl)
    }

    when (val uiState = viewModel.downloadVideoUiState.collectAsState().value) {
        is DownloadVideoUiState.Loading -> {
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DownloadVideoUiState.Success -> {
            val data = uiState.data
            MainDownloadBottomSheetScreen(
                modifier = modifier,
                data = data,
                video = video
            ) { url ->
                if (!viewModel.isVideoAvailable(video.baseUrl.toString())) {
                    viewModel.startDownload(
                        baseUrl = video.baseUrl.toString(),
                        fileName = video.title,
                        url = url,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewModel.storeVideoLocally(video)
                    }
                }
                onButtonClickListener()
                onDismiss()
            }
        }
    }
}

@Composable
fun MainDownloadBottomSheetScreen(
    modifier: Modifier = Modifier,
    data: List<VideoDetails>,
    video: Video,
    onButtonClickListener: (String) -> Unit
) {
    val context = LocalContext.current
    val videoUrl = remember {
        mutableStateOf("")
    }
    Box(
        modifier = modifier, contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()
        ) {
            ShowProfile(context = context, modifier = modifier, video = video)
            Spacer(modifier = modifier.padding(size_8))
            ShowDownloadText(modifier = modifier)
            Spacer(modifier = modifier.padding(size_8))
            VideoQualities(modifier = modifier, resolutions = data) { url ->
                videoUrl.value = url
            }
            DownloadButtonView(modifier = modifier,
                onButtonClickListener = { onButtonClickListener(videoUrl.value) })
            Mp3DownloadButtonView(modifier = modifier, onButtonClickListener = {})
        }
    }
}

@Composable
fun Mp3DownloadButtonView(modifier: Modifier, onButtonClickListener: () -> Unit) {
    Button(
        onClick = onButtonClickListener,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = size_16)
            .padding(bottom = size_24),
        colors = ButtonColors(
            contentColor = Color.White,
            containerColor = gray_75,
            disabledContentColor = Color.DarkGray,
            disabledContainerColor = Color.LightGray
        ),
        shape = RoundedCornerShape(size_8)
    ) {
        Text(
            text = Constant.AUDIO, style = YoutubeTypography.titleMedium.copy(
                fontWeight = FontWeight.W700, fontSize = font_16
            ), modifier = modifier.padding(size_8)
        )
    }
}

@Composable
fun DownloadButtonView(modifier: Modifier, onButtonClickListener: () -> Unit) {
    Button(
        onClick = onButtonClickListener,
        modifier = modifier
            .fillMaxWidth()
            .padding(size_16),
        shape = RoundedCornerShape(size_8),
        colors = ButtonColors(
            contentColor = Color.White,
            containerColor = dark_primaryContainer,
            disabledContentColor = Color.DarkGray,
            disabledContainerColor = Color.LightGray
        )
    ) {
        Text(
            text = Constant.DOWNLOAD, style = YoutubeTypography.titleMedium.copy(
                fontWeight = FontWeight.W700, fontSize = font_16
            ), modifier = modifier.padding(size_8)
        )
    }
}

@Composable
fun VideoQualities(
    modifier: Modifier, resolutions: List<VideoDetails>, onClickListener: (String) -> Unit
) {
    val isSelected = remember {
        mutableIntStateOf(-1)
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = size_96),
        modifier = modifier.padding(horizontal = size_16)
    ) {
        itemsIndexed(resolutions) { index, resolution ->
            ResolutionView(
                resolution,
                isSelected = isSelected.intValue == index,
                onClickListener = {
                    isSelected.intValue = index
                    onClickListener(resolution.url.toString())
                })
        }
    }
}

@Composable
fun ResolutionView(resolution: VideoDetails, isSelected: Boolean, onClickListener: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(size_8)
            .clickable {
                onClickListener()
            },
        shape = RoundedCornerShape(size_8),
        colors = CardColors(
            contentColor = Color.White,
            containerColor = if (!isSelected) gray_10 else gray_75,
            disabledContentColor = Color.DarkGray,
            disabledContainerColor = Color.LightGray
        ),
        border = BorderStroke(
            width = size_2, color = if (isSelected) dark_selectedBorder else Color.Transparent
        ),
    ) {
        Column {
            Text(
                text = resolution.resolution.toString(),
                style = YoutubeTypography.titleSmall.copy(
                    fontWeight = FontWeight.W700, fontSize = font_16
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = size_16)
                    .padding(top = size_16, bottom = size_8),
                textAlign = TextAlign.Center
            )
            Text(
                text = resolution.size.toString(),
                style = YoutubeTypography.titleSmall.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = font_14,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = size_8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ShowDownloadText(modifier: Modifier) {
    Text(
        text = stringResource(id = R.string.select_quality),
        style = YoutubeTypography.titleMedium,
        modifier = modifier
    )
}

@Composable
fun ShowProfile(context: Context, modifier: Modifier, video: Video) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .background(gray_75), shape = RoundedCornerShape(size_1)
    ) {
        Row(modifier = Modifier.padding(horizontal = size_16, vertical = size_8)) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.size(size_64)) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(video.thumbnailUrl).build(),
                    contentDescription = "thumbnail",
                    modifier = modifier
                        .fillMaxSize()
                        .background(
                            color = Color.Transparent, shape = RoundedCornerShape(size_8)
                        )
                        .clip(RoundedCornerShape(size_8)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = modifier.padding(end = size_8))
            Column {
                Text(
                    text = video.title.toString(),
                    maxLines = 2,
                    style = YoutubeTypography.titleSmall
                )
                Text(
                    text = stringResource(id = R.string.ready_to_save),
                    style = YoutubeTypography.titleSmall.copy(
                        fontSize = font_12, color = dark_primary
                    )
                )

            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun DownloadPreview() {
    Box(modifier = Modifier) {
        DownloadBottomSheet(onDismiss = {}, video = Video()) {}
    }
}