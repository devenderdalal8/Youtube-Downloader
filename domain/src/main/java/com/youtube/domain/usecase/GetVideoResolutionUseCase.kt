package com.youtube.domain.usecase

import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.domain.utils.PythonMethod
import com.youtube.domain.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetVideoResolutionUseCase @Inject constructor(
    private val pythonScriptRepository: PythonScriptRepository
) {
    suspend operator fun invoke(url: String, resolution: String): Resource<Any> {
        return withContext(Dispatchers.IO) {
            pythonScriptRepository.downloadAsync(
                PythonMethod.VIDEO_RESOLUTION.title,
                url,
                resolution
            )
        }
    }
}