package com.youtube.youtube_downloader.presenter.ui.screen.mainActivity

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.youtube.domain.utils.Constant.DOWNLOAD_COMPLETE
import com.youtube.domain.utils.Constant.DOWNLOAD_FAILED
import com.youtube.domain.utils.Constant.PROGRESS_DATA
import com.youtube.youtube_downloader.presenter.ui.screen.navigation.MainNavigationScreen
import com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded.VideoDownloadViewModel
import com.youtube.youtube_downloader.presenter.ui.theme.YoutubeDownloaderTheme
import com.youtube.youtube_downloader.util.broadcastReceiver.VideoBroadCastReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

    private val broadcastReceiver = VideoBroadCastReceiver()
    private val videoDownloadViewModel: VideoDownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        requestNotificationPermission()
        setContent {
            YoutubeDownloaderTheme {
                MainNavigationScreen(
                    modifier = Modifier,
                    intent = intent,
                    videoDownloadViewModel = videoDownloadViewModel
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        broadcastReceiver.setVideoUpdateFlow(videoDownloadViewModel.videoUpdates)
        registerBroadcastReceiver()
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

    private fun registerBroadcastReceiver() {
        val mIntentFilter = IntentFilter().apply {
            addAction(PROGRESS_DATA)
            addAction(DOWNLOAD_COMPLETE)
            addAction(DOWNLOAD_FAILED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(
                broadcastReceiver,
                mIntentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            this.registerReceiver(broadcastReceiver, mIntentFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(broadcastReceiver)
    }
}