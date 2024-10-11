package com.youtube.domain.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.youtube.domain.model.DownloadProgress
import com.youtube.domain.utils.Constant.TABLE_NAME
import java.util.UUID

@Entity(tableName = TABLE_NAME)
data class LocalVideo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "title") val title: String? = "",
    @ColumnInfo(name = "description") val description: String? = "",
    @ColumnInfo(name = "thumbnailUrl") val thumbnailUrl: String? = "",
    @ColumnInfo(name = "videoUrl") val videoUrl: String? = "",
    @ColumnInfo(name = "baseUrl") val baseUrl: String? = "",
    @ColumnInfo(name = "videoId")val videoId: String? = "",
    @ColumnInfo(name = "duration") val duration: String? = "",
    @ColumnInfo(name = "size")var size: String? = "",
    @ColumnInfo(name = "isDownloaded") var isDownloaded: Boolean = false,
    @ColumnInfo(name = "downloadedPath") var downloadedPath: String? = "",
) {

}
