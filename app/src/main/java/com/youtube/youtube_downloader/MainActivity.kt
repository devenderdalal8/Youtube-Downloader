package com.youtube.youtube_downloader

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.youtube.youtube_downloader.ui.theme.YoutubeDownloaderTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // Handle permission results here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            YoutubeDownloaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissionsLauncher.launch(permissions)
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier) {
    val result = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("YouTube Downloader")

        Button(onClick = {
            val result = callPythonFunction("download_video", "https://www.youtube.com/watch?v=GGvM28rWqWc")
            Log.d("TAG", "MainScreen: result $result")
        }) {
            Text("Download Video")
        }

        Button(onClick = {
            result.value = callPythonFunction("download_audio", "https://www.youtube.com/watch?v=GGvM28rWqWc") as String
        }) {
            Text("Download Audio")
        }

        Button(onClick = {
            result.value = callPythonFunction(
                "download_subtitles",
                "https://www.youtube.com/watch?v=GGvM28rWqWc",
                "en",
                "captions.txt"
            ) as String
        }) {
            Text("Download Subtitles")
        }

        Button(onClick = {
            result.value = callPythonFunction(
                "download_playlist",
                "https://www.youtube.com/playlist?list=GGvM28rWqWc"
            ) as String
        }) {
            Text("Download Playlist")
        }

        Button(onClick = {
            val channelName = callPythonFunction("download_channel", "https://www.youtube.com/@examplechannel")
            result.value = channelName.toString() // Channel name
        }) {
            Text("Download Channel Videos")
        }

        Text(text = result.value)
    }
}

fun callPythonFunction(functionName: String, vararg args: Any): Any? {
    val python = Python.getInstance()
    val pythonFile = python.getModule("script") // Replace with your script name
    return try {
        pythonFile.callAttr(functionName, *args)
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.message}"
    }
}
