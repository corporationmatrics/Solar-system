package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TarangBlue,
    onPrimary = Color.White,
    primaryContainer = TarangContainerBlue,
    onPrimaryContainer = TarangDarkBlue,
    secondary = TarangDarkBlue,
    onSecondary = Color.White,
    background = TarangBg,
    onBackground = TarangTextDark,
    surface = Color.White,
    onSurface = TarangTextDark,
    surfaceVariant = TarangLightGray,
    onSurfaceVariant = TarangTextGray,
    outline = TarangBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = TarangContainerBlue,
    onPrimary = TarangDarkBlue,
    primaryContainer = TarangBlue,
    onPrimaryContainer = Color.White,
    secondary = TarangBlue,
    onSecondary = Color.White,
    background = TarangDarkBlue,
    onBackground = Color.White,
    surface = TarangTextDark,
    onSurface = Color.White,
    surfaceVariant = TarangTextGray,
    onSurfaceVariant = TarangLightGray,
    outline = TarangTextSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamic color of Android 12+ (turned off to guarantee our branding colors)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
