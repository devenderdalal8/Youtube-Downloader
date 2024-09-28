package com.youtube.youtube_downloader.ui.screen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.youtube.youtube_downloader.MainViewModel
import com.youtube.youtube_downloader.ui.screen.ChannelScreen
import com.youtube.youtube_downloader.ui.screen.HomeScreen
import com.youtube.youtube_downloader.ui.screen.PlayListScreen
import com.youtube.youtube_downloader.ui.screen.SettingScreen
import com.youtube.youtube_downloader.util.BottomNavScreen
import com.youtube.youtube_downloader.util.Route


@Composable
fun MainNavigationScreen(
    modifier: Modifier,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    Scaffold(bottomBar = { CustomBottomBar(navController = navController) }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Route.Home.route) {
                HomeScreen("https://www.youtube.com/watch?v=ulZBNRlXW7A", viewModel = viewModel)
            }
            composable(BottomNavScreen.Home.route) {
                HomeScreen("https://www.youtube.com/watch?v=ulZBNRlXW7A", viewModel = viewModel)
            }
            composable(BottomNavScreen.Setting.route) {
                SettingScreen()
            }
            composable(BottomNavScreen.Channels.route) {
                ChannelScreen()
            }
            composable(BottomNavScreen.PlayList.route) {
                PlayListScreen()
            }
            composable(BottomNavScreen.Search.route) {
                PlayListScreen()
            }
            composable(Route.Download.route) {

            }
        }
    }
}