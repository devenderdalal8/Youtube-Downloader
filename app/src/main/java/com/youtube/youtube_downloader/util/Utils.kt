package com.youtube.youtube_downloader.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
fun String.convertDateToYearsAndDays(): String {
    val offsetDateTime = OffsetDateTime.parse(this)

    val currentDateTime = OffsetDateTime.now()
    val years = ChronoUnit.YEARS.between(offsetDateTime, currentDateTime)
    val days = ChronoUnit.DAYS.between(offsetDateTime.plusYears(years), currentDateTime)
    return if (years > 0) {
        "$years years"
    } else {
        "$days days"
    }
}

fun Long.getFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var fileSize = toDouble()
    var unitIndex = 0
    while (fileSize >= 1024 && unitIndex < units.size - 1) {
        fileSize /= 1024
        unitIndex++
    }
    return "%.2f %s".format(fileSize, units[unitIndex])
}

