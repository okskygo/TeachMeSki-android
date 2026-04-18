package com.teachmeski.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TmsLightColorScheme = lightColorScheme(
    primary = TmsColor.Primary,
    onPrimary = TmsColor.OnPrimary,
    primaryContainer = TmsColor.PrimaryContainer,
    onPrimaryContainer = TmsColor.OnPrimaryContainer,
    secondary = TmsColor.Secondary,
    secondaryContainer = TmsColor.SecondaryContainer,
    background = TmsColor.Background,
    onBackground = TmsColor.OnSurface,
    surface = TmsColor.Surface,
    onSurface = TmsColor.OnSurface,
    onSurfaceVariant = TmsColor.OnSurfaceVariant,
    surfaceVariant = TmsColor.SurfaceVariant,
    surfaceContainer = TmsColor.SurfaceContainer,
    surfaceContainerLow = TmsColor.SurfaceLow,
    surfaceContainerHigh = TmsColor.SurfaceLowest,
    surfaceContainerHighest = TmsColor.SurfaceHighest,
    surfaceContainerLowest = TmsColor.SurfaceLowest,
    surfaceDim = TmsColor.SurfaceDim,
    outline = TmsColor.Outline,
    outlineVariant = TmsColor.OutlineVariant,
    error = TmsColor.Error,
    errorContainer = TmsColor.ErrorContainer,
    inverseSurface = TmsColor.InverseSurface,
    inverseOnSurface = TmsColor.InverseOnSurface,
)

@Composable
fun TeachMeSkiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TmsLightColorScheme,
        typography = TmsTypography,
        shapes = TmsShapes,
        content = content,
    )
}
