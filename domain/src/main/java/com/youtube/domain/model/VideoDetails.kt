package com.youtube.domain.model

import com.youtube.domain.utils.Constant.NOTHING

data class VideoDetails(
    var resolution: String? = NOTHING,
    var url: String? = NOTHING,
    var size: String? = NOTHING
) {
    override fun toString(): String {
        return "VideoDetails(resolution=$resolution, size=$size)"
    }
}