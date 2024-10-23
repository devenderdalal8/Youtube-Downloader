package com.youtube.domain.usecase

import android.util.Log
import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.domain.utils.PythonMethod
import com.youtube.domain.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSearchVideoUseCase @Inject constructor(
    private val pythonScriptRepository: PythonScriptRepository
) {
    suspend operator fun invoke(query: String): Resource<Any> {
        return withContext(Dispatchers.IO) {
            pythonScriptRepository.downloadAsync(
                PythonMethod.SEARCH_VIDEO.title,
                query
            )
        }
    }
}