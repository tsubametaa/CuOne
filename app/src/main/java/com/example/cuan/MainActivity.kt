package com.example.cuan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cuan.ui.navigation.AppNavGraph
import com.example.cuan.ui.navigation.BottomNavBarComponent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.CuanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuanTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Routes that show bottom nav
                val bottomNavRoutes = listOf(
                    "dashboard",
                    "analytics",
                    "ai_chat",
                    "goals",
                    "settings"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomNavRoutes) {
                            BottomNavBarComponent(navController = navController)
                        }
                    },
                    containerColor = Background
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        startDestination = "splash"
                    )
                }
            }
        }
    }
}