package com.youtube.youtube_downloader

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.youtube.youtube_downloader.ui.screen.navigation.MainNavigationScreen
import com.youtube.youtube_downloader.ui.theme.YoutubeDownloaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        setContent {
            YoutubeDownloaderTheme {
                MainNavigationScreen(modifier = Modifier)
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissions.any {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissionsLauncher.launch(permissions)
        }
    }
}


@Composable
fun MainScreen(
    modifier: Modifier,
    viewModel: MainViewModel
) {
    val result = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("YouTube Downloader")

        Button(onClick = {
            viewModel.downloadAsync(
                functionName = "download_video",
                "https://www.youtube.com/watch?v=GGvM28rWqWc",
                result = result
            )
        }) {
            Text("Download Video")
        }

        Button(onClick = {
            viewModel.downloadAsync(
                "download_audio",
                "https://www.youtube.com/watch?v=GGvM28rWqWc",
                result = result
            )
        }) {
            Text("Download Audio")
        }

        Button(onClick = {
            viewModel.downloadAsync(
                "download_subtitles",
                "https://www.youtube.com/watch?v=GGvM28rWqWc",
                "en",
                "captions.txt",
                result = result
            )
        }) {
            Text("Download Subtitles")
        }

        Button(onClick = {
            viewModel.downloadAsync(
                "download_playlist",
                "https://www.youtube.com/playlist?list=GGvM28rWqWc",
                result = result
            )
        }) {
            Text("Download Playlist")
        }

        Button(onClick = {
            viewModel.downloadAsync(
                "download_channel",
                "https://www.youtube.com/@examplechannel",
                result = result
            )
        }) {
            Text("Download Channel Videos")
        }

        Text(text = result.value)
    }
}