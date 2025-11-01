package com.propentatech.moncoin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

/**
 * Composant Timer pour afficher le temps restant ou écoulé d'une tâche
 * @param endTime L'heure de fin de la tâche
 * @param isCompact Si true, affiche une version compacte du timer
 */
@Composable
fun TaskTimer(
    endTime: LocalDateTime,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    var timeRemaining by remember { mutableStateOf(Duration.ZERO) }
    var isOverdue by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    
    // Mettre à jour le timer toutes les secondes
    LaunchedEffect(endTime) {
        while (true) {
            val now = LocalDateTime.now()
            val duration = Duration.between(now, endTime)
            
            if (duration.isNegative) {
                // Le temps est écoulé, arrêter le timer à 00:00
                isOverdue = true
                isFinished = true
                timeRemaining = Duration.ZERO
                break // Arrêter la boucle
            } else {
                isOverdue = false
                timeRemaining = duration
            }
            
            delay(1000) // Mise à jour chaque seconde
        }
    }
    
    val hours = timeRemaining.toHours()
    val minutes = timeRemaining.toMinutes() % 60
    val seconds = timeRemaining.seconds % 60
    
    val timeText = when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
    
    val color = when {
        isFinished -> MaterialTheme.colorScheme.error
        timeRemaining.toMinutes() < 5 -> MaterialTheme.colorScheme.error
        timeRemaining.toMinutes() < 15 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    if (isCompact) {
        // Version compacte pour les petites cards
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // Version complète pour les grandes cards
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
                Column {
                    Text(
                        text = if (isFinished) "Temps écoulé" else "Temps restant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.titleLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Composant pour afficher le temps écoulé depuis le début d'une tâche
 * @param startTime L'heure de début de la tâche
 */
@Composable
fun ElapsedTimer(
    startTime: LocalDateTime,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true
) {
    var timeElapsed by remember { mutableStateOf(Duration.ZERO) }
    
    LaunchedEffect(startTime) {
        while (true) {
            val now = LocalDateTime.now()
            timeElapsed = Duration.between(startTime, now)
            delay(1000)
        }
    }
    
    val hours = timeElapsed.toHours()
    val minutes = timeElapsed.toMinutes() % 60
    val seconds = timeElapsed.seconds % 60
    
    val timeText = when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = timeText,
            style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
