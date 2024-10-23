package com.youtube.domain.model

import com.google.gson.annotations.SerializedName

data class SearchVideo(
    @SerializedName("title") val title: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("video_id") val videoId: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("views") val views: Long? = 0L,
    @SerializedName("upload_date") val uploadDate: String,
)


data class VideoResponse(
    @SerializedName("videos") val videos: List<SearchVideo>? = emptyList(),
    @SerializedName("total") val total: Int
)