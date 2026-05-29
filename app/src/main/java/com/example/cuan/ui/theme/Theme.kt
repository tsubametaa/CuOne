package com.example.cuan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// CuOne Color Scheme - Light theme only
private val CuOneColorScheme = lightColorScheme(
    primary = Secondary,
    onPrimary = OnSecondary,
    primaryContainer = SecondaryContainer,
    onPrimaryContainer = OnSecondaryContainer,
    secondary = Accent,
    onSecondary = OnAccent,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Background,
    onSurface = OnBackground,
    surfaceVariant = BackgroundVariant,
    onSurfaceVariant = OnBackground,
    error = ErrorColor,
    onError = OnAccent,
    outline = TextSecondary
)

@Composable
fun CuanTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CuOneColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CuOneTypography,
        content = content
    )
}