package com.example.cuan.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

// Floating Capsule Bottom Navigation Bar Component (Redesigned matching user mockup) //
@Composable
fun BottomNavBarComponent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Analytics,
        BottomNavItem.Scan,
        BottomNavItem.Goals,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom nav for main screens
    val showBottomNav = currentRoute in listOf(
        AppRoute.Dashboard.route,
        AppRoute.Analytics.route,
        AppRoute.Scan.route,
        AppRoute.Goals.route,
        AppRoute.Settings.route
    )

    if (showBottomNav) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(Color.Transparent)
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(32.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val selected = currentRoute == item.route
                    val isCenter = index == 2 // AIChat is the middle button

                    // professional bouncy scale animation on selection
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "scale"
                    )

                    // professional vertical offset lifting animation on selection
                    val translationY by animateDpAsState(
                        targetValue = if (selected) (-3).dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "translationY"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(AppRoute.Dashboard.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                indication = null, // Remove default ripple to keep it clean and minimal
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Get matching icon pair (Active / Inactive)
                        val iconPair = when (item) {
                            BottomNavItem.Dashboard -> Icons.Default.Home to Icons.Outlined.Home
                            BottomNavItem.Analytics -> Icons.Default.BarChart to Icons.Outlined.BarChart
                            BottomNavItem.Scan -> Icons.AutoMirrored.Filled.ReceiptLong to Icons.AutoMirrored.Filled.ReceiptLong
                            BottomNavItem.Goals -> Icons.Default.TrackChanges to Icons.Outlined.TrackChanges
                            BottomNavItem.Settings -> Icons.Default.Settings to Icons.Outlined.Settings
                        }

                        if (isCenter) {
                            // Center button with circle border/fill
                            val centerContainerModifier = if (selected) {
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Secondary)
                            } else {
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Secondary.copy(alpha = 0.4f), CircleShape)
                            }

                            Box(
                                modifier = centerContainerModifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.translationY = translationY.toPx()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) iconPair.first else iconPair.second,
                                    contentDescription = item.title,
                                    tint = if (selected) Color.White else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            // Other normal buttons
                            Icon(
                                imageVector = if (selected) iconPair.first else iconPair.second,
                                contentDescription = item.title,
                                tint = if (selected) Secondary else TextSecondary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.translationY = translationY.toPx()
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}