package com.youtube.data.repositoryImpl

import android.util.Log
import com.chaquo.python.PyObject
import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.domain.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PythonScriptRepositoryImpl @Inject constructor(
    private val pythonService: PyObject,
) : PythonScriptRepository {
    override suspend fun downloadAsync(functionName: String, vararg args: Any): Resource<Any> {
        val result = withContext(Dispatchers.IO) {
            try {
                val data = callPythonFunction(functionName, *args)
                Resource.Success(data)
            } catch (ex: Exception) {
                Resource.Error(ex.message.toString())
            }
        }
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
            Log.e("TAG", "callPythonFunction: ${e.message.toString()} ")
            "Error: ${e.message}"
        }
    }
}