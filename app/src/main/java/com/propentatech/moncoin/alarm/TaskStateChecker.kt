package com.propentatech.moncoin.alarm

import android.content.Context
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to check and update task states based on time
 */
@Singleton
class TaskStateChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val occurrenceRepository: OccurrenceRepository,
    private val notificationHelper: NotificationHelper
) {
    
    /**
     * Check all occurrences and update states if needed
     * - SCHEDULED past end time -> MISSED (with notification)
     * - RUNNING past end time -> COMPLETED (alarm should have triggered)
     * - CANCELLED -> No action
     */
    suspend fun checkAndUpdateStates() {
        val now = LocalDateTime.now()
        
        // Get all occurrences that ended in the past
        val pastOccurrences = occurrenceRepository.getOccurrencesBetween(
            LocalDateTime.now().minusDays(1),
            now
        ).first()
        
        pastOccurrences.forEach { occurrence ->
            when {
                // If still SCHEDULED after end time -> MISSED
                occurrence.state == TaskState.SCHEDULED && occurrence.endAt.isBefore(now) -> {
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.MISSED)
                    
                    // Show notification for missed task
                    val task = occurrenceRepository.getOccurrenceById(occurrence.id)
                    task?.let {
                        notificationHelper.showMissedTaskNotification(
                            occurrenceId = occurrence.id,
                            taskTitle = "Tâche manquée" // We'd need to get the actual title
                        )
                    }
                }
                
                // If RUNNING after end time -> COMPLETED (alarm should have handled this)
                occurrence.state == TaskState.RUNNING && occurrence.endAt.isBefore(now) -> {
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.COMPLETED)
                }
            }
        }
    }
    
    /**
     * Check if an occurrence should trigger its end alarm
     * Only trigger if state is RUNNING
     */
    suspend fun shouldTriggerEndAlarm(occurrenceId: String): Boolean {
        val occurrence = occurrenceRepository.getOccurrenceById(occurrenceId)
        return occurrence?.state == TaskState.RUNNING
    }
    
    /**
     * Check if an occurrence should trigger its start alarm
     * Only trigger if state is SCHEDULED
     */
    suspend fun shouldTriggerStartAlarm(occurrenceId: String): Boolean {
        val occurrence = occurrenceRepository.getOccurrenceById(occurrenceId)
        return occurrence?.state == TaskState.SCHEDULED
    }
}
