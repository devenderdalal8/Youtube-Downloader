package com.youtube.data.util

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun ContentResolver.getVideoUri(title: String): Uri? {
    // Prepare the content values for the new video
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$title.mp4")
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES) // Save in Movies directory
    }

    // Check if the video already exists
    val existingUri = query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.MediaColumns._ID),
        "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
        arrayOf("$title.mp4"),
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
        } else {
            null
        }
    }

    // Return existing URI or insert a new one
    return existingUri ?: insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
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