package com.youtube.youtube_downloader.presenter.ui.screen.splashScreen

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.presenter.ui.theme.size_200
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    // Animation state for scaling the splash screen
    val scale = remember { Animatable(0f) }

    // Launch animation asynchronously (without background work)
    LaunchedEffect(Unit) {
        // Animate the splash screen with a scaling effect
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800, // Animation duration
                easing = {
                    OvershootInterpolator(1f).getInterpolation(it)
                })
        )

        // Hold the splash screen for a short time (2 seconds in this case)
        delay(2000L)

        // Navigate to the next screen or main content
        onSplashFinished()
    }

    // Displaying the splash screen with the animation
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .scale(scale.value) // Apply the scaling effect to the splash
    ) {
        // Animated preloader (Lottie animation or any other custom logo)
        AnimatedPreloader(modifier = modifier.size(size_200)) // Adjust size if needed
    }
}

@Composable
fun AnimatedPreloader(modifier: Modifier = Modifier) {
    // Preload Lottie animation resource (replace with your splash screen animation)
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_screen)
    )

    val preloaderProgress by animateLottieCompositionAsState(
        composition = preloaderLottieComposition,
        iterations = 1 // Play only once
    )

    // Display the Lottie animation
    LottieAnimation(
        composition = preloaderLottieComposition,
        progress = preloaderProgress,
        modifier = modifier
    )
}
