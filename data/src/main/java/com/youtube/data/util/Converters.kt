package com.youtube.data.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.youtube.domain.model.DownloadProgress
import com.youtube.domain.model.DownloadState
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val gson = Gson()


    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromDownloadProgress(progress: DownloadProgress): String {
        return gson.toJson(progress)
    }

    @TypeConverter
    fun toDownloadProgress(progress: String): DownloadProgress {
        val type = object : TypeToken<DownloadProgress>() {}.type
        return gson.fromJson(progress, type)
    }

    @TypeConverter
    fun toDownloadState(state: DownloadState): String {
        return gson.toJson(state)
    }

    @TypeConverter
    fun fromDownloadState(state: String): DownloadState {
        val type = object : TypeToken<DownloadState>() {}.type
        return gson.fromJson(state, type)
    }
}
