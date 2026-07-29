package com.aura.ai.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.aura.ai.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = LavenderBg,
    surface = AuraSurface,
    surfaceVariant = AuraSurfaceVariant,
    onSurface = AuraOnSurface,
    onBackground = AuraOnSurface,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = AuraPrimaryDark,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = LavenderBgDark,
    surface = AuraSurfaceDark,
    surfaceVariant = AuraSurfaceVariantDark,
    onSurface = AuraOnSurfaceDark,
    onBackground = AuraOnSurfaceDark,
    error = ErrorRed
)

@Composable
fun AuraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
