package com.propentatech.moncoin.util

/**
 * Utilitaires pour formater le temps de manière lisible
 */
object TimeFormatUtils {
    
    /**
     * Formate une durée en minutes en format lisible
     * - Moins de 60 min : "45 min"
     * - 60 min exactement : "1h"
     * - Plus de 60 min sans reste : "2h"
     * - Plus de 60 min avec reste : "2h 30min"
     */
    fun formatDuration(minutes: Int): String {
        return when {
            minutes < 60 -> "${minutes}min"
            minutes % 60 == 0 -> "${minutes / 60}h"
            else -> {
                val hours = minutes / 60
                val mins = minutes % 60
                "${hours}h ${mins}min"
            }
        }
    }
    
    /**
     * Formate une durée en minutes en format court
     * - Moins de 60 min : "45min"
     * - 60 min et plus : "2h30" ou "2h"
     */
    fun formatDurationShort(minutes: Int): String {
        return when {
            minutes < 60 -> "${minutes}min"
            minutes % 60 == 0 -> "${minutes / 60}h"
            else -> "${minutes / 60}h${minutes % 60}"
        }
    }
}
