package com.youtube.data.repositoryImpl

import android.util.Log
import com.chaquo.python.PyObject
import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.domain.utils.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PythonScriptRepositoryImpl @Inject constructor(
    private val pythonService: PyObject,

) : PythonScriptRepository {
    override suspend fun downloadAsync(functionName: String, vararg args: Any): Resource<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val result = callPythonFunction(functionName, *args)
                Resource.Success(result)
            } catch (ex: Exception) {
                Log.e("TAG", "downloadAsync: ${ex.message}", )
                Resource.Error(ex.message.toString())
            }
        }
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