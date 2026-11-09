package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val JummoUltraPremiumColorScheme = darkColorScheme(
    primary = JummoVioletBright,
    onPrimary = Color.White,
    primaryContainer = JummoVioletDeep,
    onPrimaryContainer = JummoVioletSoft,
    secondary = JummoGoldBright,
    onSecondary = JummoObsidian,
    secondaryContainer = JummoGoldDeep,
    onSecondaryContainer = JummoGoldSoft,
    tertiary = JummoSuccessGreen,
    onTertiary = JummoObsidian,
    background = JummoObsidian,
    onBackground = JummoTextPrimary,
    surface = JummoDeepPurple,
    onSurface = JummoTextPrimary,
    surfaceVariant = JummoCardPurple,
    onSurfaceVariant = JummoTextSecondary,
    outline = JummoBorderSubtle,
    error = JummoErrorRed,
    onError = Color.White
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
        colorScheme = JummoUltraPremiumColorScheme,
        typography = Typography,
        shapes = JummoShapes,
        content = content
    )
}
