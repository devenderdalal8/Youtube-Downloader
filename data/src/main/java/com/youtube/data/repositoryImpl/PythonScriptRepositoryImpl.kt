package com.youtube.data.repositoryImpl

import com.chaquo.python.PyObject
import com.youtube.domain.repository.PythonScriptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PythonScriptRepositoryImpl @Inject constructor(
    private val pythonService: PyObject
) : PythonScriptRepository {
    override suspend fun downloadAsync(functionName: String, vararg args: Any): Any {
        val result: Any = callPythonFunction(functionName, *args)
        return result
    }

    private suspend fun callPythonFunction(functionName: String, vararg args: Any): Any {
        return try {
            val service: PyObject
            withContext(Dispatchers.IO) {
                service = pythonService.callAttr(functionName, *args)
            }
            return service
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}