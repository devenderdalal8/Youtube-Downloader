package com.youtube.youtube_downloader.presenter.ui.screen.mainActivity

import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.MainNavigationScreen
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeDownloaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        setContent {
            val navController = rememberNavController()
            intent?.let {
                handleUpcomingIntent(it)
            }
            intent.data.let { uri ->
                val itemId = when {
                    uri?.host == "www.youtube.com" && uri.pathSegments.contains("watch") -> {
                        uri.getQueryParameter("v")
                    }

                    uri?.host == "youtu.be" -> {
                        uri.lastPathSegment // For shortened URLs
                    }

                    else -> null
                }
                itemId?.let { navController.navigate("watch/${it}") }
                Log.d("TAG", "onCreate: $itemId")
            }
            YoutubeDownloaderTheme {
                MainNavigationScreen(
                    modifier = Modifier,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    private fun handleUpcomingIntent(intent: android.content.Intent) {
        when (intent.action) {
            ACTION_VIEW -> {

                intent.data?.let { uri ->
                    Log.d("TAG", "handleUpcomingIntent: 1$uri")
                    extractAndNavigateToVideo(uri)
                }
            }

            ACTION_SEND -> {
                // Handle shared content
                val sharedText = intent.getStringExtra(EXTRA_TEXT)

                sharedText?.let {
                    val uri = Uri.parse(it)
                    Log.d("TAG", "handleUpcomingIntent:2 $uri")
                    extractAndNavigateToVideo(uri)
                }
            }
        }
    }

    private fun extractAndNavigateToVideo(uri: Uri) {
        val videoId = when {
            uri.host == "www.youtube.com" && uri.pathSegments.contains("watch") -> {
                uri.getQueryParameter("v") // Get video ID from query parameter
            }

            uri.host == "youtu.be" -> {
                uri.lastPathSegment // Get video ID from the last path segment
            }

            else -> null
        }
        Log.d("TAG", "extractAndNavigateToVideo: $videoId")
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