package com.example.rmcfrontend.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.rmcfrontend.api.ApiClient

// Blue-first, colorful palette inspired by modern onboarding UIs.
private val BluePrimary = Color(0xFF2563EB)
private val BluePrimaryDark = Color(0xFF1D4ED8)
private val CyanAccent = Color(0xFF06B6D4)
private val PinkAccent = Color(0xFFEC4899)
private val PurpleAccent = Color(0xFF7C3AED)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF0B1B3A),

    secondary = CyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),

    tertiary = PinkAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF3B0A23),

    background = Color(0xFFF7FAFF),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0B1220),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),

    secondary = Color(0xFF67E8F9),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF155E75),
    onSecondaryContainer = Color(0xFFCFFAFE),

    tertiary = Color(0xFFF9A8D4),
    onTertiary = Color(0xFF3B0A23),
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFCE7F3),

    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF111C33),
    onSurfaceVariant = Color(0xFFB6C2D1),
    outline = Color(0xFF334155)
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
)

@Composable
fun RmcTheme(
    // Light theme by default (matches the design reference). If you want system dark mode,
    // change this to: isSystemInDarkTheme().
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Keep current behavior: make sure ApiClient is initialized.
    LaunchedEffect(Unit) { ApiClient.hasAuthToken() }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}

// Exported accents for gradient UI (kept here to avoid extra files)
internal val GradientA = BluePrimary
internal val GradientB = BluePrimaryDark
internal val GradientC = PurpleAccent
internal val GradientD = PinkAccent
