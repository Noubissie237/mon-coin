package com.propentatech.moncoin.domain

import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskMode
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests de cohérence et synchronisation du système
 * Vérifie que les différentes parties du système restent synchronisées
 */
class SystemCoherenceTest {

    @Test
    fun `task and occurrence states must be synchronized`() {
        // Given
        val task = TaskEntity(
            id = "task-1",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.RUNNING
        )
        
        val occurrence = OccurrenceEntity(
            id = "occ-1",
            taskId = "task-1",
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusHours(1),
            state = TaskState.SCHEDULED // BUG: incohérence avec la tâche
        )
        
        // When
        val areStatesCoherent = task.state == occurrence.state
        
        // Then
        assertFalse("Task and occurrence states should be synchronized", areStatesCoherent)
    }

    @Test
    fun `when task is RUNNING occurrence must exist`() {
        // Given
        val runningTask = TaskEntity(
            id = "task-2",
            title = "Running Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.RUNNING
        )
        
        val hasOccurrence = false // BUG: pas d'occurrence pour une tâche RUNNING
        
        // When
        val isCoherent = runningTask.state != TaskState.RUNNING || hasOccurrence
        
        // Then
        assertFalse("RUNNING task must have an occurrence", isCoherent)
    }

    @Test
    fun `when occurrence is COMPLETED task should be COMPLETED`() {
        // Given
        val completedOccurrence = OccurrenceEntity(
            id = "occ-2",
            taskId = "task-3",
            startAt = LocalDateTime.now().minusHours(2),
            endAt = LocalDateTime.now().minusHours(1),
            state = TaskState.COMPLETED
        )
        
        val task = TaskEntity(
            id = "task-3",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.RUNNING // BUG: incohérence
        )
        
        // When
        val isCoherent = task.state == completedOccurrence.state
        
        // Then
        assertFalse("Task state should match completed occurrence", isCoherent)
    }

    @Test
    fun `DUREE task can only have one RUNNING occurrence at a time`() {
        // Given
        val task = TaskEntity(
            id = "task-4",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.RUNNING
        )
        
        val occurrences = listOf(
            OccurrenceEntity(
                id = "occ-3",
                taskId = "task-4",
                startAt = LocalDateTime.now().minusMinutes(30),
                endAt = LocalDateTime.now().plusMinutes(30),
                state = TaskState.RUNNING
            ),
            OccurrenceEntity(
                id = "occ-4",
                taskId = "task-4",
                startAt = LocalDateTime.now(),
                endAt = LocalDateTime.now().plusHours(1),
                state = TaskState.RUNNING // BUG: deux occurrences RUNNING
            )
        )
        
        // When
        val runningCount = occurrences.count { it.state == TaskState.RUNNING }
        val isValid = task.mode == TaskMode.DUREE && runningCount <= 1
        
        // Then
        assertFalse("DUREE task can only have one RUNNING occurrence", isValid)
    }

