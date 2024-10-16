package com.youtube.data.util

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

fun ContentResolver.getUri(name: String): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValue = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$name.mp4")
            put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValue)
    } else {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "$name.mp4")

        try {
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            file.createNewFile()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

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