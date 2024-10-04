package com.youtube.youtube_downloader.presenter.ui.screen.download

data class DownloadVideoModel(
    var videoUrl: String? = "",
    var title: String? = "",
    var details: List<VideoDetails> = arrayListOf(),
    var thumbnailUrl: String? = ""
) {
    override fun toString(): String {
        return "DownloadVideoModel(videoUrl=$videoUrl, title=$title, details=$details, thumbnailUrl=$thumbnailUrl)"
    }
}

data class VideoDetails(
    var resolution: String? = "",
    var url: String? = "",
    var size: String? = ""
){
    override fun toString(): String {
        return "LocalVideo(resolution=$resolution, url=$url, size=$size)"
    }
}