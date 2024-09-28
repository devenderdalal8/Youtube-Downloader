package com.youtube.youtube_downloader.data.repositoryImpl

import com.chaquo.python.PyObject
import com.youtube.youtube_downloader.data.repository.PythonScriptRepository
import javax.inject.Inject

class PythonScriptRepositoryImpl @Inject constructor(
    private val pythonService: PyObject
) : PythonScriptRepository {
    override suspend fun downloadAsync(functionName: String, vararg args: Any): Any? {
        val result = callPythonFunction(functionName, *args)
        return result
    }

    private fun callPythonFunction(functionName: String, vararg args: Any): Any? {
        return try {
            val service = pythonService.callAttr(functionName, *args)
            return service
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}