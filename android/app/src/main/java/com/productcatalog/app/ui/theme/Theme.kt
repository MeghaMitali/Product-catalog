package com.productcatalog.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ProductCatalogColors.Primary,
    secondary = ProductCatalogColors.Secondary,
    tertiary = ProductCatalogColors.Border,
    background = ProductCatalogColors.Background,
    surface = ProductCatalogColors.Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ProductCatalogColors.TextPrimary,
    onSurface = ProductCatalogColors.TextPrimary,
    error = ProductCatalogColors.Error
)

private val DarkColors = darkColorScheme(
    primary = ProductCatalogColors.Primary,
    secondary = ProductCatalogColors.Secondary,
    background = Color(0xFF0F172A),
    surface = Color(0xFF111827),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ProductCatalogColors.Error
)

@Composable
fun ProductCatalogTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = ProductCatalogTypography, content = content)
}
