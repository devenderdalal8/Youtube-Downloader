package com.youtube.youtube_downloader.presenter.ui.screen.download

import com.youtube.domain.utils.Constant.NOTHING

data class VideoDetails(
    var resolution: String? = NOTHING,
    var url: String? = NOTHING,
    var size: String? = NOTHING
){
    override fun toString(): String {
        return "LocalVideo(resolution=$resolution, url=$url, size=$size)"
    }
}