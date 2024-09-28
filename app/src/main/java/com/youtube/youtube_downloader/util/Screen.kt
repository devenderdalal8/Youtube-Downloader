package com.youtube.youtube_downloader.util

import com.youtube.youtube_downloader.R

enum class Screen {
    HOME,
    LOGIN,
    DOWNLOAD,
    SEARCH,
    CHANNELS,
    PLAYLIST,
    SETTING
}

sealed class Route(val route: String) {
    data object Home : Route(Screen.HOME.name)
    data object Download : Route(Screen.DOWNLOAD.name)
    data object PlayList : Route(Screen.PLAYLIST.name)
    data object Channels : Route(Screen.CHANNELS.name)
    data object Setting : Route(Screen.SETTING.name)
}

sealed class BottomNavScreen(val route: String, val title: String, val icon: Int) {
    data object Home : BottomNavScreen("home", "Home", R.drawable.ic_home_icon)
    data object PlayList : BottomNavScreen("PlayList", "PlayList", R.drawable.ic_playlist_icon)
    data object Search : BottomNavScreen("search", "Search", R.drawable.ic_search_icon)
    data object Channels : BottomNavScreen("Channels", "Channels", R.drawable.ic_chennel_icon)
    data object Setting : BottomNavScreen("Setting", "Setting", R.drawable.ic_settings_icon)
}