package com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.youtube.domain.model.SearchVideo
import com.youtube.domain.model.VideoResponse
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeTypography
import com.youtube.youtube_downloader.presenter.ui.theme.font_12
import com.youtube.youtube_downloader.presenter.ui.theme.font_16
import com.youtube.youtube_downloader.presenter.ui.theme.gray_75
import com.youtube.youtube_downloader.presenter.ui.theme.size_0
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.presenter.ui.theme.size_4
import com.youtube.youtube_downloader.presenter.ui.theme.size_8
import com.youtube.youtube_downloader.util.calculateYearsMonthsDaysString
import com.youtube.youtube_downloader.util.convertIntoNumber

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onClickListener: (String) -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value
    var query by rememberSaveable { mutableStateOf(NOTHING) }
    var searchButtonVisibility by remember {
        mutableStateOf(query.isNotEmpty())
    }
    val context = LocalContext.current
    val isLoading = viewModel.loading.collectAsState().value
    when (uiState) {
        is UiState.Success -> {
            val data = uiState.data as VideoResponse
            MainHomeScreen(modifier = Modifier,
                onValueListener = { search ->
                    query = search
                    searchButtonVisibility = search.isNotEmpty()
                },
                query = query,
                isLoading = isLoading,
                data = data.videos ?: emptyList(),
                onClickListener = { videoUrl ->
                    onClickListener(videoUrl)
                },
                searchButtonVisibility = searchButtonVisibility,
                onSearchListener = {
                    viewModel.searchQuery(query = query)
                })
        }

        is UiState.Error -> {
            Toast.makeText(context, "Error : ${uiState.message}", Toast.LENGTH_SHORT).show()
        }

        else -> {
            MainHomeScreen(modifier = Modifier,
                onValueListener = { search ->
                    query = search
                    searchButtonVisibility = query.isNotEmpty()
                },
                query = query,
                isLoading = isLoading,
                data = emptyList(),
                onClickListener = { videoUrl ->
                    onClickListener(videoUrl)
                },
                searchButtonVisibility = searchButtonVisibility,
                onSearchListener = {
                    viewModel.searchQuery(query = query)
                })
        }
    }
}

@Composable
fun MainHomeScreen(
    modifier: Modifier = Modifier,
    onValueListener: (String) -> Unit,
    query: String,
    isLoading: Boolean,
    data: List<SearchVideo> = emptyList(),
    onClickListener: (String) -> Unit,
    searchButtonVisibility: Boolean,
    onSearchListener: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(modifier = modifier.align(alignment = Alignment.TopStart)) {
            Card(
                modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(
                    topEnd = size_0, topStart = size_0, bottomEnd = size_16, bottomStart = size_16
                ), colors = CardColors(
                    contentColor = gray_75,
                    containerColor = gray_75,
                    disabledContentColor = gray_75,
                    disabledContainerColor = gray_75
                )
            ) {
                DefaultSearchBar(
                    modifier = modifier,
                    query = query,
                    onValueListener = { onValueListener(it) },
                    searchButtonVisibility = searchButtonVisibility,
                    onSearchListener = onSearchListener
                )
            }
            ShowVideo(data = data, modifier = modifier, onClickListener = { video, _ ->
                onClickListener(video.videoUrl)
            })
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ShowVideo(
    data: List<SearchVideo>, modifier: Modifier, onClickListener: (SearchVideo, Int) -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(data) { index, video ->
            HomeVideoItem(video = video,
                modifier = modifier,
                onClickListener = { onClickListener(video, index) })
        }
    }
}

@Composable
fun HomeVideoItem(video: SearchVideo, modifier: Modifier, onClickListener: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = size_16)
            .clickable { onClickListener() },
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(video.thumbnailUrl).build(),
            contentDescription = "VideoImage",
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.height(size_8))
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = size_16)
        ) {
            Text(
                text = video.title, style = YoutubeTypography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif
                ), maxLines = 2, modifier = modifier
            )
            Spacer(modifier = Modifier.height(size_4))
            Row {
                if (video.views != 0L) {
                    Text(
                        text = "${video.views?.convertIntoNumber().toString()} views",
                        style = YoutubeTypography.titleSmall.copy(
                            fontSize = font_12, fontFamily = FontFamily.SansSerif
                        )
                    )
                }
                if (video.uploadDate.isNotEmpty()) {
                    Text(
                        text = ". ${video.uploadDate.calculateYearsMonthsDaysString()}",
                        style = YoutubeTypography.titleSmall.copy(
                            fontSize = font_12, fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DefaultSearchBar(
    modifier: Modifier,
    query: String,
    onValueListener: (String) -> Unit,
    searchButtonVisibility: Boolean,
    onSearchListener: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    TextField(
        value = query,
        onValueChange = { onValueListener(it) },
        modifier = modifier
            .fillMaxWidth()
            .padding(size_16)
            .background(Color.Transparent),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "search icon",
                modifier = modifier.padding(start = size_16, end = size_8)
            )
        },
        keyboardActions = KeyboardActions(onDone = {
            onSearchListener()
            keyboard?.hide()
        }),
        placeholder = {
            Text(
                text = "Search video / Paste url", style = YoutubeTypography.titleSmall.copy(
                    fontSize = font_16, color = Color.Gray.copy(alpha = 0.5f)
                )
            )
        },
        trailingIcon = {
            if (searchButtonVisibility) {
                Card(shape = RoundedCornerShape(size_16), onClick = { onSearchListener() }) {
                    IconButton(onClick = { onSearchListener() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(size_8),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        singleLine = true
    )
}


