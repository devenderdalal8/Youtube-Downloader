package com.youtube.youtube_downloader.presenter.ui.screen.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.youtube.youtube_downloader.presenter.ui.theme.size_16
import com.youtube.youtube_downloader.util.BottomNavScreen

@Composable
fun CustomBottomBar(navController: NavController) {
    Card(shape = RoundedCornerShape(topStart = size_16, topEnd = size_16)) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = BottomNavScreen.Home.icon),
                        contentDescription = BottomNavScreen.Home.title
                    )
                },
                selected = currentDestination?.isSelected(BottomNavScreen.Home.route) ?: false,
                onClick = {
                    navController.bottomNavBar(BottomNavScreen.Home.route)
                },
                colors = NavigationBarItemColors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Transparent,
                    selectedIndicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.Transparent,
                    disabledIconColor = Color.Transparent,
                    disabledTextColor = Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = BottomNavScreen.PlayList.icon),
                        contentDescription = BottomNavScreen.PlayList.title
                    )
                },
                selected = currentDestination?.isSelected(BottomNavScreen.PlayList.route) ?: false,
                onClick = {
                    navController.bottomNavBar(BottomNavScreen.PlayList.route)
                },
                colors = NavigationBarItemColors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Transparent,
                    selectedIndicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.Transparent,
                    disabledIconColor = Color.Transparent,
                    disabledTextColor = Color.Transparent
                )
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = BottomNavScreen.Download.icon),
                        contentDescription = BottomNavScreen.Download.title
                    )
                },
                selected = currentDestination?.isSelected(BottomNavScreen.Download.route) ?: false,
                onClick = {
                    navController.bottomNavBar(BottomNavScreen.Download.route)
                }, colors = NavigationBarItemColors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Transparent,
                    selectedIndicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.Transparent,
                    disabledIconColor = Color.Transparent,
                    disabledTextColor = Color.Transparent
                )
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = BottomNavScreen.Setting.icon),
                        contentDescription = BottomNavScreen.Setting.title
                    )
                },
                selected = currentDestination?.isSelected(BottomNavScreen.Setting.route) ?: false,
                onClick = {
                    navController.bottomNavBar(BottomNavScreen.Setting.route)
                }, colors = NavigationBarItemColors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Transparent,
                    selectedIndicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.Transparent,
                    disabledIconColor = Color.Transparent,
                    disabledTextColor = Color.Transparent
                )
            )
        }
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

fun NavDestination?.isSelected(screen: String) = this?.hierarchy?.any { it.route == screen }