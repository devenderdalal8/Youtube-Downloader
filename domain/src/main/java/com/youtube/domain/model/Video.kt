package com.youtube.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.youtube.domain.utils.Constant.TABLE_NAME
import java.util.UUID
import kotlin.random.Random

@Entity(tableName = TABLE_NAME)
data class Video(
    @PrimaryKey
    @SerializedName("id") val id: UUID = UUID.randomUUID(),
    @SerializedName("video_id") var videoId: String? = "",
    @SerializedName("title") var title: String? = "",
    @SerializedName("description") var description: String? = "",
    @SerializedName("thumbnail_url") var thumbnailUrl: String? = "",
    @SerializedName("video_url") var videoUrl: String? = "",
    @SerializedName("base_url") var baseUrl: String? = "",
    @SerializedName("duration") var duration: String? = "",
    @SerializedName("views") var views: String = "",
    @SerializedName("likes") var likes: String? = "",
    @SerializedName("resolution") var resolution: List<String> = arrayListOf(),
    @SerializedName("upload_date") var uploadDate: String = "",
    @SerializedName("channel_url") var channelUrl: String? = "",
    @SerializedName("channel_id") var channelId: String? = "",
    @SerializedName("size") var size: String? = "",
    @SerializedName("content_length") var length: Long? = 0L,
    @SerializedName("downloaded") var isDownloaded: Boolean = false,
    @SerializedName("file_path") var filePath: String? = "",
    @SerializedName("error") var error: String? = "",
    @SerializedName("selectedResolution") var selectedResolution: String? = "",
    @SerializedName("isExpire") var isExpire: Boolean = false,
    @SerializedName("selectedVideoUrl") var selectedVideoUrl: String? = "",
    @Embedded var downloadProgress: DownloadProgress = DownloadProgress.EMPTY,
    var state: DownloadState = DownloadState.PENDING,
    @SerializedName("notification_id") val notificationId: Int = Random.nextInt(Int.MAX_VALUE),
    @SerializedName("video_type") var videoType: VideoType = VideoType.VIDEO
) {
    override fun toString(): String {
        return "Video(id=$id, videoId=$videoId, title=$title, thumbnailUrl=$thumbnailUrl, videoUrl=$videoUrl, baseUrl=$baseUrl, duration=$duration, views='$views', likes=$likes, resolution=$resolution, uploadDate='$uploadDate', channelUrl=$channelUrl, channelId=$channelId, size=$size, length=$length, isDownloaded=$isDownloaded, downloadedPath=$filePath, error=$error, selectedResolution=$selectedResolution, isExpire=$isExpire, selectedVideoUrl=$selectedVideoUrl, downloadProgress=$downloadProgress, state=$state, notificationId=$notificationId)"
    }
}