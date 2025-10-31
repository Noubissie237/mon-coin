package com.propentatech.moncoin.data.model

import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests pour l'entité OccurrenceEntity
 */
class OccurrenceEntityTest {

    @Test
    fun `create occurrence with valid data`() {
        // Given
        val start = LocalDateTime.now()
        val end = start.plusHours(1)

        // When
        val occurrence = OccurrenceEntity(
            id = "occ-1",
            taskId = "task-1",
            startAt = start,
            endAt = end,
            state = TaskState.SCHEDULED
        )

        // Then
        assertEquals("occ-1", occurrence.id)
        assertEquals("task-1", occurrence.taskId)
        assertEquals(start, occurrence.startAt)
        assertEquals(end, occurrence.endAt)
        assertEquals(TaskState.SCHEDULED, occurrence.state)
    }

    @Test
    fun `occurrence duration is calculated correctly`() {
        // Given
        val start = LocalDateTime.of(2025, 10, 31, 10, 0)
        val end = LocalDateTime.of(2025, 10, 31, 12, 30)

        // When
        val occurrence = OccurrenceEntity(
            id = "occ-2",
            taskId = "task-2",
            startAt = start,
            endAt = end,
            state = TaskState.SCHEDULED
        )

        // Then
        val duration = java.time.Duration.between(occurrence.startAt, occurrence.endAt)
        assertEquals(150, duration.toMinutes()) // 2h30 = 150 minutes
    }

    @Test
    fun `occurrence state can be updated`() {
        // Given
        val occurrence = OccurrenceEntity(
            id = "occ-3",
            taskId = "task-3",
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusHours(1),
            state = TaskState.SCHEDULED
        )

        // When
        val running = occurrence.copy(state = TaskState.RUNNING)
        val completed = running.copy(state = TaskState.COMPLETED)

        // Then
        assertEquals(TaskState.SCHEDULED, occurrence.state)
        assertEquals(TaskState.RUNNING, running.state)
        assertEquals(TaskState.COMPLETED, completed.state)
    }

    @Test
    fun `occurrence is in past`() {
        // Given
        val pastOccurrence = OccurrenceEntity(
            id = "occ-4",
            taskId = "task-4",
            startAt = LocalDateTime.now().minusHours(2),
            endAt = LocalDateTime.now().minusHours(1),
            state = TaskState.SCHEDULED
        )

        // When
        val now = LocalDateTime.now()
        val isInPast = pastOccurrence.endAt.isBefore(now)

        // Then
        assertTrue(isInPast)
    }

    @Test
    fun `occurrence is in future`() {
        // Given
        val futureOccurrence = OccurrenceEntity(
            id = "occ-5",
            taskId = "task-5",
            startAt = LocalDateTime.now().plusHours(1),
            endAt = LocalDateTime.now().plusHours(2),
            state = TaskState.SCHEDULED
        )

        // When
        val now = LocalDateTime.now()
        val isInFuture = futureOccurrence.startAt.isAfter(now)

        // Then
        assertTrue(isInFuture)
    }

    @Test
    fun `occurrence is currently running`() {
        // Given
        val runningOccurrence = OccurrenceEntity(
            id = "occ-6",
            taskId = "task-6",
            startAt = LocalDateTime.now().minusMinutes(30),
            endAt = LocalDateTime.now().plusMinutes(30),
            state = TaskState.RUNNING
        )

        // When
        val now = LocalDateTime.now()
        val isRunning = runningOccurrence.startAt.isBefore(now) && 
                       runningOccurrence.endAt.isAfter(now) &&
                       runningOccurrence.state == TaskState.RUNNING

        // Then
        assertTrue(isRunning)
    }
}
