package com.youtube.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.youtube.domain.model.entity.LocalVideo
import com.youtube.domain.utils.Constant.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: LocalVideo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(video: LocalVideo)

    @Delete
    suspend fun delete(video: LocalVideo)

    @Query("SELECT * FROM video_table ")
    suspend fun getAllVideo(): List<LocalVideo>

    @Query("SELECT COUNT(*) FROM video_table WHERE id = :videoId")
    suspend fun isVideoAvailable(videoId: String): Int

    @Query("SELECT * FROM video_table WHERE id = :videoId")
    suspend fun videoById(videoId: String): LocalVideo
}