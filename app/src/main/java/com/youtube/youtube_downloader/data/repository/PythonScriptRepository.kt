package com.youtube.youtube_downloader.data.repository

interface PythonScriptRepository {
    suspend fun downloadAsync(functionName: String, vararg args: Any): Any?
}