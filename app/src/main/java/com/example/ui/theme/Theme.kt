package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val JummoPurplePremiumColorScheme = lightColorScheme(
    primary = JummoPrimary,
    onPrimary = JummoButtonText,
    primaryContainer = JummoSurface,
    onPrimaryContainer = JummoTextPrimary,
    secondary = JummoAccent,
    onSecondary = JummoButtonText,
    secondaryContainer = JummoSecondary,
    onSecondaryContainer = JummoTextPrimary,
    tertiary = JummoSecondary,
    onTertiary = JummoTextPrimary,
    background = JummoBackground,
    onBackground = JummoTextPrimary,
    surface = JummoSurface,
    onSurface = JummoTextPrimary,
    surfaceVariant = JummoSurface,
    onSurfaceVariant = JummoTextSecondary,
    outline = JummoSecondary,
    error = JummoErrorRed,
    onError = JummoButtonText
)

val JummoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JummoPurplePremiumColorScheme,
        typography = Typography,
        shapes = JummoShapes,
        content = content
    )
}
