package com.youtube.youtube_downloader.ui.screen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.youtube.youtube_downloader.MainViewModel
import com.youtube.youtube_downloader.ui.screen.DownloadScreen
import com.youtube.youtube_downloader.ui.screen.HomeScreen
import com.youtube.youtube_downloader.ui.screen.PlayListScreen
import com.youtube.youtube_downloader.ui.screen.SearchScreen
import com.youtube.youtube_downloader.ui.screen.SettingScreen
import com.youtube.youtube_downloader.util.BottomNavScreen
import com.youtube.youtube_downloader.util.Route


@Composable
fun MainNavigationScreen(
    modifier: Modifier,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()

    Scaffold(bottomBar = {
        // Show bottom bar only on specific routes
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        if (currentRoute in listOf(
                BottomNavScreen.Home.route,
                BottomNavScreen.PlayList.route,
                BottomNavScreen.Channels.route,
                BottomNavScreen.Setting.route,
                BottomNavScreen.Search.route
            )
        ) {
            CustomBottomBar(navController = navController)
        }
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeScreen(
                    "https://www.youtube.com/watch?v=ulZBNRlXW7A",
                    viewModel = viewModel,
                    onDownloadClicked = { navController.navigate(Route.Download.route) }
                )
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
                DownloadScreen()
            }
        }
    }
}