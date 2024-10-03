package com.youtube.domain.repository

interface PythonScriptRepository {
    suspend fun downloadAsync(functionName: String, vararg args: Any): Any?
}