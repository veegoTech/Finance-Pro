package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkNavyBlue,
    onPrimary = Color.White,
    primaryContainer = DarkNavySlate,
    onPrimaryContainer = Color.White,
    secondary = RoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = RoyalBlueSubtle,
    onSecondaryContainer = RoyalBlue,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = BackgroundLightGrey,
    onBackground = TextPrimary,
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardAlt,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderMedium,
    error = OverdueRed,
    onError = Color.White,
    errorContainer = OverdueRedLight,
    onErrorContainer = OverdueRedDark
)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlueBright,
    onPrimary = DarkNavyBlue,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = Color.White,
    secondary = RoyalBlueLight,
    onSecondary = DarkNavyBlue,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = RoyalBlueLight,
    tertiary = SuccessGreen,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkBorder,
    error = OverdueRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FintechTypography,
        content = content
    )
}
