package com.youtube.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.youtube.domain.model.Video
import java.util.UUID

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: Video)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(video: Video)

    @Delete
    suspend fun delete(video: Video)

    @Query("SELECT * FROM video_table ")
    suspend fun getVideos(): List<Video>

    @Query("SELECT COUNT(*) FROM video_table WHERE baseUrl = :baseUrl LIMIT 1")
    suspend fun isVideoAvailable(baseUrl: String): Int

    @Query("SELECT * FROM video_table WHERE baseUrl = :baseUrl LIMIT 1")
    suspend fun videoByBaseUrl(baseUrl: String): Video

    @Query("SELECT * FROM video_table WHERE id = :id LIMIT 1")
    suspend fun videoById(id: UUID): Video
}