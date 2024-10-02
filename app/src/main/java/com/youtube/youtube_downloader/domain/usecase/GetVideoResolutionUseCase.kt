package com.youtube.youtube_downloader.domain.usecase

import com.youtube.youtube_downloader.data.repository.PythonScriptRepository
import com.youtube.youtube_downloader.util.PythonMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetVideoResolutionUseCase @Inject constructor(
    private val pythonScriptRepository: PythonScriptRepository
) {
    suspend operator fun invoke(url: String, resolution: String): String {
        var result: String
        withContext(Dispatchers.IO) {
            val details =
                pythonScriptRepository.downloadAsync(
                    PythonMethod.VIDEO_RESOLUTION.title,
                    url,
                    resolution
                ).toString()
            result = details
        }
        return result
    }
}