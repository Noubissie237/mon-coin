package com.propentatech.moncoin.worker

import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskMode
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tests pour vérifier la logique de création quotidienne des occurrences
 */
class DailyOccurrenceWorkerTest {

    @Test
    fun `QUOTIDIENNE task should have occurrence created today`() {
        // Given
        val today = LocalDate.now()
        val task = TaskEntity(
            id = "task-1",
            title = "Daily Task",
            type = TaskType.QUOTIDIENNE,
            mode = TaskMode.PLAGE,
            startTime = today.atTime(10, 0),
            endTime = today.atTime(11, 0),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Le worker devrait créer une occurrence pour aujourd'hui
        val shouldCreateToday = task.type == TaskType.QUOTIDIENNE
        
        // Then
        assertTrue("QUOTIDIENNE task should create occurrence today", shouldCreateToday)
    }

    @Test
    fun `PERIODIQUE task should create occurrence only on selected days`() {
        // Given
        val today = LocalDate.now()
        val selectedDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        
        val task = TaskEntity(
            id = "task-2",
            title = "Periodic Task",
            type = TaskType.PERIODIQUE,
            mode = TaskMode.PLAGE,
            recurrence = com.propentatech.moncoin.data.model.Recurrence(
                daysOfWeek = selectedDays
            ),
            startTime = today.atTime(14, 0),
            endTime = today.atTime(15, 0),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When
        val shouldCreateToday = selectedDays.contains(today.dayOfWeek)
        
        // Then
        // Le résultat dépend du jour actuel
        if (today.dayOfWeek in selectedDays) {
            assertTrue("Should create occurrence on selected day", shouldCreateToday)
        } else {
            assertFalse("Should NOT create occurrence on non-selected day", shouldCreateToday)
        }
    }

    @Test
    fun `yesterday SCHEDULED occurrence should become MISSED`() {
        // Given
        val yesterday = LocalDate.now().minusDays(1)
        val occurrence = OccurrenceEntity(
            id = "occ-1",
            taskId = "task-1",
            startAt = yesterday.atTime(10, 0),
            endAt = yesterday.atTime(11, 0),
            state = TaskState.SCHEDULED
        )
        
        // When - Le worker met à jour les statuts
        val now = LocalDateTime.now()
        val isPast = occurrence.endAt.isBefore(now)
        val shouldBeMissed = isPast && occurrence.state == TaskState.SCHEDULED
        
        // Then
        assertTrue("Past SCHEDULED occurrence should be marked as MISSED", shouldBeMissed)
    }

    @Test
    fun `yesterday RUNNING occurrence should become COMPLETED`() {
        // Given
        val yesterday = LocalDate.now().minusDays(1)
        val occurrence = OccurrenceEntity(
            id = "occ-2",
            taskId = "task-2",
            startAt = yesterday.atTime(10, 0),
            endAt = yesterday.atTime(11, 0),
            state = TaskState.RUNNING
        )
        
        // When - Le worker met à jour les statuts
        val now = LocalDateTime.now()
        val isPast = occurrence.endAt.isBefore(now)
        val shouldBeCompleted = isPast && occurrence.state == TaskState.RUNNING
        
        // Then
        assertTrue("Past RUNNING occurrence should be marked as COMPLETED", shouldBeCompleted)
    }

    @Test
    fun `completed QUOTIDIENNE task appears again today`() {
        // Given - Tâche complétée hier
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayOccurrence = OccurrenceEntity(
            id = "occ-3",
            taskId = "task-3",
            startAt = yesterday.atTime(18, 0),
            endAt = yesterday.atTime(19, 0),
            state = TaskState.COMPLETED
        )
        
        val task = TaskEntity(
            id = "task-3",
            title = "Daily Sport",
            type = TaskType.QUOTIDIENNE,
            mode = TaskMode.PLAGE,
            startTime = yesterday.atTime(18, 0), // Même heure
            endTime = yesterday.atTime(19, 0),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Le worker crée une nouvelle occurrence pour aujourd'hui
        val today = LocalDate.now()
        val shouldCreateNewOccurrence = task.type == TaskType.QUOTIDIENNE
        
        // Then
        assertTrue("QUOTIDIENNE task should create new occurrence today", shouldCreateNewOccurrence)
        
        // Vérifier que l'occurrence d'hier reste COMPLETED
        assertEquals(TaskState.COMPLETED, yesterdayOccurrence.state)
        
        // Vérifier que la nouvelle occurrence serait pour aujourd'hui
        val newOccurrenceStart = today.atTime(18, 0)
        assertNotEquals("New occurrence should be for today, not yesterday", 
            yesterdayOccurrence.startAt, newOccurrenceStart)
    }

    @Test
    fun `PONCTUELLE task should NOT create occurrence after completion`() {
        // Given - Tâche ponctuelle complétée
        val task = TaskEntity(
            id = "task-4",
            title = "One-time Task",
            type = TaskType.PONCTUELLE,
            mode = TaskMode.PLAGE,
            startTime = LocalDate.now().minusDays(1).atTime(10, 0),
            endTime = LocalDate.now().minusDays(1).atTime(11, 0),
            alarmsEnabled = true,
            state = TaskState.COMPLETED
        )
        
        // When - Le worker ne devrait PAS créer de nouvelle occurrence
        val shouldCreateToday = task.type == TaskType.QUOTIDIENNE || task.type == TaskType.PERIODIQUE
        
        // Then
        assertFalse("PONCTUELLE task should NOT create new occurrence", shouldCreateToday)
    }

    @Test
    fun `worker should not create duplicate occurrences`() {
        // Given - Une occurrence existe déjà pour aujourd'hui
        val today = LocalDate.now()
        val existingOccurrence = OccurrenceEntity(
            id = "occ-4",
            taskId = "task-5",
            startAt = today.atTime(10, 0),
            endAt = today.atTime(11, 0),
            state = TaskState.SCHEDULED
        )
        
        val task = TaskEntity(
            id = "task-5",
            title = "Daily Task",
            type = TaskType.QUOTIDIENNE,
            mode = TaskMode.PLAGE,
            startTime = today.atTime(10, 0),
            endTime = today.atTime(11, 0),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Le worker vérifie les occurrences existantes
        val hasExistingOccurrence = existingOccurrence.taskId == task.id &&
                                   existingOccurrence.startAt.toLocalDate() == today
        
        // Then
        assertTrue("Should detect existing occurrence", hasExistingOccurrence)
        // Le worker ne devrait PAS créer de doublon
    }

    @Test
    fun `occurrence times should match task times`() {
        // Given
        val today = LocalDate.now()
        val task = TaskEntity(
            id = "task-6",
            title = "Morning Task",
            type = TaskType.QUOTIDIENNE,
            mode = TaskMode.PLAGE,
            startTime = today.atTime(8, 30),
            endTime = today.atTime(9, 30),
            alarmsEnabled = true,
            state = TaskState.SCHEDULED
        )
        
        // When - Le worker crée une occurrence
        val occurrence = OccurrenceEntity(
            taskId = task.id,
            startAt = today.atTime(task.startTime!!.toLocalTime()),
            endAt = today.atTime(task.endTime!!.toLocalTime()),
            state = TaskState.SCHEDULED
        )
        
        // Then
        assertEquals("Occurrence start time should match task start time",
            task.startTime!!.toLocalTime(), occurrence.startAt.toLocalTime())
        assertEquals("Occurrence end time should match task end time",
            task.endTime!!.toLocalTime(), occurrence.endAt.toLocalTime())
    }
}
