package com.teachmeski.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    // The app currently only supports a light color scheme on a light surface
    // background. Force dark status/navigation bar icons so they remain legible
    // against the light background under edge-to-edge.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = TmsLightColorScheme,
        typography = TmsTypography,
        shapes = TmsShapes,
        content = content,
    )
}
