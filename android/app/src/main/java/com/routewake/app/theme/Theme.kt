package com.routewake.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RouteWakeDarkColorScheme = darkColorScheme(
    primary = RouteWakeAccentLime,
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),

    secondary = RouteWakeAccentCyan,
    onSecondary = Color(0xFF083344),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),

    tertiary = RouteWakeInfoBlue,
    onTertiary = Color(0xFF082F49),
    tertiaryContainer = Color(0xFF0369A1),
    onTertiaryContainer = Color(0xFFE0F2FE),

    background = RouteWakeDeepDark,
    onBackground = RouteWakeTextPrimaryDark,
    surface = RouteWakeSurfaceDark,
    onSurface = RouteWakeTextPrimaryDark,
    surfaceVariant = RouteWakeSurfaceElevatedDark,
    onSurfaceVariant = RouteWakeTextSecondaryDark,
    outline = RouteWakeSurfaceBorderDark,
    outlineVariant = Color(0xFF1E293B),

    error = RouteWakeErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)

private val RouteWakeLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),

    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),

    tertiary = Color(0xFF0EA5E9),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBAE6FD),
    onTertiaryContainer = Color(0xFF0369A1),

    background = RouteWakeBackgroundLight,
    onBackground = RouteWakeTextPrimaryLight,
    surface = RouteWakeSurfaceLight,
    onSurface = RouteWakeTextPrimaryLight,
    surfaceVariant = RouteWakeSurfaceElevatedLight,
    onSurfaceVariant = RouteWakeTextSecondaryLight,
    outline = RouteWakeSurfaceBorderLight,
    outlineVariant = Color(0xFFCBD5E1),

    error = RouteWakeErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun RouteWakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RouteWakeDarkColorScheme else RouteWakeLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
