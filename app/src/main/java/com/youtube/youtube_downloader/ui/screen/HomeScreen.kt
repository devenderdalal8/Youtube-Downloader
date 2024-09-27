package com.youtube.youtube_downloader.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HomeScreen(videoUrl: String = "") {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Surface(modifier = Modifier.fillMaxSize()) {
        YoutubeVideoPlayer("GGvM28rWqWc", height = 300.dp, width = screenWidth)
        Spacer(modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun YoutubeVideoPlayer(videoId: String, height: Dp, width: Dp) {
    val context = LocalContext.current

    val webView = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        webViewClient = WebViewClient()
    }

    val htmlData = getHTMLData(videoId, height, width)

    Column(Modifier.fillMaxWidth()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.widthIn(max = width)
        ) { view ->
            view.loadDataWithBaseURL(
                "https://www.youtube.com",
                htmlData,
                "text/html",
                "UTF-8",
                null
            )
            view.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java", ReplaceWith("true"))
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ): Boolean {
                    return true
                }
            }
        }
    }
}


fun getHTMLData(videoId: String, height: Dp, width: Dp): String {
    return """
        <html>
            <body style="margin:0px;padding:0px;">
                <div id="player"></div>
                <script>
                    var player;
                    function onYouTubeIframeAPIReady() {
                        player = new YT.Player('player', {
                            height: '${height.value}',
                            width: '${width.value}',
                            videoId: '$videoId',
                            playerVars: {
                                'playsinline': 1
                            },
                            events: {
                                'onReady': onPlayerReady
                            }
                        });
                    }

                    function onPlayerReady(event) {
                        player.playVideo();
                        // Override the click behavior for the YouTube logo
                        var logo = document.querySelector('.ytp-title-link');
                        if (logo) {
                            logo.onclick = function(event) {
                                event.preventDefault(); // Prevent default behavior
                                Android.openYouTube('$videoId'); // Call Android method
                            };
                        }
                    }
                </script>
                <script src="https://www.youtube.com/iframe_api"></script>
            </body>
        </html>
    """.trimIndent()
}

@Preview
@Composable
private fun Preview() {
    HomeScreen()
}