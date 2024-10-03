package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.youtube.domain.model.Video
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.MainViewModel
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.HomeScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.PlayListScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.SearchScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.SettingScreen
import com.youtube.youtube_downloader.presenter.ui.screen.download.DownloadBottomSheet
import com.youtube.youtube_downloader.presenter.ui.screen.splashScreen.SplashScreen
import com.youtube.youtube_downloader.util.BottomNavScreen
import com.youtube.youtube_downloader.util.BottomSheet
import com.youtube.youtube_downloader.util.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController
) {

    val downloadBottomSheetState = rememberModalBottomSheetState()
    val activeBottomSheet = remember { mutableStateOf<BottomSheet?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val downloadResolution = remember { mutableStateOf<Video?>(null) }

    Scaffold(
        bottomBar = {
            if (navController.isBottomBarScreen()) {
                CustomBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash.route,  // Splash screen as the start
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
                    videoUrl = "https://www.youtube.com/watch?v=ulZBNRlXW7A",
                    viewModel = viewModel,
                    isSearchable = true,
                    onDownloadClicked = { videos ->
                        downloadResolution.value = videos
                        activeBottomSheet.value = BottomSheet.Download
                        coroutineScope.launch { downloadBottomSheetState.show() }
                    }
                )
            }
            composable(BottomNavScreen.Setting.route) {
                SettingScreen()
            }
            composable(BottomNavScreen.Download.route) {
                PlayListScreen()
            }
            composable(BottomNavScreen.PlayList.route) {
                PlayListScreen()
            }
            composable(BottomNavScreen.Search.route) {
                SearchScreen()
            }

            composable(
                route = "watch/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://youtu.be/{itemId}" },
                    navDeepLink { uriPattern = "https://www.youtube.com/watch?v={itemId}" }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                HomeScreen(
                    videoUrl = "https://www.youtube.com/watch?v=${itemId}",
                    viewModel = viewModel,
                    isSearchable = true,
                    onDownloadClicked = { videos ->
                        downloadResolution.value = videos
                        activeBottomSheet.value = BottomSheet.Download
                        coroutineScope.launch { downloadBottomSheetState.show() }
                    }
                )
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
                    downloadResolution.value?.let {
                        DownloadBottomSheet(
                            modifier = modifier,
                            video = it,
                            onDismiss = {
                                coroutineScope.launch {
                                    downloadBottomSheetState.hide()
                                    activeBottomSheet.value = null
                                }
                            })
                    }
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

    return (currentRoute in listOf(
        BottomNavScreen.Home.route,
        BottomNavScreen.PlayList.route,
        BottomNavScreen.Download.route,
        BottomNavScreen.Setting.route,
        BottomNavScreen.Search.route
    ))
}
