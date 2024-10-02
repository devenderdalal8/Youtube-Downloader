package com.youtube.youtube_downloader.data.model

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("title") val title: String? = "",
    @SerializedName("description") val description: String? = "",
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = "",
    @SerializedName("video_url") val videoUrl: String? = "",
    @SerializedName("base_url") val baseUrl: String? = "",
    @SerializedName("video_id") val videoId: String? = "",
    @SerializedName("duration") val duration: String? = "",
    @SerializedName("views") val views: String? = "",
    @SerializedName("likes") val likes: String? = "",  // Nullable, as it can be null
    @SerializedName("resolution") val resolution: List<String> = arrayListOf(),
    @SerializedName("resolutionList") val resolutionList: Map<String, String> = mapOf(),
    @SerializedName("upload_date") val uploadDate: String = "",
    @SerializedName("channel_url") val channelUrl: String? = "",
    @SerializedName("channel_id") val channelId: String? = "",
    @SerializedName("size") var size: String? = "",
    @SerializedName("downloaded") var isDownloaded: Boolean = false,
    @SerializedName("downloaded_path") var downloadedPath: String? = "",
) {
    override fun toString(): String {
        return "Video(title=$title, size = $size ,thumbnailUrl=$thumbnailUrl , videoUrl = $videoUrl ,duration=$duration, views=$views, likes=$likes, resolution=$resolution, resolutionList=$resolutionList, uploadDate=$uploadDate, channelUrl=$channelUrl, channelId=$channelId)"
    }
}