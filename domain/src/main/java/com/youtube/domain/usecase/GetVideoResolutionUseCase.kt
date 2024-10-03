package com.youtube.domain.usecase

import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.domain.utils.PythonMethod
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