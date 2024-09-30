package com.youtube.youtube_downloader.ui.screen.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.youtube.youtube_downloader.util.BottomNavScreen

@Composable
fun CustomBottomBar(navController: NavController) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val isSelected = remember {
            mutableStateOf(BottomNavScreen.Home.route)
        }
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = BottomNavScreen.Home.icon),
                    contentDescription = BottomNavScreen.Home.title
                )
            },
            label = { Text(BottomNavScreen.Home.title) },
            selected = currentDestination?.isSelected(BottomNavScreen.Home.route) ?: false,
            onClick = {
                navController.bottomNavBar(BottomNavScreen.Home.route)
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = BottomNavScreen.PlayList.icon),
                    contentDescription = BottomNavScreen.PlayList.title
                )
            },
            label = { Text(BottomNavScreen.PlayList.title) },
            selected = currentDestination?.isSelected(BottomNavScreen.PlayList.route) ?: false,
            onClick = {
                navController.bottomNavBar(BottomNavScreen.PlayList.route)
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = BottomNavScreen.Search.icon),
                    contentDescription = BottomNavScreen.Search.title
                )
            },
            label = { Text(BottomNavScreen.Search.title) },
            selected = currentDestination?.isSelected(BottomNavScreen.Search.route) ?: false,
            onClick = {
                navController.bottomNavBar(BottomNavScreen.Search.route)
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = BottomNavScreen.Channels.icon),
                    contentDescription = BottomNavScreen.Channels.title
                )
            },
            label = { Text(BottomNavScreen.Channels.title) },
            selected = currentDestination?.isSelected(BottomNavScreen.Channels.route) ?: false,
            onClick = {
                navController.bottomNavBar(BottomNavScreen.Channels.route)
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = BottomNavScreen.Setting.icon),
                    contentDescription = BottomNavScreen.Setting.title
                )
            },
            label = { Text(BottomNavScreen.Setting.title) },
            selected = currentDestination?.isSelected(BottomNavScreen.Setting.route) ?: false,
            onClick = {
                navController.bottomNavBar(BottomNavScreen.Setting.route)
            }
        )
    }
}

fun NavController.bottomNavBar(screen: String) {
    navigate(screen) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavDestination?.isSelected(screen: String) =
    this?.hierarchy?.any { it.route == screen }


val getBottomNavScreens = listOf(
    BottomNavScreen.Home.route,
    BottomNavScreen.PlayList.route,
    BottomNavScreen.Search.route,
    BottomNavScreen.Channels.route,
    BottomNavScreen.Setting.route,

    )