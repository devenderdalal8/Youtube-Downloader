package com.youtube.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "TABLE_DOWNLOAD_ITEM")
data class DownloadItem(
    @PrimaryKey
    var url : String,
    var fileName:String,
    @Embedded
    var downloadProgress : DownloadProgress = DownloadProgress.EMPTY
) {
}