package com.example.mindfullexpenses.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Azure,
    onPrimary = OnPrimaryDark,
    primaryContainer = MistBlue,
    onPrimaryContainer = DeepNavy,
    secondary = Cerulean,
    onSecondary = OnPrimaryDark,
    secondaryContainer = PaleSky,
    onSecondaryContainer = DeepNavy,
    tertiary = BrightAqua,
    onTertiary = OnPrimaryDark,
    background = SurfaceBackground,
    onBackground = DeepNavy,
    surface = SurfaceBackground,
    onSurface = DeepNavy,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Sapphire,
    outline = Outline,
    outlineVariant = Outline
)

private val DarkColors = darkColorScheme(
    primary = MistBlue,
    onPrimary = DeepNavy,
    primaryContainer = Sapphire,
    onPrimaryContainer = IceBlue,
    secondary = SoftAqua,
    onSecondary = DeepNavy,
    secondaryContainer = Sapphire,
    onSecondaryContainer = IceBlue,
    tertiary = BrightAqua,
    onTertiary = DeepNavy,
    background = DeepNavy,
    onBackground = IceBlue,
    surface = Sapphire,
    onSurface = IceBlue,
    surfaceVariant = Sapphire,
    onSurfaceVariant = MistBlue,
    outline = MistBlue,
    outlineVariant = SoftAqua
)

@Composable
fun MindfullExpensesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}