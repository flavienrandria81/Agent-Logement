package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Navy800,
    onPrimaryContainer = Blue50,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald700,
    tertiary = Amber500,
    background = Navy900,
    surface = Navy800,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Navy700,
    onSurfaceVariant = Slate300
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = DesignColors.PrimaryContainer,
    onPrimaryContainer = Navy900,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald50,
    tertiary = Amber600,
    background = Slate50,
    surface = Color.White,
    onBackground = Navy900,
    onSurface = Navy900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
