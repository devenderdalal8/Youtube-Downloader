package com.youtube.youtube_downloader.presenter.ui.screen.mainActivity

import android.Manifest
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        requestNotificationPermission()
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
            }
            YoutubeDownloaderTheme {
                MainNavigationScreen(
                    modifier = Modifier,
                    navController = navController
                )
            }
        }
    }

    private fun handleUpcomingIntent(intent: android.content.Intent) {
        when (intent.action) {
            ACTION_VIEW -> {

                intent.data?.let { uri ->
                    extractAndNavigateToVideo(uri)
                }
            }

            ACTION_SEND -> {
                // Handle shared content
                val sharedText = intent.getStringExtra(EXTRA_TEXT)

                sharedText?.let {
                    val uri = Uri.parse(it)
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
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
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
}