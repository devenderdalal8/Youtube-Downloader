package com.youtube.youtube_downloader.presenter.ui.screen.download

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.font_14
import com.youtube.youtube_downloader.presenter.ui.theme.font_16
import com.youtube.youtube_downloader.presenter.ui.theme.light_downloadButton
import com.youtube.youtube_downloader.presenter.ui.theme.light_outline
import com.youtube.youtube_downloader.presenter.ui.theme.light_selectedBorder
import com.youtube.youtube_downloader.presenter.ui.theme.light_selectedContainer
import com.youtube.youtube_downloader.presenter.ui.theme.light_tertiary
import com.youtube.youtube_downloader.presenter.ui.theme.light_unselectedBorder
import com.youtube.youtube_downloader.presenter.ui.theme.size_1
import com.youtube.youtube_downloader.presenter.ui.theme.size_10
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_2
import com.youtube.youtube_downloader.presenter.ui.theme.size_30
import com.youtube.youtube_downloader.presenter.ui.theme.size_4
import com.youtube.youtube_downloader.presenter.ui.theme.size_64
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.presenter.ui.theme.size_96
import com.youtube.youtube_downloader.util.Constant

@Composable
fun DownloadBottomSheet(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    val resolution = listOf("360p", "480p", "720p", "1080p", "2048p")
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()
        ) {
            ShowProfile(context = context, modifier = modifier)
            Spacer(modifier = modifier.padding(size_8))
            ShowDownloadText(modifier = modifier)
            Spacer(modifier = modifier.padding(size_16))
            VideoQualities(modifier = modifier, resolution)
            DownloadButtonView(modifier = modifier, onButtonClickListener = {})
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
            .padding(horizontal = size_16),
        colors = ButtonColors(
            contentColor = Color.DarkGray,
            containerColor = Color.LightGray,
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
            containerColor = light_downloadButton,
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
fun VideoQualities(modifier: Modifier, resolutions: List<String>) {
    val isSelected = remember {
        mutableIntStateOf(-1)
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = size_96),
        modifier = modifier.padding(horizontal = size_16)
    ) {
        itemsIndexed(resolutions) { index, resolution ->
            ResolutionView(resolution,
                isSelected = isSelected.value == index,
                onClickListener = { isSelected.intValue = index })
        }
    }
}

@Composable
fun ResolutionView(resolution: String, isSelected: Boolean, onClickListener: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(size_8)
            .background(color = Color.White)
            .clickable {
                onClickListener()
            },
        shape = RoundedCornerShape(size_8),
        colors = CardColors(
            contentColor = Color.Black,
            containerColor = if (!isSelected) Color.Transparent else light_selectedContainer,
            disabledContentColor = Color.DarkGray,
            disabledContainerColor = Color.LightGray
        ),
        border = BorderStroke(
            width = size_2, color = if (isSelected) light_selectedBorder else light_unselectedBorder
        ),
    ) {
        Column {
            Text(
                text = resolution,
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
                text = "28 MB",
                style = YoutubeTypography.titleSmall.copy(
                    fontWeight = FontWeight.W500, fontSize = font_14, color = Color.Gray.copy()
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
        color = Color.Black
    )
}

@Composable
fun ShowProfile(context: Context, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent),
        shape = RoundedCornerShape(size_1)
    ) {
        Row(modifier = Modifier.padding(horizontal = size_16, vertical = size_8)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://i.ytimg.com/vi/-65VtGmPoi0/sddefault.jpg").build(),
                contentDescription = "thumbnail",
                modifier = modifier
                    .size(size_64)
                    .background(
                        color = Color.Transparent, shape = RoundedCornerShape(
                            size_8
                        )
                    )
            )
            Spacer(modifier = modifier.padding(end = size_8))
            Column {
                Text(
                    text = "85 Iconic MCU Moments | Marvel 85th Anniversary Compilation (4K)",
                    maxLines = 2,
                    style = YoutubeTypography.titleSmall
                )
                Text(
                    text = stringResource(id = R.string.ready_to_save),
                    style = YoutubeTypography.titleSmall.copy(
                        fontSize = font_12, color = light_tertiary
                    )
                )

            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DownloadPreview() {
    Box(modifier = Modifier) {
        DownloadBottomSheet(onDismiss = {})
    }
}