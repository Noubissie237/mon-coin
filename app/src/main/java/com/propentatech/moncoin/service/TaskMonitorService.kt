package com.propentatech.moncoin.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.propentatech.moncoin.alarm.NotificationHelper
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Service qui surveille les tâches en cours et les termine automatiquement
 * quand le temps est écoulé
 */
@AndroidEntryPoint
class TaskMonitorService : Service() {
    
    @Inject
    lateinit var occurrenceRepository: OccurrenceRepository
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "TaskMonitorService started")
        startMonitoring()
        return START_STICKY
    }
    
    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkRunningTasks()
                    delay(1000) // Vérifier chaque seconde pour une réactivité immédiate
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking running tasks", e)
                }
            }
        }
    }
    
    private suspend fun checkRunningTasks() {
        val now = LocalDateTime.now()
        
        // Récupérer toutes les occurrences en cours
        val today = now.toLocalDate()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.atTime(23, 59, 59)
        
        val occurrences = occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay).first()
        
        occurrences.forEach { occurrence ->
            when {
                // Tâche en cours dont le temps est écoulé
                occurrence.state == TaskState.RUNNING && occurrence.endAt.isBefore(now) -> {
                    Log.d(TAG, "Task ${occurrence.id} time elapsed, marking as COMPLETED")
                    
                    // Récupérer le titre de la tâche
                    val task = taskRepository.getTaskById(occurrence.taskId)
                    val taskTitle = task?.title ?: "Tâche"
                    
                    // Marquer automatiquement la tâche comme terminée
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.COMPLETED)
                    
                    // Mettre à jour l'état de la tâche principale si nécessaire
                    if (task != null) {
                        taskRepository.updateTaskState(task.id, TaskState.COMPLETED)
                    }
                    
                    // Afficher la notification de succès
                    notificationHelper.showTaskCompletedNotification(
                        occurrenceId = occurrence.id,
                        taskId = occurrence.taskId,
                        taskTitle = taskTitle
                    )
                    
                    Log.d(TAG, "Task completed automatically: $taskTitle")
                }
                
                // Tâche programmée dont le temps de début est dépassé
                occurrence.state == TaskState.SCHEDULED && occurrence.endAt.isBefore(now) -> {
                    Log.d(TAG, "Task ${occurrence.id} was not started, marking as MISSED")
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.MISSED)
                    
                    val task = taskRepository.getTaskById(occurrence.taskId)
                    val taskTitle = task?.title ?: "Tâche"
                    notificationHelper.showMissedTaskNotification(occurrence.id, taskTitle)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TaskMonitorService destroyed")
        monitorJob?.cancel()
        serviceScope.cancel()
    }
    
    companion object {
        private const val TAG = "TaskMonitorService"
    }
}
