package com.youtube.domain.usecase

import android.util.Log
import com.google.gson.Gson
import com.youtube.domain.model.Video
import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.domain.utils.PythonMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetVideoDetailsUseCase @Inject constructor(
    private val pythonScriptRepository: PythonScriptRepository
) {
    suspend operator fun invoke(url: String): Video? {
        var result: Video?
        withContext(Dispatchers.IO) {
            val details =
                pythonScriptRepository.downloadAsync(PythonMethod.VIDEO_DETAILS.title, url)
                    .toString()
            result = Gson().fromJson(details, Video::class.java)
        }
        return result
    }

}