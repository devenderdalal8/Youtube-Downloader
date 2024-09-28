package com.youtube.youtube_downloader.util

enum class PythonMethod(val title: String) {
    DOWNLOAD_VIDEO("download_video"),
    DOWNLOAD_AUDIO("download_audio"),
    DOWNLOAD_SUBTITLES("download_subtitles"),
    DOWNLOAD_PLAYLIST("download_playlist"),
    DOWNLOAD_CHANNEL("download_channel"),
    VIDEO_DETAILS("video_details"),
}