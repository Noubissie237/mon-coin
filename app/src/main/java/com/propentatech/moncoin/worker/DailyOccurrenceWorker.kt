package com.propentatech.moncoin.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.propentatech.moncoin.alarm.AlarmScheduler
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Worker qui s'exécute chaque jour à minuit pour :
 * 1. Créer les occurrences du jour pour les tâches QUOTIDIENNES et PERIODIQUES
 * 2. Mettre à jour les statuts des occurrences passées
 * 3. Planifier les alarmes pour les nouvelles occurrences
 */
@HiltWorker
class DailyOccurrenceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // 1. Mettre à jour les statuts des occurrences d'hier
            updateYesterdayOccurrences()
            
            // 2. Créer les occurrences d'aujourd'hui
            createTodayOccurrences()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    /**
     * Met à jour les statuts des occurrences d'hier
     * - SCHEDULED -> MISSED
     * - RUNNING -> COMPLETED
     */
    private suspend fun updateYesterdayOccurrences() {
        val yesterday = LocalDate.now().minusDays(1)
        val startOfYesterday = yesterday.atStartOfDay()
        val endOfYesterday = yesterday.atTime(23, 59, 59)
        
        val yesterdayOccurrences = occurrenceRepository
            .getOccurrencesBetween(startOfYesterday, endOfYesterday)
            .first()
        
        yesterdayOccurrences.forEach { occurrence ->
            when (occurrence.state) {
                TaskState.SCHEDULED -> {
                    // Tâche non démarrée -> MISSED
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.MISSED)
                }
                TaskState.RUNNING -> {
                    // Tâche en cours mais jour terminé -> COMPLETED
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.COMPLETED)
                }
                else -> {
                    // COMPLETED, MISSED, CANCELLED -> ne rien faire
                }
            }
        }
    }
    
    /**
     * Crée les occurrences d'aujourd'hui pour les tâches récurrentes
     */
    private suspend fun createTodayOccurrences() {
        val today = LocalDate.now()
        
        // Récupérer toutes les tâches
        val allTasks = taskRepository.getAllTasks().first()
        
        // Filtrer les tâches récurrentes (QUOTIDIENNE et PERIODIQUE)
        val recurringTasks = allTasks.filter { task ->
            task.type == TaskType.QUOTIDIENNE || task.type == TaskType.PERIODIQUE
        }
        
        recurringTasks.forEach { task ->
            // Vérifier si une occurrence existe déjà pour aujourd'hui
            val startOfToday = today.atStartOfDay()
            val endOfToday = today.atTime(23, 59, 59)
            
            val existingOccurrences = occurrenceRepository
                .getOccurrencesBetween(startOfToday, endOfToday)
                .first()
                .filter { it.taskId == task.id }
            
            // Si aucune occurrence n'existe pour aujourd'hui, en créer une
            if (existingOccurrences.isEmpty()) {
                val shouldCreateToday = when (task.type) {
                    TaskType.QUOTIDIENNE -> true
                    TaskType.PERIODIQUE -> {
                        // Vérifier si aujourd'hui fait partie des jours sélectionnés
                        task.recurrence?.daysOfWeek?.contains(today.dayOfWeek) == true
                    }
                    else -> false
                }
                
                if (shouldCreateToday && task.startTime != null && task.endTime != null) {
                    // Créer l'occurrence
                    val occurrence = OccurrenceEntity(
                        taskId = task.id,
                        startAt = today.atTime(task.startTime.toLocalTime()),
                        endAt = today.atTime(task.endTime.toLocalTime()),
                        state = TaskState.SCHEDULED
                    )
                    
                    occurrenceRepository.insertOccurrence(occurrence)
                    
                    // Planifier les alarmes
                    if (task.alarmsEnabled) {
                        // Alarme de début
                        alarmScheduler.scheduleStartAlarm(occurrence, task.title)
                        
                        // Alarme de fin
                        alarmScheduler.scheduleAlarm(occurrence, task.title)
                        
                        // Rappels
                        if (task.notificationsEnabled) {
                            task.reminders.forEach { minutesBefore ->
                                alarmScheduler.scheduleReminder(occurrence, task.title, minutesBefore)
                            }
                        }
                    }
                }
            }
        }
    }
}
