package com.example.cuan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cuan.feature.analytics.AnalyticsScreen
import com.example.cuan.feature.ai_chat.AIChatScreen
import com.example.cuan.feature.dashboard.DashboardScreen
import com.example.cuan.feature.goals.GoalsScreen
import com.example.cuan.feature.onboarding.OnboardingCarouselScreen
import com.example.cuan.feature.onboarding.OnboardingNameScreen
import com.example.cuan.feature.profile.ProfileScreen
import com.example.cuan.feature.settings.SettingsScreen
import com.example.cuan.feature.splash.SplashScreen
import com.example.cuan.feature.transaction.add.AddTransactionScreen
import com.example.cuan.feature.transaction.freetext.FreeTextScreen
import com.example.cuan.feature.transaction.list.TransactionListScreen
import com.example.cuan.feature.transaction.scan.ScanScreen
import com.example.cuan.core.utils.OcrTextHolder

import androidx.compose.ui.Modifier

// Navigation graph for CuOne app
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AppRoute.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(AppRoute.OnboardingCarousel.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding
        composable(AppRoute.OnboardingCarousel.route) {
            OnboardingCarouselScreen(
                onNavigateToName = {
                    navController.navigate(AppRoute.OnboardingName.route)
                }
            )
        }

        composable(AppRoute.OnboardingName.route) {
            OnboardingNameScreen(
                onOnboardingComplete = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.OnboardingCarousel.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Screens (Bottom Nav)
        composable(AppRoute.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(AppRoute.AddTransaction.route)
                },
                onNavigateToScan = {
                    navController.navigate(AppRoute.Scan.route)
                },
                onNavigateToFreeText = {
                    navController.navigate(AppRoute.FreeText.route)
                },
                onNavigateToTransactionList = {
                    navController.navigate(AppRoute.TransactionList.route)
                },
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                },
                onNavigateToAIChat = {
                    navController.navigate(AppRoute.AIChat.route)
                }
            )
        }

        composable(AppRoute.Analytics.route) {
            AnalyticsScreen()
        }

        composable(AppRoute.AIChat.route) {
            AIChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Goals.route) {
            GoalsScreen()
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                }
            )
        }

        // Transaction Screens
        composable(AppRoute.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Scan.route) {
            ScanScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanSuccess = {
                    // ocrText is already stored in OcrTextHolder by ScanScreen
                    navController.navigate(AppRoute.ScanResult.route)
                },
                onNavigateToFreeText = {
                    navController.navigate(AppRoute.FreeText.route)
                },
                onNavigateToManual = {
                    navController.navigate(AppRoute.AddTransaction.route)
                }
            )
        }

        composable(route = AppRoute.ScanResult.route) {
            val ocrText = androidx.compose.runtime.remember { OcrTextHolder.getAndClear() }
            com.example.cuan.feature.transaction.scan.ScanResultScreen(
                ocrText = ocrText,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.popBackStack(AppRoute.Dashboard.route, inclusive = false)
                }
            )
        }

        composable(AppRoute.FreeText.route) {
            FreeTextScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.popBackStack(AppRoute.Dashboard.route, inclusive = false)
                }
            )
        }

        composable(AppRoute.TransactionList.route) {
            TransactionListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profile
        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}