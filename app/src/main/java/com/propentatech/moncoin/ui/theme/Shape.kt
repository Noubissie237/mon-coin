package com.propentatech.moncoin.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Formes modernes avec des coins arrondis élégants
val Shapes = Shapes(
    // Petites formes - Pour les chips, badges, etc.
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    
    // Formes moyennes - Pour les cartes, boutons, etc.
    medium = RoundedCornerShape(12.dp),
    
    // Grandes formes - Pour les dialogs, bottom sheets, etc.
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
