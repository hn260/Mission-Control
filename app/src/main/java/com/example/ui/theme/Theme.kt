package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaceDarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F172A),
    onPrimaryContainer = PrimaryCyan,
    secondary = SecondaryBlue,
    onSecondary = Color.Black,
    tertiary = AccentPurple,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = OffWhite,
    surface = SurfaceSlate,
    onSurface = OffWhite,
    surfaceVariant = GlassCardColor,
    onSurfaceVariant = SpaceGlow,
    error = StatusRed,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpaceDarkColorScheme,
        typography = Typography,
        content = content
    )
}
