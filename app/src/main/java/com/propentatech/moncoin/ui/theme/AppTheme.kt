package com.propentatech.moncoin.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Thèmes disponibles dans l'application
 */
enum class AppTheme(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    INDIGO_SOFT(
        displayName = "Indigo Doux",
        emoji = "💜",
        description = "Apaisant et élégant"
    ),
    OCEAN_BREEZE(
        displayName = "Brise Océane",
        emoji = "🌊",
        description = "Frais et relaxant"
    ),
    SUNSET_GLOW(
        displayName = "Lueur du Couchant",
        emoji = "🌅",
        description = "Chaleureux et doux"
    ),
    FOREST_CALM(
        displayName = "Calme Forestier",
        emoji = "🌲",
        description = "Naturel et reposant"
    ),
    ROSE_GARDEN(
        displayName = "Jardin de Roses",
        emoji = "🌸",
        description = "Délicat et romantique"
    ),
    LAVENDER_DREAM(
        displayName = "Rêve Lavande",
        emoji = "💐",
        description = "Doux et serein"
    );
    
    companion object {
        fun fromOrdinal(ordinal: Int): AppTheme {
            return values().getOrNull(ordinal) ?: INDIGO_SOFT
        }
    }
}

/**
 * Palette de couleurs pour chaque thème
 */
data class ThemeColors(
    // Mode clair
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val tertiary: Color,
    val tertiaryVariant: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    
    // Mode sombre
    val primaryDark: Color,
    val primaryVariantDark: Color,
    val secondaryDark: Color,
    val secondaryVariantDark: Color,
    val tertiaryDark: Color,
    val tertiaryVariantDark: Color,
    val backgroundDark: Color,
    val surfaceDark: Color,
    val surfaceVariantDark: Color
)

/**
 * Obtenir les couleurs pour un thème donné
 */
