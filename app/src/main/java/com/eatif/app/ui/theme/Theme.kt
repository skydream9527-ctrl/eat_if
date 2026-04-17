package com.eatif.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun EatIfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColorKey: String = "orange",
    content: @Composable () -> Unit
) {
    val seedColor = ThemeColorOptions.find { it.key == seedColorKey }?.color ?: OrangePrimary
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = seedColor,
            onPrimary = White,
            primaryContainer = seedColor.copy(alpha = 0.7f),
            onPrimaryContainer = White,
            secondary = GrayMedium,
            onSecondary = Black,
            secondaryContainer = GrayMedium,
            onSecondaryContainer = White,
            background = Black,
            onBackground = White,
            surface = Black,
            onSurface = White,
            surfaceVariant = GrayMedium,
            onSurfaceVariant = GrayLight,
            error = Red,
            onError = White
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            onPrimary = White,
            primaryContainer = seedColor.copy(alpha = 0.85f),
            onPrimaryContainer = Black,
            secondary = GrayMedium,
            onSecondary = White,
            secondaryContainer = GrayLight,
            onSecondaryContainer = Black,
            background = White,
            onBackground = Black,
            surface = White,
            onSurface = Black,
            surfaceVariant = GrayLight,
            onSurfaceVariant = GrayMedium,
            error = Red,
            onError = White
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
