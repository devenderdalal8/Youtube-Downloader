package com.youtube.youtube_downloader.presenter.ui.screen.download

data class VideoDetails(
    var resolution: String? = "",
    var url: String? = "",
    var size: String? = ""
){
    override fun toString(): String {
        return "LocalVideo(resolution=$resolution, url=$url, size=$size)"
    }
}