package com.youtube.youtube_downloader.presenter.ui.screen.mainActivity

import android.Manifest
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.MainNavigationScreen
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeDownloaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        requestNotificationPermission()
        setContent {


            YoutubeDownloaderTheme {
                MainNavigationScreen(
                    modifier = Modifier,
                    intent= intent
                )
            }
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