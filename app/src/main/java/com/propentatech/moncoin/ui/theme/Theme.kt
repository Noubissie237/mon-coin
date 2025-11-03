package com.propentatech.moncoin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Créer un ColorScheme sombre à partir d'un thème
 */
fun createDarkColorScheme(theme: AppTheme) = darkColorScheme(
    primary = getThemeColors(theme).primaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = getThemeColors(theme).primaryVariantDark,
    onPrimaryContainer = OnPrimaryDark,
    
    secondary = getThemeColors(theme).secondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = getThemeColors(theme).secondaryVariantDark,
    onSecondaryContainer = OnSecondaryDark,
    
    tertiary = getThemeColors(theme).tertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = getThemeColors(theme).tertiaryVariantDark,
    onTertiaryContainer = OnTertiaryDark,
    
    error = ErrorDark,
    onError = OnPrimaryDark,
    errorContainer = ErrorDark,
    onErrorContainer = OnPrimaryDark,
    
    background = getThemeColors(theme).backgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = getThemeColors(theme).surfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = getThemeColors(theme).surfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    outline = OnSurfaceVariantDark,
    outlineVariant = getThemeColors(theme).surfaceVariantDark
)

/**
 * Créer un ColorScheme clair à partir d'un thème
 */
fun createLightColorScheme(theme: AppTheme) = lightColorScheme(
    primary = getThemeColors(theme).primary,
    onPrimary = OnPrimary,
    primaryContainer = getThemeColors(theme).primaryVariant,
    onPrimaryContainer = OnPrimary,
    
    secondary = getThemeColors(theme).secondary,
    onSecondary = OnSecondary,
    secondaryContainer = getThemeColors(theme).secondaryVariant,
    onSecondaryContainer = OnSecondary,
    
    tertiary = getThemeColors(theme).tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = getThemeColors(theme).tertiaryVariant,
    onTertiaryContainer = OnTertiary,
    
    error = Error,
    onError = OnPrimary,
    errorContainer = Error,
    onErrorContainer = OnPrimary,
    
    background = getThemeColors(theme).background,
    onBackground = OnBackground,
    
    surface = getThemeColors(theme).surface,
    onSurface = OnSurface,
    surfaceVariant = getThemeColors(theme).surfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    
    outline = OnSurfaceVariant,
    outlineVariant = getThemeColors(theme).surfaceVariant
)

@Composable
fun MonCoinTheme(
    selectedTheme: AppTheme = AppTheme.INDIGO_SOFT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> createDarkColorScheme(selectedTheme)
        else -> createLightColorScheme(selectedTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
