package com.youtube.youtube_downloader.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun DownloadScreen() {
    Column(Modifier.fillMaxSize()){
        Text(text = "Download" , color = Color.Red)
    }
}