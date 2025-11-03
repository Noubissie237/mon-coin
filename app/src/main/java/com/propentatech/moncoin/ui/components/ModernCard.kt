package com.propentatech.moncoin.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Carte moderne avec animations et élévation
 */
@Composable
fun ModernCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animation de l'élévation au clic
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation / 2 else elevation,
        animationSpec = tween(durationMillis = 150),
        label = "elevation"
    )
    
    // Animation de la couleur au clic
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            containerColor.copy(alpha = 0.95f)
        } else {
            containerColor
        },
        animationSpec = tween(durationMillis = 150),
        label = "color"
    )
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = CardDefaults.cardColors(
                containerColor = animatedColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation,
                pressedElevation = animatedElevation / 2,
                hoveredElevation = animatedElevation * 1.2f
            ),
            border = border,
            interactionSource = interactionSource,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation
            ),
            border = border,
            content = content
        )
    }
}
