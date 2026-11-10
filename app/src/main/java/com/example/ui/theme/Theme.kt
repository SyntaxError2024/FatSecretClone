package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    primary = FatSecretGreen,
    onPrimary = Color.Black,
    primaryContainer = FatSecretGreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = AccentBlue,
    onSecondary = Color.Black,
    secondaryContainer = AccentBlueContainer,
    onSecondaryContainer = AccentBlue,
    tertiary = CalorieOrange,
    onTertiary = Color.Black,
    tertiaryContainer = CalorieOrangeContainer,
    onTertiaryContainer = CalorieOrange,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = DividerColor,
    error = CalorieRed,
    onError = Color.White,
    errorContainer = CalorieRedContainer,
    onErrorContainer = CalorieRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val systemDensityValue = if (configuration.densityDpi > 0) {
        configuration.densityDpi / 160f
    } else {
        LocalDensity.current.density
    }
    val systemFontScale = configuration.fontScale

    // Synchronize both global Android display zoom (densityDpi) and system font zoom (fontScale)
    val dynamicDensity = remember(configuration.densityDpi, configuration.fontScale) {
        Density(
            density = systemDensityValue,
            fontScale = systemFontScale
        )
    }

    CompositionLocalProvider(LocalDensity provides dynamicDensity) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
