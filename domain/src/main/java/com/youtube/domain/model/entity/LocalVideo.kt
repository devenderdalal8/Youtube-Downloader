package com.youtube.domain.model.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.youtube.domain.model.DownloadProgress
import com.youtube.domain.utils.Constant.TABLE_NAME
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = TABLE_NAME)
data class LocalVideo constructor(
    @PrimaryKey @ColumnInfo(name = "baseUrl") var baseUrl: String = "",
    @ColumnInfo(name = "title") var title: String? = "",
    @ColumnInfo(name = "description") var description: String? = "",
    @ColumnInfo(name = "thumbnailUrl") var thumbnailUrl: String? = "",
    @ColumnInfo(name = "videoUrl") var videoUrl: String? = "",
    @ColumnInfo(name = "videoId") var videoId: String? = "",
    @ColumnInfo(name = "duration") var duration: String? = "",
    @ColumnInfo(name = "size")var size: String? = "",
    @ColumnInfo(name = "workerId") var workerId: UUID = UUID.fromString(""),
    @ColumnInfo(name = "downloadedPath") var downloadedPath: String? = "",
    @Embedded var downloadProgress: DownloadProgress = DownloadProgress.EMPTY
) {
    override fun toString(): String {
        return "LocalVideo(title=$title, thumbnailUrl=$thumbnailUrl, baseUrl=$baseUrl, videoId=$videoId, duration=$duration, size=$size, workerId=$workerId, downloadedPath=$downloadedPath, downloadProgress=$downloadProgress)"
    }
}
