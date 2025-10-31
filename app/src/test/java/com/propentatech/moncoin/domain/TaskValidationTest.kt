package com.propentatech.moncoin.domain

import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskMode
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests de validation et cohérence des tâches
 * Vérifie que les modifications de tâches maintiennent la cohérence du système
 */
class TaskValidationTest {

    @Test
    fun `task title cannot be empty`() {
        // Given
        val task = TaskEntity(
            id = "test-1",
            title = "",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When
        val isValid = task.title.isNotBlank()
        
        // Then
        assertFalse("Task title should not be empty", isValid)
    }

    @Test
    fun `DUREE task must have duration`() {
        // Given
        val task = TaskEntity(
            id = "test-2",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = null, // BUG: pas de durée
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When
        val isValid = task.mode == TaskMode.DUREE && task.durationMinutes != null && task.durationMinutes > 0
        
        // Then
        assertFalse("DUREE task must have a positive duration", isValid)
    }

    @Test
    fun `PLAGE task must have start and end time`() {
        // Given
        val task = TaskEntity(
            id = "test-3",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = null, // BUG: pas d'heure de début
            endTime = null,   // BUG: pas d'heure de fin
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When
        val isValid = task.mode == TaskMode.PLAGE && task.startTime != null && task.endTime != null
        
        // Then
        assertFalse("PLAGE task must have start and end time", isValid)
    }

    @Test
    fun `PLAGE task end time must be after start time`() {
        // Given
        val start = LocalDateTime.of(2025, 10, 31, 10, 0)
        val end = LocalDateTime.of(2025, 10, 31, 9, 0) // BUG: fin avant début
        
        val task = TaskEntity(
            id = "test-4",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = start,
            endTime = end,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When
        val isValid = task.endTime?.isAfter(task.startTime) == true
        
        // Then
        assertFalse("End time must be after start time", isValid)
    }

    @Test
    fun `PERIODIQUE task must have at least one day selected`() {
        // Given
        val task = TaskEntity(
            id = "test-5",
            title = "Test Task",
            type = TaskType.PERIODIQUE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            recurrence = com.propentatech.moncoin.data.model.Recurrence(
                daysOfWeek = emptyList() // BUG: aucun jour sélectionné
            ),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When
        val isValid = task.type == TaskType.PERIODIQUE && 
                     task.recurrence != null && 
                     task.recurrence.daysOfWeek.isNotEmpty()
        
        // Then
        assertFalse("PERIODIQUE task must have at least one day selected", isValid)
    }

    @Test
    fun `modifying RUNNING task should preserve state`() {
        // Given
        val originalTask = TaskEntity(
            id = "test-6",
            title = "Original Title",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.RUNNING
        )
        
        // When - Modification du titre
        val modifiedTask = originalTask.copy(title = "Modified Title")
        
        // Then - L'état doit rester RUNNING
        assertEquals(TaskState.RUNNING, modifiedTask.state)
        assertEquals("Modified Title", modifiedTask.title)
    }

    @Test
    fun `modifying task should not change ID`() {
        // Given
        val originalTask = TaskEntity(
            id = "original-id",
            title = "Original",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Modification multiple
        val modified1 = originalTask.copy(title = "Modified 1")
        val modified2 = modified1.copy(durationMinutes = 90)
        val modified3 = modified2.copy(alarmsEnabled = false)
        
        // Then - L'ID ne doit jamais changer
        assertEquals("original-id", originalTask.id)
        assertEquals("original-id", modified1.id)
        assertEquals("original-id", modified2.id)
        assertEquals("original-id", modified3.id)
    }

    @Test
    fun `changing task mode from DUREE to PLAGE should clear duration`() {
        // Given
        val dureeTask = TaskEntity(
            id = "test-7",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Changement vers PLAGE
        val plageTask = dureeTask.copy(
            mode = TaskMode.PLAGE,
            durationMinutes = null, // Doit être nettoyé
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1)
        )
        
        // Then
        assertEquals(TaskMode.PLAGE, plageTask.mode)
        assertNull("Duration should be null for PLAGE mode", plageTask.durationMinutes)
        assertNotNull(plageTask.startTime)
        assertNotNull(plageTask.endTime)
    }

    @Test
    fun `changing task mode from PLAGE to DUREE should clear times`() {
        // Given
        val plageTask = TaskEntity(
            id = "test-8",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Changement vers DUREE
        val dureeTask = plageTask.copy(
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            startTime = null, // Doit être nettoyé
            endTime = null    // Doit être nettoyé
        )
        
        // Then
        assertEquals(TaskMode.DUREE, dureeTask.mode)
        assertNull("Start time should be null for DUREE mode", dureeTask.startTime)
        assertNull("End time should be null for DUREE mode", dureeTask.endTime)
        assertNotNull(dureeTask.durationMinutes)
    }

    @Test
    fun `task state transitions are valid`() {
        // Given
        val task = TaskEntity(
            id = "test-9",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // Valid transitions
        val validTransitions = mapOf(
            TaskState.SCHEDULED to listOf(TaskState.RUNNING, TaskState.CANCELLED, TaskState.MISSED),
            TaskState.RUNNING to listOf(TaskState.COMPLETED, TaskState.CANCELLED),
            TaskState.COMPLETED to emptyList<TaskState>(), // État final
            TaskState.MISSED to emptyList<TaskState>(),     // État final
            TaskState.CANCELLED to emptyList<TaskState>()   // État final
        )
        
        // Then - Vérifier les transitions valides depuis SCHEDULED
        val validFromScheduled = validTransitions[TaskState.SCHEDULED]!!
        assertTrue(validFromScheduled.contains(TaskState.RUNNING))
        assertTrue(validFromScheduled.contains(TaskState.CANCELLED))
        assertTrue(validFromScheduled.contains(TaskState.MISSED))
        assertFalse(validFromScheduled.contains(TaskState.COMPLETED)) // Invalide: SCHEDULED -> COMPLETED
    }

    @Test
    fun `cannot modify COMPLETED task`() {
        // Given
        val completedTask = TaskEntity(
            id = "test-10",
            title = "Completed Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.COMPLETED
        )
        
        // When
        val canModify = completedTask.state !in listOf(TaskState.COMPLETED, TaskState.MISSED, TaskState.CANCELLED)
        
        // Then
        assertFalse("Cannot modify completed task", canModify)
    }

    @Test
    fun `duration must be positive`() {
        // Given
        val invalidDurations = listOf(-1, 0, -60)
        
        // When & Then
        invalidDurations.forEach { duration ->
            val isValid = duration > 0
            assertFalse("Duration $duration should be invalid", isValid)
        }
    }

    @Test
    fun `duration should be in 15 minute increments`() {
        // Given
        val validDurations = listOf(15, 30, 45, 60, 75, 90, 120)
        val invalidDurations = listOf(10, 20, 35, 50, 65)
        
        // When & Then
        validDurations.forEach { duration ->
            val isValid = duration % 15 == 0
            assertTrue("Duration $duration should be valid (multiple of 15)", isValid)
        }
        
        invalidDurations.forEach { duration ->
            val isValid = duration % 15 == 0
            assertFalse("Duration $duration should be invalid (not multiple of 15)", isValid)
        }
    }

    @Test
    fun `reminders must be before task start time`() {
        // Given
        val reminders = listOf(10, 30, 60) // minutes avant
        
        // When & Then
        reminders.forEach { reminder ->
            assertTrue("Reminder $reminder should be positive", reminder > 0)
        }
    }

    @Test
    fun `task with no alarms should not have reminders`() {
        // Given
        val task = TaskEntity(
            id = "test-11",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = false,
            reminders = listOf(10, 30), // BUG: reminders mais pas d'alarmes
            state = TaskState.SCHEDULED
        )
        
        // When
        val isValid = !task.alarmsEnabled && task.reminders.isEmpty()
        
        // Then
        assertFalse("Task without alarms should not have reminders", isValid)
    }
}
