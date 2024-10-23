package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.youtube.domain.model.Video
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.PlayListScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.SettingScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.home.HomeScreen
import com.youtube.youtube_downloader.presenter.ui.screen.download.DownloadBottomSheet
import com.youtube.youtube_downloader.presenter.ui.screen.playVideo.PlayVideoScreen
import com.youtube.youtube_downloader.presenter.ui.screen.splashScreen.SplashScreen
import com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded.VideoDownloadScreen
import com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded.VideoDownloadViewModel
import com.youtube.youtube_downloader.presenter.ui.screen.videoDownloaded.bottomSheet.VideoDownloadedBottomSheet
import com.youtube.youtube_downloader.util.BottomNavScreen
import com.youtube.youtube_downloader.util.BottomSheet
import com.youtube.youtube_downloader.util.Route
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    modifier: Modifier = Modifier,
    intent: Intent,
    videoDownloadViewModel: VideoDownloadViewModel
) {
    val navController = rememberNavController()
    val downloadBottomSheetState = rememberModalBottomSheetState()
    val activeBottomSheet = remember { mutableStateOf<BottomSheet?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val selectedVideo = remember { mutableStateOf(Video()) }

    LaunchedEffect(Unit) {
        if (intent.action == ACTION_SEND) {
            val sharedUrl = intent.getStringExtra(EXTRA_TEXT)
            val url = Uri.encode(sharedUrl)
            navController.navigate("videoPlayer/$url")
        }
    }

    Scaffold(
        bottomBar = {
            if (navController.isBottomBarScreen()) {
                CustomBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash.route,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Route.Splash.route) {
                SplashScreen(modifier = modifier) {
                    navController.navigate(BottomNavScreen.Home.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            }

            composable(BottomNavScreen.Home.route) {
                HomeScreen(
                    isSearchable = true
                ) { videoUrl ->
                    // when i am searching video from home  screen
                    navController.navigate(
                        "videoPlayer/${
                            URLEncoder.encode(videoUrl, "UTF-8")
                        }"
                    )
                }
            }

            composable(BottomNavScreen.Setting.route) {
                SettingScreen()
            }

            composable(BottomNavScreen.Download.route) {
                VideoDownloadScreen(
                    viewModel = videoDownloadViewModel,
                    onMoreOptionClick = { _, video ->
                        selectedVideo.value = video
                        activeBottomSheet.value = BottomSheet.MODIFY_VIDEO
                    }) { id, isDownloaded ->
                    // video is already downloaded and now we can play offline
                    navController.navigate(
                        "videoPlayer/${URLEncoder.encode(id, "UTF-8")}/${isDownloaded}"
                    )
                }
            }

            composable(BottomNavScreen.PlayList.route) {
                PlayListScreen()
            }

            composable(
                route = "videoPlayer/{videoUrl}",
                arguments = listOf(navArgument("videoUrl") { type = NavType.StringType }),
            ) { backStackEntry ->
                val videoUrl = backStackEntry.arguments?.getString("videoUrl")
                PlayVideoScreen(
                    navController = navController,
                    videoUrl = videoUrl.toString(),
                ) { videos ->
                    selectedVideo.value = videos
                    activeBottomSheet.value = BottomSheet.Download
                    coroutineScope.launch { downloadBottomSheetState.show() }
                }
            }

            composable(
                route = "videoPlayer/{videoId}/{isDownloaded}",
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType },
                    navArgument("isDownloaded") { type = NavType.BoolType }
                ),
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId")
                val isDownloaded = backStackEntry.arguments?.getBoolean("isDownloaded")
                PlayVideoScreen(
                    navController = navController,
                    videoId = videoId.toString(),
                    isDownloaded = isDownloaded ?: false
                ) { videos ->
                    selectedVideo.value = videos
                    activeBottomSheet.value = BottomSheet.Download
                    coroutineScope.launch { downloadBottomSheetState.show() }
                }
            }

        }
    }

    if (activeBottomSheet.value != null) {
        ModalBottomSheet(
            sheetState = downloadBottomSheetState,
            onDismissRequest = { activeBottomSheet.value = null },
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        ) {
            when (activeBottomSheet.value) {
                BottomSheet.Download -> {
                    DownloadBottomSheet(
                        modifier = modifier,
                        video = selectedVideo.value,
                        onDismiss = {
                            coroutineScope.launch {
                                downloadBottomSheetState.hide()
                                activeBottomSheet.value = null
                            }
                        }) {
                        navController.navigate(BottomNavScreen.Download.route)
                    }
                }

                BottomSheet.MODIFY_VIDEO -> {
                    VideoDownloadedBottomSheet(
                        viewModel = videoDownloadViewModel,
                        modifier = modifier,
                        video = selectedVideo.value,
                        onDismiss = {
                            coroutineScope.launch {
                                downloadBottomSheetState.hide()
                                activeBottomSheet.value = null
                            }
                        })
                }

                else -> {}
            }
        }
    }
}

@Composable
fun NavHostController.isBottomBarScreen(): Boolean {
    val navBackStackEntry by this.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    return currentRoute in listOf(
        BottomNavScreen.Home.route,
        BottomNavScreen.PlayList.route,
        BottomNavScreen.Download.route,
        BottomNavScreen.Setting.route,
    )
}
