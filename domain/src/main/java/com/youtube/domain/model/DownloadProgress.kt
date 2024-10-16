package com.youtube.domain.model

import android.net.Uri

data class DownloadProgress(
    var megaBytesDownloaded: String = "0",
    var progress: Int = 0,
    var percentageDisplay: String = "0",
    var totalMegaBytes: String = "0",
    var bytesDownloaded: Long = 0L,
    var totalBytes: Long = 0L,
    var state: DownloadState = DownloadState.PENDING,
    var uri: String = Uri.EMPTY.toString()
) {
    companion object {
        val EMPTY: DownloadProgress
            get() = DownloadProgress(
                megaBytesDownloaded = "0",
                progress = 0,
                percentageDisplay = "0",
                totalMegaBytes = "0",
                bytesDownloaded = 0L,
                totalBytes = 0L,
                state = DownloadState.PENDING,
                uri = Uri.EMPTY.toString()
            )
    }

    override fun toString(): String {
        return "DownloadProgress(megaBytesDownloaded='$megaBytesDownloaded', percentage=$progress, percentageDisplay='$percentageDisplay', totalMegaBytes='$totalMegaBytes', bytesDownloaded=$bytesDownloaded, totalBytes=$totalBytes, state=$state, uri='$uri')"
    }

}

enum class DownloadState {
    PENDING, PAUSED, COMPLETED, DOWNLOADING, FAILED
}