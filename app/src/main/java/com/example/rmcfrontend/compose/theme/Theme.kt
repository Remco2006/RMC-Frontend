package com.example.rmcfrontend.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.rmcfrontend.api.ApiClient

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun RmcTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Ensure ApiClient is initialized (no-op but keeps intent clear)
    LaunchedEffect(Unit) {
        // Nothing required here; token is restored in MainActivity.
        ApiClient.hasAuthToken()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
