package com.youtube.youtube_downloader

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    fun downloadAsync(functionName: String, vararg args: Any, result: MutableState<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val output = callPythonFunction(functionName, *args)
            result.value = output.toString()
            Log.d("TAG", "$functionName Result: ${result.value}")
        }
    }

    private fun callPythonFunction(functionName: String, vararg args: Any): Any? {
        val python = Python.getInstance()
        val pythonFile = python.getModule("script") // Ensure this matches your Python script name
        return try {
            pythonFile.callAttr(functionName, *args)
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }
    }
}