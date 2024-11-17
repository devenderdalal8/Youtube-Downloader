package com.youtube.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Long.getFileSize(): String {
    return withContext(Dispatchers.IO) {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var fileSize = this@getFileSize.toDouble()
        var unitIndex = 0
        while (fileSize >= 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }
        "%.2f %s".format(fileSize, units[unitIndex])
    }
}