package com.propentatech.moncoin.domain.scheduler

import com.propentatech.moncoin.alarm.NotificationHelper
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to check and mark missed tasks
 */
@Singleton
class MissedTaskChecker @Inject constructor(
    private val occurrenceRepository: OccurrenceRepository,
    private val taskRepository: TaskRepository,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Check for missed tasks and update their state
     */
    fun checkMissedTasks() {
        scope.launch {
            val now = LocalDateTime.now()
            val missedOccurrences = occurrenceRepository.getMissedOccurrences(now)
            
            missedOccurrences.forEach { occurrence ->
                // Update occurrence state to MISSED
                occurrenceRepository.updateOccurrenceState(
                    occurrence.id,
                    TaskState.MISSED
                )
                
                // Get task details for notification
                val task = taskRepository.getTaskById(occurrence.taskId)
                task?.let {
                    // Send notification about missed task
                    notificationHelper.showMissedTaskNotification(
                        occurrence.id,
                        it.title
                    )
                }
            }
        }
    }
    
    /**
     * Schedule periodic checks for missed tasks
     * This should be called from a WorkManager periodic task
     */
    fun schedulePeriodicCheck() {
        // TODO: Implement with WorkManager
        // For now, this is a placeholder
    }
}
