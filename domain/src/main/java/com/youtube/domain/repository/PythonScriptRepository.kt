package com.youtube.domain.repository

import com.youtube.domain.utils.Resource

interface PythonScriptRepository {
    suspend fun downloadAsync(functionName: String, vararg args: Any): Resource<Any>
}