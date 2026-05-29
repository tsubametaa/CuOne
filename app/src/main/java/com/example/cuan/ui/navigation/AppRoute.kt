package com.example.cuan.ui.navigation

/**
 * Navigation routes for CuOne app
 */
sealed class AppRoute(val route: String) {
    object Splash : AppRoute("splash")
    object OnboardingCarousel : AppRoute("onboarding_carousel")
    object OnboardingName : AppRoute("onboarding_name")
    object Dashboard : AppRoute("dashboard")
    object AddTransaction : AppRoute("add_transaction")
    object Scan : AppRoute("scan")
    object ScanResult : AppRoute("scan_result")
    object FreeText : AppRoute("free_text")
    object TransactionList : AppRoute("transaction_list")
    object Analytics : AppRoute("analytics")
    object AIChat : AppRoute("ai_chat")
    object Goals : AppRoute("goals")
    object Profile : AppRoute("profile")
    object Settings : AppRoute("settings")
}

/**
 * Bottom navigation items
 */
enum class BottomNavItem(
    val route: String,
    val title: String,
    val iconName: String
) {
    Dashboard("dashboard", "Beranda", "LayoutDashboard"),
    Analytics("analytics", "Analitik", "BarChart3"),
    Scan("scan", "Pindai", "ScanLine"),
    Goals("goals", "Target", "Target"),
    Settings("settings", "Setelan", "Settings2")
}