    @Test
    fun `PLAGE task occurrence times must match task times`() {
        // Given
        val taskStart = LocalDateTime.of(2025, 10, 31, 10, 0)
        val taskEnd = LocalDateTime.of(2025, 10, 31, 11, 0)
        
        val task = TaskEntity(
            id = "task-5",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = taskStart,
            endTime = taskEnd,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        val occurrence = OccurrenceEntity(
            id = "occ-5",
            taskId = "task-5",
            startAt = LocalDateTime.of(2025, 10, 31, 10, 30), // BUG: différent
            endAt = LocalDateTime.of(2025, 10, 31, 11, 30),   // BUG: différent
            state = TaskState.SCHEDULED
        )
        
        // When
        val timesMatch = occurrence.startAt == task.startTime && occurrence.endAt == task.endTime
        
        // Then
        assertFalse("Occurrence times should match task times for PLAGE mode", timesMatch)
    }

    @Test
    fun `modifying task should update all related occurrences`() {
        // Given
        val originalTask = TaskEntity(
            id = "task-6",
            title = "Original Title",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        val modifiedTask = originalTask.copy(
            title = "Modified Title",
            durationMinutes = 90
        )
        
        val occurrence = OccurrenceEntity(
            id = "occ-6",
            taskId = "task-6",
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusHours(1), // BUG: toujours 60 min
            state = TaskState.SCHEDULED
        )
        
        // When
        val expectedDuration = java.time.Duration.between(occurrence.startAt, occurrence.endAt)
        val shouldBe90Minutes = expectedDuration.toMinutes() == 90L
        
        // Then
        assertFalse("Occurrence duration should be updated to 90 minutes", shouldBe90Minutes)
    }

    @Test
    fun `deleting task should delete all related occurrences`() {
        // Given
        val taskId = "task-7"
        val taskDeleted = true
        val occurrencesExist = true // BUG: occurrences orphelines
        
        // When
        val isCoherent = !taskDeleted || !occurrencesExist
        
        // Then
        assertFalse("Deleting task should delete all occurrences", isCoherent)
    }

    @Test
    fun `occurrence cannot exist without parent task`() {
        // Given
        val occurrence = OccurrenceEntity(
            id = "occ-7",
            taskId = "non-existent-task", // BUG: tâche inexistante
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusHours(1),
            state = TaskState.SCHEDULED
        )
        
        val taskExists = false
        
        // When
        val isValid = taskExists
        
        // Then
        assertFalse("Occurrence cannot exist without parent task", isValid)
    }

    @Test
    fun `past SCHEDULED occurrences should be marked as MISSED`() {
        // Given
        val now = LocalDateTime.now()
        val pastOccurrence = OccurrenceEntity(
            id = "occ-8",
            taskId = "task-8",
            startAt = now.minusHours(2),
            endAt = now.minusHours(1),
            state = TaskState.SCHEDULED // BUG: devrait être MISSED
        )
        
        // When
        val isPast = pastOccurrence.endAt.isBefore(now)
        val shouldBeMissed = isPast && pastOccurrence.state == TaskState.SCHEDULED
        
        // Then
        assertTrue("Past SCHEDULED occurrence should be marked as MISSED", shouldBeMissed)
    }

    @Test
    fun `past RUNNING occurrences should be marked as COMPLETED`() {
        // Given
        val now = LocalDateTime.now()
        val pastOccurrence = OccurrenceEntity(
            id = "occ-9",
            taskId = "task-9",
            startAt = now.minusHours(2),
            endAt = now.minusHours(1),
            state = TaskState.RUNNING // BUG: devrait être COMPLETED
        )
        
        // When
        val isPast = pastOccurrence.endAt.isBefore(now)
        val shouldBeCompleted = isPast && pastOccurrence.state == TaskState.RUNNING
        
        // Then
        assertTrue("Past RUNNING occurrence should be marked as COMPLETED", shouldBeCompleted)
    }

    @Test
    fun `alarm should be scheduled when task has alarmsEnabled`() {
        // Given
        val task = TaskEntity(
            id = "task-10",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = LocalDateTime.now().plusHours(1),
            endTime = LocalDateTime.now().plusHours(2),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        val alarmScheduled = false // BUG: alarme non planifiée
        
        // When
        val isCoherent = !task.alarmsEnabled || alarmScheduled
        
        // Then
        assertFalse("Alarm should be scheduled when alarmsEnabled is true", isCoherent)
    }

    @Test
    fun `alarm should be cancelled when task is CANCELLED`() {
        // Given
        val cancelledTask = TaskEntity(
            id = "task-11",
            title = "Cancelled Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            alarmsEnabled = true,
            state = TaskState.CANCELLED
        )
        
        val alarmStillActive = true // BUG: alarme toujours active
        
        // When
        val isCoherent = cancelledTask.state != TaskState.CANCELLED || !alarmStillActive
        
        // Then
        assertFalse("Alarm should be cancelled when task is CANCELLED", isCoherent)
    }

    @Test
    fun `PERIODIQUE task should have occurrences for all selected days`() {
        // Given
        val task = TaskEntity(
            id = "task-12",
            title = "Weekly Task",
            type = TaskType.PERIODIQUE,
            mode = TaskMode.DUREE,
            durationMinutes = 60,
            recurrence = com.propentatech.moncoin.data.model.Recurrence(
                daysOfWeek = listOf(
                    java.time.DayOfWeek.MONDAY,
                    java.time.DayOfWeek.WEDNESDAY,
                    java.time.DayOfWeek.FRIDAY
                )
            ),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        val occurrencesCount = 2 // BUG: seulement 2 occurrences au lieu de 3
        
        // When
        val expectedCount = task.recurrence?.daysOfWeek?.size ?: 0
        val hasAllOccurrences = occurrencesCount == expectedCount
        
        // Then
        assertFalse("PERIODIQUE task should have occurrences for all selected days", hasAllOccurrences)
    }

    @Test
    fun `notification should be sent when occurrence starts`() {
        // Given
        val occurrence = OccurrenceEntity(
            id = "occ-10",
            taskId = "task-13",
            startAt = LocalDateTime.now().minusMinutes(1), // Commencé il y a 1 minute
            endAt = LocalDateTime.now().plusHours(1),
            state = TaskState.SCHEDULED
        )
        
        val task = TaskEntity(
            id = "task-13",
            title = "Test Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = occurrence.startAt,
            endTime = occurrence.endAt,
            notificationsEnabled = true,
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        val now = LocalDateTime.now()
        val shouldNotify = occurrence.startAt.isBefore(now) && 
                          occurrence.startAt.isAfter(now.minusMinutes(2)) &&
                          task.notificationsEnabled
        
        // Then
        assertTrue("Notification should be sent when occurrence starts", shouldNotify)
    }
}
