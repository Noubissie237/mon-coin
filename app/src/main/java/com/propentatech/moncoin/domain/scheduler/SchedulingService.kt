package com.propentatech.moncoin.domain.scheduler

import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.SleepScheduleEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.SleepScheduleRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import com.propentatech.moncoin.domain.model.TimeSlot
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling tasks and detecting conflicts
 */
@Singleton
class SchedulingService @Inject constructor(
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val sleepScheduleRepository: SleepScheduleRepository
) {
    
    /**
     * Check if a time slot conflicts with existing occurrences
     */
    suspend fun hasConflict(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        excludeTaskId: String? = null
    ): Boolean {
        val occurrences = occurrenceRepository.getOccurrencesBetween(
            startTime.minusDays(1),
            endTime.plusDays(1)
        ).first()
        
        return occurrences.any { occurrence ->
            if (excludeTaskId != null) {
                val task = taskRepository.getTaskById(occurrence.taskId)
                if (task?.id == excludeTaskId) return@any false
            }
            
            // Check overlap
            occurrence.startAt.isBefore(endTime) && occurrence.endAt.isAfter(startTime)
        }
    }
    
    /**
     * Check if a time slot conflicts with sleep schedule
     */
    suspend fun conflictsWithSleep(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Boolean {
        val sleepSchedule = sleepScheduleRepository.getSleepScheduleOnce() ?: return false
        
        val sleepStart = sleepSchedule.getStartLocalTime()
        val sleepEnd = sleepSchedule.getEndLocalTime()
        
        // Handle sleep schedule that crosses midnight
        val sleepCrossesMidnight = sleepEnd.isBefore(sleepStart)
        
        val taskStart = startTime.toLocalTime()
        val taskEnd = endTime.toLocalTime()
        
        return if (sleepCrossesMidnight) {
            // Sleep goes from e.g., 23:00 to 05:00
            (taskStart.isAfter(sleepStart) || taskStart.isBefore(sleepEnd)) ||
            (taskEnd.isAfter(sleepStart) || taskEnd.isBefore(sleepEnd))
        } else {
            // Normal sleep schedule e.g., 22:00 to 06:00 (same day)
            (taskStart.isAfter(sleepStart) && taskStart.isBefore(sleepEnd)) ||
            (taskEnd.isAfter(sleepStart) && taskEnd.isBefore(sleepEnd)) ||
            (taskStart.isBefore(sleepStart) && taskEnd.isAfter(sleepEnd))
        }
    }
    
    /**
     * Get available time slots for a given date and duration
     */
    suspend fun getAvailableSlots(
        date: LocalDate,
        durationMinutes: Int,
        minGapMinutes: Int = 1
    ): List<TimeSlot> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(23, 59)
        
        // Get all occurrences for the day
        val occurrences = occurrenceRepository.getOccurrencesBetween(
            startOfDay,
            endOfDay
        ).first()
        
        // Get sleep schedule
        val sleepSchedule = sleepScheduleRepository.getSleepScheduleOnce()
        
        // Create occupied slots
        val occupiedSlots = mutableListOf<TimeSlot>()
        
        // Add existing occurrences
        occurrences.forEach { occurrence ->
            occupiedSlots.add(
                TimeSlot(
                    start = occurrence.startAt,
                    end = occurrence.endAt,
                    isAvailable = false,
                    conflictingTaskId = occurrence.taskId
                )
            )
        }
        
        // Add sleep schedule
        sleepSchedule?.let { sleep ->
            val sleepStart = sleep.getStartLocalTime()
            val sleepEnd = sleep.getEndLocalTime()
            
            if (sleepEnd.isBefore(sleepStart)) {
                // Sleep crosses midnight
                occupiedSlots.add(
                    TimeSlot(
                        start = date.atTime(sleepStart),
                        end = date.plusDays(1).atTime(sleepEnd),
                        isAvailable = false
                    )
                )
            } else {
                occupiedSlots.add(
                    TimeSlot(
                        start = date.atTime(sleepStart),
                        end = date.atTime(sleepEnd),
                        isAvailable = false
                    )
                )
            }
        }
        
        // Sort occupied slots by start time
        occupiedSlots.sortBy { it.start }
        
        // Find available slots
        val availableSlots = mutableListOf<TimeSlot>()
        var currentTime = startOfDay
        
        for (occupiedSlot in occupiedSlots) {
            if (currentTime.isBefore(occupiedSlot.start)) {
                val gap = java.time.Duration.between(currentTime, occupiedSlot.start).toMinutes()
                if (gap >= durationMinutes + minGapMinutes) {
                    availableSlots.add(
                        TimeSlot(
                            start = currentTime,
                            end = occupiedSlot.start.minusMinutes(minGapMinutes.toLong()),
                            isAvailable = true
                        )
                    )
                }
            }
            currentTime = maxOf(currentTime, occupiedSlot.end.plusMinutes(minGapMinutes.toLong()))
        }
        
        // Check if there's time left at the end of the day
        if (currentTime.isBefore(endOfDay)) {
            val gap = java.time.Duration.between(currentTime, endOfDay).toMinutes()
            if (gap >= durationMinutes) {
                availableSlots.add(
                    TimeSlot(
                        start = currentTime,
                        end = endOfDay,
                        isAvailable = true
                    )
                )
            }
        }
        
        return availableSlots
    }
    
    /**
     * Generate occurrences for a recurring task
     */
    suspend fun generateOccurrences(
        task: TaskEntity,
        fromDate: LocalDate,
        count: Int = 30
    ): List<OccurrenceEntity> {
        if (task.type == TaskType.PONCTUELLE) {
            // Non-recurring task, single occurrence
            return if (task.startTime != null && task.endTime != null) {
                listOf(
                    OccurrenceEntity(
                        taskId = task.id,
                        startAt = task.startTime,
                        endAt = task.endTime,
                        state = TaskState.SCHEDULED
                    )
                )
            } else {
                emptyList()
            }
        }
        
        val occurrences = mutableListOf<OccurrenceEntity>()
        var currentDate = fromDate
        var generated = 0
        
        val recurrence = task.recurrence ?: return emptyList()
        val startTime = task.startTime?.toLocalTime() ?: return emptyList()
        val endTime = task.endTime?.toLocalTime() ?: return emptyList()
        
        // Generate up to 'count' occurrences or 90 days, whichever comes first
        val maxDate = fromDate.plusDays(90)
        
        while (generated < count && currentDate.isBefore(maxDate)) {
            val shouldGenerate = when (task.type) {
                TaskType.QUOTIDIENNE -> true
                TaskType.PERIODIQUE -> {
                    recurrence.daysOfWeek.contains(currentDate.dayOfWeek)
                }
                else -> false
            }
            
            if (shouldGenerate) {
                val occurrence = OccurrenceEntity(
                    taskId = task.id,
                    startAt = currentDate.atTime(startTime),
                    endAt = currentDate.atTime(endTime),
                    state = TaskState.SCHEDULED
                )
                
                // Check if this occurrence conflicts with existing ones
                val hasConflict = hasConflict(
                    occurrence.startAt,
                    occurrence.endAt,
                    task.id
                )
                
                if (!hasConflict) {
                    occurrences.add(occurrence)
                    generated++
                }
            }
            
            currentDate = currentDate.plusDays(recurrence.interval.toLong())
        }
        
        return occurrences
    }
    
    /**
     * Suggest alternative time slots when there's a conflict
     */
    suspend fun suggestAlternatives(
        date: LocalDate,
        durationMinutes: Int,
        preferredStartTime: LocalTime? = null,
        count: Int = 5
    ): List<TimeSlot> {
        val availableSlots = getAvailableSlots(date, durationMinutes)
        
        return if (preferredStartTime != null) {
            // Sort by proximity to preferred time
            availableSlots
                .filter { it.durationMinutes() >= durationMinutes }
                .sortedBy { 
                    kotlin.math.abs(
                        java.time.Duration.between(
                            preferredStartTime,
                            it.start.toLocalTime()
                        ).toMinutes()
                    )
                }
                .take(count)
        } else {
            // Return first available slots
            availableSlots
                .filter { it.durationMinutes() >= durationMinutes }
                .take(count)
        }
    }
}
