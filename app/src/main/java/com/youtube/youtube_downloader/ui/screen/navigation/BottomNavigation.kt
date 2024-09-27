package com.youtube.youtube_downloader.ui.screen.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.youtube.youtube_downloader.R
import com.youtube.youtube_downloader.util.BottomNavScreen


@Composable
fun CustomBottomBar(navController: NavController) {
    val selectedItem = remember { mutableStateOf(BottomNavScreen.Home.title) }
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(40.dp),
    ) {
        BottomNavigation(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            backgroundColor = Color.Black
        ) {
            // Home Icon
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = selectedItem.value == BottomNavScreen.Home.title,
                onClick = { bottomNavBar(navController, BottomNavScreen.Home) }
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_playlist_icon),
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = selectedItem.value == BottomNavScreen.PlayList.title,
                onClick = { bottomNavBar(navController, BottomNavScreen.PlayList) }
            )

            // Create Item Icon
            BottomNavigationItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Color.White,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search_icon),
                            contentDescription = "Create",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                selected = selectedItem.value == BottomNavScreen.Search.title,
                onClick = { bottomNavBar(navController, BottomNavScreen.Search) }
            )

            // Notifications Icon
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chennel_icon),
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = selectedItem.value == BottomNavScreen.Channels.title,
                onClick = { bottomNavBar(navController, BottomNavScreen.Channels) }
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_icon),
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = selectedItem.value == BottomNavScreen.Setting.title,
                onClick = {
                    selectedItem.value = BottomNavScreen.Setting.title
                    bottomNavBar(navController, BottomNavScreen.Setting)
                }

            )
        }
    }
}

fun bottomNavBar(navController: NavController, screen: BottomNavScreen) {
    navController.navigate(screen.route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

