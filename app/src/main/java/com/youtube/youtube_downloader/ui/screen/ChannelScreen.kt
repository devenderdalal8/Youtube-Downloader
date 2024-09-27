package com.youtube.youtube_downloader.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ChannelScreen() {
    Text(text = "Channel Screen")
}


@Composable
private fun CustomBottomBar() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
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
                selected = true,  // Set this to dynamically manage selection
                onClick = { /* Handle navigation here */ }
            )

            // Search Icon
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = false,
                onClick = { /* Handle navigation here */ }
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
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                selected = false,
                onClick = { /* Handle navigation here */ }
            )

            // Notifications Icon
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = false,
                onClick = { /* Handle navigation here */ }
            )

            // Profile Icon
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                },
                selected = false,
                onClick = { /* Handle navigation here */ }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewBottomBar() {
    CustomBottomBar()
}
