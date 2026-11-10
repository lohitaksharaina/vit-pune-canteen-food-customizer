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

private val CanteenDarkColorScheme = darkColorScheme(
    primary = FlameOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4E260E),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = AmberGlow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF423214),
    onSecondaryContainer = Color(0xFFFFE082),
    background = CharcoalDark,
    onBackground = WarmIvory,
    surface = CharcoalSurface,
    onSurface = WarmIvory,
    surfaceVariant = CharcoalVariant,
    onSurfaceVariant = WarmGrey,
    outline = CharcoalBorder,
    error = Color(0xFFFF5252),
    onError = Color.White
)

private val CanteenLightColorScheme = lightColorScheme(
    primary = FlameOrangeDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF381400),
    secondary = Color(0xFFC77700),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF261900),
    background = Color(0xFFFAF6F2),
    onBackground = Color(0xFF1F1A17),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1A17),
    surfaceVariant = Color(0xFFF0E7DF),
    onSurfaceVariant = Color(0xFF52453C),
    outline = Color(0xFFD3C5BA),
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to deep warm canteen food aesthetic
    dynamicColor: Boolean = false, // Preserve restaurant branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CanteenDarkColorScheme
        else -> CanteenLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
