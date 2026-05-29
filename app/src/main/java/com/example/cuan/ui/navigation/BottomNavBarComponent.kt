package com.example.cuan.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

/**
 * Bottom navigation bar component
 */
@Composable
fun BottomNavBarComponent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Dashboard to Icons.Default.Home,
        BottomNavItem.Analytics to Icons.Default.BarChart,
        BottomNavItem.AIChat to Icons.AutoMirrored.Filled.Chat,
        BottomNavItem.Goals to Icons.Default.TrackChanges,
        BottomNavItem.Settings to Icons.Default.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom nav for main screens
    val showBottomNav = currentRoute in listOf(
        AppRoute.Dashboard.route,
        AppRoute.Analytics.route,
        AppRoute.AIChat.route,
        AppRoute.Goals.route,
        AppRoute.Settings.route
    )

    if (showBottomNav) {
        NavigationBar(
            modifier = modifier,
            containerColor = Secondary
        ) {
            items.forEach { (item, icon) ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(AppRoute.Dashboard) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(text = item.title)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Secondary,
                        selectedTextColor = Background,
                        unselectedIconColor = Background.copy(alpha = 0.7f),
                        unselectedTextColor = Background.copy(alpha = 0.7f),
                        indicatorColor = Background
                    )
                )
            }
        }
    }
}