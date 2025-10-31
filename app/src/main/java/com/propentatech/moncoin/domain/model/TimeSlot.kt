package com.propentatech.moncoin.domain.model

import java.time.LocalDateTime

/**
 * Represents a time slot with start and end times
 */
data class TimeSlot(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val isAvailable: Boolean = true,
    val conflictingTaskId: String? = null
) {
    /**
     * Check if this time slot overlaps with another
     */
    fun overlapsWith(other: TimeSlot): Boolean {
        return start.isBefore(other.end) && end.isAfter(other.start)
    }
    
    /**
     * Check if this time slot contains a specific time
     */
    fun contains(time: LocalDateTime): Boolean {
        return !time.isBefore(start) && time.isBefore(end)
    }
    
    /**
     * Get the duration in minutes
     */
    fun durationMinutes(): Long {
        return java.time.Duration.between(start, end).toMinutes()
    }
    
    /**
     * Check if this slot is valid (end is after start)
     */
    fun isValid(): Boolean {
        return end.isAfter(start)
    }
}
