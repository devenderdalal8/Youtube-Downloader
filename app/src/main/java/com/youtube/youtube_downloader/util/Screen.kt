package com.youtube.youtube_downloader.util

import com.youtube.youtube_downloader.R

enum class Screen {
    HOME,
    DOWNLOAD,
    WATCHLIST,
    CHANNELS,
    PLAYLIST,
    SETTING,
    SPLASH
}

sealed class Route(val route: String) {
    data object Splash : Route(Screen.SPLASH.name)
}

enum class BottomSheet {
    Download
}

sealed class BottomNavScreen(val route: String, val title: String, val icon: Int) {
    data object Home : BottomNavScreen("home", "Home", R.drawable.ic_home_icon)
    data object PlayList : BottomNavScreen("PlayList", "PlayList", R.drawable.ic_playlist_icon)
    data object Download : BottomNavScreen("download", "Download", R.drawable.ic_cloud_download_icon)
    data object Setting : BottomNavScreen("setting", "Setting", R.drawable.ic_settings_icon)
}
