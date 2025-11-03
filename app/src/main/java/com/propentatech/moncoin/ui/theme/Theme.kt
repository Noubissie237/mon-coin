package com.propentatech.moncoin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryVariantDark,
    onPrimaryContainer = OnPrimaryDark,
    
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryVariantDark,
    onSecondaryContainer = OnSecondaryDark,
    
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryVariantDark,
    onTertiaryContainer = OnTertiaryDark,
    
    error = ErrorDark,
    onError = OnPrimaryDark,
    errorContainer = ErrorDark,
    onErrorContainer = OnPrimaryDark,
    
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    outline = OnSurfaceVariantDark,
    outlineVariant = SurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnPrimary,
    
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = OnSecondary,
    
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryVariant,
    onTertiaryContainer = OnTertiary,
    
    error = Error,
    onError = OnPrimary,
    errorContainer = Error,
    onErrorContainer = OnPrimary,
    
    background = Background,
    onBackground = OnBackground,
    
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    
    outline = OnSurfaceVariant,
    outlineVariant = SurfaceVariant
)

@Composable
fun MonCoinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color désactivé pour utiliser notre palette personnalisée
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}