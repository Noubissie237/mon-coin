package com.propentatech.moncoin.data.model

import com.propentatech.moncoin.data.local.entity.TaskEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests pour l'entité TaskEntity
 */
class TaskEntityTest {

    @Test
    fun `create task with valid data`() {
        // Given & When
        val task = TaskEntity(
            id = "test-1",
            title = "Test Task",
            description = "Description",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // Then
        assertEquals("test-1", task.id)
        assertEquals("Test Task", task.title)
        assertEquals(TaskType.PONCTUELLE, task.type)
        assertEquals(TaskMode.PLAGE, task.mode)
        assertEquals(TaskState.SCHEDULED, task.state)
        assertTrue(task.alarmsEnabled)
    }

    @Test
    fun `task with DUREE mode has duration`() {
        // Given & When
        val task = TaskEntity(
            id = "test-2",
            title = "Duration Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // Then
        assertEquals(TaskMode.DUREE, task.mode)
        assertEquals(60, task.durationMinutes)
        assertNull(task.startTime)
        assertNull(task.endTime)
    }

    @Test
    fun `task with PLAGE mode has start and end time`() {
        // Given
        val start = LocalDateTime.now()
        val end = start.plusHours(2)

        // When
        val task = TaskEntity(
            id = "test-3",
            title = "Time Range Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = start,
            endTime = end,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // Then
        assertEquals(TaskMode.PLAGE, task.mode)
        assertEquals(start, task.startTime)
        assertEquals(end, task.endTime)
        assertNull(task.durationMinutes)
    }

    @Test
    fun `task copy preserves data`() {
        // Given
        val original = TaskEntity(
            id = "test-4",
            title = "Original",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 30,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // When
        val copy = original.copy(title = "Modified")

        // Then
        assertEquals("Modified", copy.title)
        assertEquals(original.id, copy.id)
        assertEquals(original.type, copy.type)
        assertEquals(original.mode, copy.mode)
        assertEquals(original.durationMinutes, copy.durationMinutes)
    }

    @Test
    fun `task with tags`() {
        // Given & When
        val task = TaskEntity(
            id = "test-5",
            title = "Tagged Task",
            tags = listOf("urgent", "work", "important"),
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 45,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // Then
        assertEquals(3, task.tags.size)
        assertTrue(task.tags.contains("urgent"))
        assertTrue(task.tags.contains("work"))
        assertTrue(task.tags.contains("important"))
    }

    @Test
    fun `task with reminders`() {
        // Given & When
        val task = TaskEntity(
            id = "test-6",
            title = "Task with Reminders",
            reminders = listOf(10, 30, 60),
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            alarmsEnabled = true,
            notificationsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // Then
        assertEquals(3, task.reminders.size)
        assertTrue(task.reminders.contains(10))
        assertTrue(task.reminders.contains(30))
        assertTrue(task.reminders.contains(60))
        assertTrue(task.notificationsEnabled)
    }

    @Test
    fun `task state transitions`() {
        // Given
        val task = TaskEntity(
            id = "test-7",
            title = "State Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 30,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )

        // When & Then - SCHEDULED to RUNNING
        val running = task.copy(state = TaskState.RUNNING)
        assertEquals(TaskState.RUNNING, running.state)

        // RUNNING to COMPLETED
        val completed = running.copy(state = TaskState.COMPLETED)
        assertEquals(TaskState.COMPLETED, completed.state)

        // SCHEDULED to MISSED
        val missed = task.copy(state = TaskState.MISSED)
        assertEquals(TaskState.MISSED, missed.state)

        // SCHEDULED to CANCELLED
        val cancelled = task.copy(state = TaskState.CANCELLED)
        assertEquals(TaskState.CANCELLED, cancelled.state)
    }
}