fun getThemeColors(theme: AppTheme): ThemeColors {
    return when (theme) {
        AppTheme.INDIGO_SOFT -> ThemeColors(
            // Mode clair - Indigo doux
            primary = Color(0xFF6366F1),
            primaryVariant = Color(0xFF4F46E5),
            secondary = Color(0xFF06B6D4),
            secondaryVariant = Color(0xFF0891B2),
            tertiary = Color(0xFFA78BFA),
            tertiaryVariant = Color(0xFF8B5CF6),
            background = Color(0xFFFAFAFC),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF5F3FF),
            
            // Mode sombre
            primaryDark = Color(0xFFA5B4FC),
            primaryVariantDark = Color(0xFF818CF8),
            secondaryDark = Color(0xFF67E8F9),
            secondaryVariantDark = Color(0xFF22D3EE),
            tertiaryDark = Color(0xFFC4B5FD),
            tertiaryVariantDark = Color(0xFFA78BFA),
            backgroundDark = Color(0xFF0F172A),
            surfaceDark = Color(0xFF1E293B),
            surfaceVariantDark = Color(0xFF334155)
        )
        
        AppTheme.OCEAN_BREEZE -> ThemeColors(
            // Mode clair - Bleu océan doux
            primary = Color(0xFF0EA5E9),
            primaryVariant = Color(0xFF0284C7),
            secondary = Color(0xFF14B8A6),
            secondaryVariant = Color(0xFF0D9488),
            tertiary = Color(0xFF06B6D4),
            tertiaryVariant = Color(0xFF0891B2),
            background = Color(0xFFF0F9FF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE0F2FE),
            
            // Mode sombre
            primaryDark = Color(0xFF7DD3FC),
            primaryVariantDark = Color(0xFF38BDF8),
            secondaryDark = Color(0xFF5EEAD4),
            secondaryVariantDark = Color(0xFF2DD4BF),
            tertiaryDark = Color(0xFF67E8F9),
            tertiaryVariantDark = Color(0xFF22D3EE),
            backgroundDark = Color(0xFF0C1821),
            surfaceDark = Color(0xFF1E2A35),
            surfaceVariantDark = Color(0xFF2D3F4F)
        )
        
        AppTheme.SUNSET_GLOW -> ThemeColors(
            // Mode clair - Orange/Rose doux
            primary = Color(0xFFF97316),
            primaryVariant = Color(0xFFEA580C),
            secondary = Color(0xFFF59E0B),
            secondaryVariant = Color(0xFFD97706),
            tertiary = Color(0xFFFB923C),
            tertiaryVariant = Color(0xFFF97316),
            background = Color(0xFFFFFBF5),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFFEDD5),
            
            // Mode sombre
            primaryDark = Color(0xFFFDBA74),
            primaryVariantDark = Color(0xFFFB923C),
            secondaryDark = Color(0xFFFBBF24),
            secondaryVariantDark = Color(0xFFF59E0B),
            tertiaryDark = Color(0xFFFCA5A5),
            tertiaryVariantDark = Color(0xFFF87171),
            backgroundDark = Color(0xFF1A0F0A),
            surfaceDark = Color(0xFF2A1F1A),
            surfaceVariantDark = Color(0xFF3F2F25)
        )
        
        AppTheme.FOREST_CALM -> ThemeColors(
            // Mode clair - Vert forêt doux
            primary = Color(0xFF10B981),
            primaryVariant = Color(0xFF059669),
            secondary = Color(0xFF14B8A6),
            secondaryVariant = Color(0xFF0D9488),
            tertiary = Color(0xFF84CC16),
            tertiaryVariant = Color(0xFF65A30D),
            background = Color(0xFFF0FDF4),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFDCFCE7),
            
            // Mode sombre
            primaryDark = Color(0xFF6EE7B7),
            primaryVariantDark = Color(0xFF34D399),
            secondaryDark = Color(0xFF5EEAD4),
            secondaryVariantDark = Color(0xFF2DD4BF),
            tertiaryDark = Color(0xFFA3E635),
            tertiaryVariantDark = Color(0xFF84CC16),
            backgroundDark = Color(0xFF0A1810),
            surfaceDark = Color(0xFF1A2820),
            surfaceVariantDark = Color(0xFF2A3830)
        )
        
        AppTheme.ROSE_GARDEN -> ThemeColors(
            // Mode clair - Rose doux
            primary = Color(0xFFEC4899),
            primaryVariant = Color(0xFFDB2777),
            secondary = Color(0xFFF472B6),
            secondaryVariant = Color(0xFFEC4899),
            tertiary = Color(0xFFFBBF24),
            tertiaryVariant = Color(0xFFF59E0B),
            background = Color(0xFFFFF5F7),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFCE7F3),
            
            // Mode sombre
            primaryDark = Color(0xFFF9A8D4),
            primaryVariantDark = Color(0xFFF472B6),
            secondaryDark = Color(0xFFFBBF24),
            secondaryVariantDark = Color(0xFFF59E0B),
            tertiaryDark = Color(0xFFFCA5A5),
            tertiaryVariantDark = Color(0xFFF87171),
            backgroundDark = Color(0xFF1A0A14),
            surfaceDark = Color(0xFF2A1A24),
            surfaceVariantDark = Color(0xFF3F2A34)
        )
        
        AppTheme.LAVENDER_DREAM -> ThemeColors(
            // Mode clair - Lavande doux
            primary = Color(0xFF9333EA),
            primaryVariant = Color(0xFF7E22CE),
            secondary = Color(0xFFA78BFA),
            secondaryVariant = Color(0xFF8B5CF6),
            tertiary = Color(0xFFC084FC),
            tertiaryVariant = Color(0xFFA855F7),
            background = Color(0xFFFAF5FF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF3E8FF),
            
            // Mode sombre
            primaryDark = Color(0xFFD8B4FE),
            primaryVariantDark = Color(0xFFC084FC),
            secondaryDark = Color(0xFFC4B5FD),
            secondaryVariantDark = Color(0xFFA78BFA),
            tertiaryDark = Color(0xFFE9D5FF),
            tertiaryVariantDark = Color(0xFFD8B4FE),
            backgroundDark = Color(0xFF140A1A),
            surfaceDark = Color(0xFF241A2A),
            surfaceVariantDark = Color(0xFF342A3F)
        )
    }
}
