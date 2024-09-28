package com.youtube.youtube_downloader.domain.usecase

import android.util.Log
import com.google.gson.Gson
import com.youtube.youtube_downloader.data.model.Video
import com.youtube.youtube_downloader.data.repository.PythonScriptRepository
import com.youtube.youtube_downloader.util.PythonMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetVideoDetailsUseCase @Inject constructor(
    private val pythonScriptRepository: PythonScriptRepository
) {
    suspend operator fun invoke(url: String): Video? {
        var result: Video? = Video()
        withContext(Dispatchers.IO) {
            val details =
                pythonScriptRepository.downloadAsync(PythonMethod.VIDEO_DETAILS.title, url)
                    .toString()
            result = Gson().fromJson(details, Video::class.java)
            Log.d("TAG", "invoke: $result")
        }
        return result
    }

}