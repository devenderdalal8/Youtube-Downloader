package com.youtube.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.youtube.domain.utils.Constant.NOTHING
import com.youtube.domain.utils.Constant.TABLE_NAME
import com.youtube.domain.utils.Constant.ZERO_LONG
import java.util.UUID
import kotlin.random.Random

@Entity(tableName = TABLE_NAME)
data class Video(
    @PrimaryKey
    @SerializedName("id") val id: UUID = UUID.randomUUID(),
    @SerializedName("video_id") var videoId: String? = NOTHING,
    @SerializedName("title") var title: String? = NOTHING,
    @SerializedName("description") var description: String? = NOTHING,
    @SerializedName("thumbnail_url") var thumbnailUrl: String? = NOTHING,
    @SerializedName("video_url") var videoUrl: String? = NOTHING,
    @SerializedName("base_url") var baseUrl: String? = NOTHING,
    @SerializedName("duration") var duration: String? = NOTHING,
    @SerializedName("views") var views: String = NOTHING,
    @SerializedName("likes") var likes: String? = NOTHING,
    @SerializedName("resolution") var resolution: List<String> = emptyList(),
    @SerializedName("upload_date") var uploadDate: String = NOTHING,
    @SerializedName("channel_url") var channelUrl: String? = NOTHING,
    @SerializedName("channel_id") var channelId: String? = NOTHING,
    @SerializedName("size") var size: String? = NOTHING,
    @SerializedName("content_length") var length: Long? = ZERO_LONG,
    @SerializedName("downloaded") var isDownloaded: Boolean = false,
    @SerializedName("file_path") var filePath: String? = NOTHING,
    @SerializedName("error") var error: String? = NOTHING,
    @SerializedName("selectedResolution") var selectedResolution: String? = NOTHING,
    @SerializedName("isExpire") var isExpire: Boolean = false,
    @SerializedName("selectedVideoUrl") var selectedVideoUrl: String? = NOTHING,
    @Embedded var downloadProgress: DownloadProgress = DownloadProgress.EMPTY,
    var state: DownloadState = DownloadState.PENDING,
    @SerializedName("notification_id") val notificationId: Int = Random.nextInt(Int.MAX_VALUE),
    @SerializedName("video_type") var videoType: VideoType = VideoType.VIDEO,
    @SerializedName("work_id") var workId: UUID = UUID.randomUUID(),
    @SerializedName("resolution_details") val resolutionDetails: List<VideoDetails>? = emptyList()
) {
    override fun toString(): String {
        return "Video(id=$id, videoId=$videoId, title=$title, baseUrl=$baseUrl, duration=$duration, views='$views', likes=$likes, uploadDate='$uploadDate', channelUrl=$channelUrl, channelId=$channelId, size=$size, length=$length, isDownloaded=$isDownloaded, filePath=$filePath, error=$error, selectedResolution=$selectedResolution, isExpire=$isExpire, downloadProgress=$downloadProgress, state=$state, notificationId=$notificationId, videoType=$videoType, workId=$workId, resolutionDetails=$resolutionDetails)"
    }
}