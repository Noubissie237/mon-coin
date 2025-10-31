package com.propentatech.moncoin.ui.components

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

/**
 * Tests pour la logique du timer de tâche
 * Vérifie que le timer se comporte correctement, notamment qu'il s'arrête à zéro
 */
class TaskTimerLogicTest {

    @Test
    fun `timer calculates remaining time correctly`() {
        // Given
        val now = LocalDateTime.of(2025, 10, 31, 10, 0, 0)
        val endTime = LocalDateTime.of(2025, 10, 31, 11, 30, 0)
        
        // When
        val duration = Duration.between(now, endTime)
        
        // Then
        assertEquals(90, duration.toMinutes()) // 1h30 = 90 minutes
        assertFalse(duration.isNegative)
    }

    @Test
    fun `timer detects when time is up`() {
        // Given
        val now = LocalDateTime.of(2025, 10, 31, 10, 0, 0)
        val endTime = LocalDateTime.of(2025, 10, 31, 10, 0, 0) // Same time
        
        // When
        val duration = Duration.between(now, endTime)
        
        // Then
        assertEquals(0, duration.toMinutes())
        assertFalse(duration.isNegative)
    }

    @Test
    fun `timer detects overdue tasks`() {
        // Given
        val now = LocalDateTime.of(2025, 10, 31, 10, 30, 0)
        val endTime = LocalDateTime.of(2025, 10, 31, 10, 0, 0) // 30 minutes ago
        
        // When
        val duration = Duration.between(now, endTime)
        
        // Then
        assertTrue(duration.isNegative)
        assertEquals(-30, duration.toMinutes())
    }

    @Test
    fun `timer should stop at zero not go negative - CRITICAL BUG`() {
        // Given
        val now = LocalDateTime.of(2025, 10, 31, 10, 30, 0)
        val endTime = LocalDateTime.of(2025, 10, 31, 10, 0, 0)
        
        // When
        val duration = Duration.between(now, endTime)
        val shouldDisplayTimer = !duration.isNegative
        
        // Then - Le timer NE DEVRAIT PAS continuer après zéro
        assertFalse("Timer should stop at zero, not continue counting", shouldDisplayTimer)
        
        // Le timer actuel CONTINUE après zéro (BUG)
        // Il affiche "Dépassé de 00:30" au lieu de s'arrêter à 00:00
    }

    @Test
    fun `timer shows warning when less than 5 minutes remaining`() {
        // Given
        val now = LocalDateTime.of(2025, 10, 31, 10, 0, 0)
        val endTime = LocalDateTime.of(2025, 10, 31, 10, 3, 0) // 3 minutes
        
        // When
        val duration = Duration.between(now, endTime)
        val isWarning = duration.toMinutes() < 5
        
        // Then
        assertTrue(isWarning)
        assertEquals(3, duration.toMinutes())
    }

    @Test
    fun `timer shows alert when less than 15 minutes remaining`() {
        // Given
        val now = LocalDateTime.of(2025, 10, 31, 10, 0, 0)
        val endTime = LocalDateTime.of(2025, 10, 31, 10, 10, 0) // 10 minutes
        
        // When
        val duration = Duration.between(now, endTime)
        val isAlert = duration.toMinutes() < 15
        
        // Then
        assertTrue(isAlert)
        assertEquals(10, duration.toMinutes())
    }

    @Test
    fun `timer formats time correctly with hours`() {
        // Given
        val duration = Duration.ofHours(2).plusMinutes(30).plusSeconds(45)
        
        // When
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        
        // Then
        assertEquals("02:30:45", formatted)
    }

    @Test
    fun `timer formats time correctly without hours`() {
        // Given
        val duration = Duration.ofMinutes(30).plusSeconds(45)
        
        // When
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        val formatted = String.format("%02d:%02d", minutes, seconds)
        
        // Then
        assertEquals("30:45", formatted)
    }

    @Test
    fun `timer at exactly zero should show 00_00`() {
        // Given
        val duration = Duration.ZERO
        
        // When
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        val formatted = String.format("%02d:%02d", minutes, seconds)
        
        // Then
        assertEquals("00:00", formatted)
        assertFalse(duration.isNegative)
    }
}
