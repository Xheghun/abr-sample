package com.example.plexplayerprep.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PlexScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFE5A00D),
    secondary = Color(0xFF68B0AB),
    tertiary = Color(0xFFE76F51),
    background = Color(0xFF101113),
    surface = Color(0xFF181A1F),
    surfaceVariant = Color(0xFF242832),
    onPrimary = Color(0xFF1B1400),
    onSecondary = Color(0xFF061D1B),
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFFC9CDD6)
)

@Composable
fun PlexPrepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlexScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
