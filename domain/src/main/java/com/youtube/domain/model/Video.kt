package com.youtube.domain.model

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("title") var title: String? = "",
    @SerializedName("description") var description: String? = "",
    @SerializedName("thumbnail_url") var thumbnailUrl: String? = "",
    @SerializedName("video_url") var videoUrl: String? = "",
    @SerializedName("base_url") var baseUrl: String? = "",
    @SerializedName("video_id") var videoId: String? = "",
    @SerializedName("duration") var duration: String? = "",
    @SerializedName("views") var views: String? = "",
    @SerializedName("likes") var likes: String? = "",  // Nullable, as it can be null
    @SerializedName("resolution") var resolution: List<String> = arrayListOf(),
    @SerializedName("resolutionList") var resolutionList: Map<String, String> = mapOf(),
    @SerializedName("upload_date") var uploadDate: String = "",
    @SerializedName("channel_url") var channelUrl: String? = "",
    @SerializedName("channel_id") var channelId: String? = "",
    @SerializedName("size") var size: String? = "",
    @SerializedName("downloaded") var isDownloaded: Boolean = false,
    @SerializedName("downloaded_path") var downloadedPath: String? = "",
) {
    override fun toString(): String {
        return "Video(title=$title, size = $size ,thumbnailUrl=$thumbnailUrl , videoUrl = $videoUrl ,duration=$duration, views=$views, likes=$likes, resolution=$resolution, resolutionList=$resolutionList, uploadDate=$uploadDate, channelUrl=$channelUrl, channelId=$channelId)"
    }
}