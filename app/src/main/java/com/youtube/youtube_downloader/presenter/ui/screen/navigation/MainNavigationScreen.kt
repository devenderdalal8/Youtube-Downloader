package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.youtube.youtube_downloader.presenter.MainViewModel
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.HomeScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.PlayListScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.SearchScreen
import com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.SettingScreen
import com.youtube.youtube_downloader.presenter.ui.screen.download.DownloadBottomSheet
import com.youtube.youtube_downloader.util.BottomNavScreen
import com.youtube.youtube_downloader.util.BottomSheet
import com.youtube.youtube_downloader.util.Route
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    modifier: Modifier = Modifier, viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val downloadBottomSheetState = rememberModalBottomSheetState()

    val activeBottomSheet = remember {
        mutableStateOf<BottomSheet?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(bottomBar = {
        if (navController.isBottomBarScreen()) {
            CustomBottomBar(navController = navController)
        }
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeScreen("https://www.youtube.com/watch?v=ulZBNRlXW7A",
                    viewModel = viewModel,
                    onDownloadClicked = {
                        activeBottomSheet.value = BottomSheet.Download
                        coroutineScope.launch { downloadBottomSheetState.show() }
                    })
            }
            composable(Route.Download.route) {
                PlayListScreen()
            }
            composable(BottomNavScreen.Setting.route) {
                SettingScreen()
            }
            composable(BottomNavScreen.Channels.route) {
                SettingScreen()
            }
            composable(BottomNavScreen.PlayList.route) {
                SettingScreen()
            }
            composable(BottomNavScreen.Search.route) {
                SearchScreen()
            }
            composable(Route.Download.route) {

            }
        }
    }

    if (activeBottomSheet.value != null) {
        ModalBottomSheet(
            sheetState = when (activeBottomSheet.value) {
                BottomSheet.Download -> downloadBottomSheetState
                else -> downloadBottomSheetState
            },
            onDismissRequest = { activeBottomSheet.value = null },
        ) {
            when (activeBottomSheet.value) {
                BottomSheet.Download -> {
                    DownloadBottomSheet(modifier = modifier, onDismiss = {
                        coroutineScope.launch {
                            downloadBottomSheetState.hide()
                            activeBottomSheet.value = null
                        }
                    })
                }

                else -> {

                }
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
        BottomNavScreen.Channels.route,
        BottomNavScreen.Setting.route,
        BottomNavScreen.Search.route
    ))
}
