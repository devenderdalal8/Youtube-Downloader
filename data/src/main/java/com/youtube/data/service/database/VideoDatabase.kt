package com.youtube.data.service.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.youtube.data.dao.VideoDao
import com.youtube.data.util.Converters
import com.youtube.domain.model.Video

@Database(entities = [Video::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}