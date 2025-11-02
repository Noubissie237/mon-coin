package com.propentatech.moncoin.ui.screen.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.alarm.AlarmScheduler
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import com.propentatech.moncoin.service.TaskMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class OccurrenceWithTask(
    val occurrence: OccurrenceEntity,
    val taskTitle: String
)

data class RunningTaskWithOccurrence(
    val task: TaskEntity,
    val occurrence: OccurrenceEntity
)

// Structure unifiée pour toutes les tâches
sealed class UnifiedTask {
    abstract val isCompleted: Boolean
    abstract val sortPriority: Int // Pour trier : 0 = en cours, 1 = programmée, 2 = terminée
    
    data class OccurrenceTask(
        val occurrence: OccurrenceEntity,
        val taskTitle: String
    ) : UnifiedTask() {
        override val isCompleted: Boolean = occurrence.state == TaskState.COMPLETED
        override val sortPriority: Int = when (occurrence.state) {
            TaskState.RUNNING -> 0
            TaskState.SCHEDULED -> 1
            TaskState.COMPLETED -> 2
            TaskState.MISSED, TaskState.CANCELLED -> 2
            else -> 1
        }
    }
    
    data class DurationTask(
        val task: TaskEntity
    ) : UnifiedTask() {
        override val isCompleted: Boolean = task.state == TaskState.COMPLETED
        override val sortPriority: Int = when (task.state) {
            TaskState.RUNNING -> 0
            TaskState.SCHEDULED -> 1
            TaskState.COMPLETED -> 2
            else -> 1
        }
    }
    
    data class FlexibleTask(
        val task: TaskEntity
    ) : UnifiedTask() {
        override val isCompleted: Boolean = task.state == TaskState.COMPLETED
        override val sortPriority: Int = if (task.state == TaskState.COMPLETED) 2 else 1
    }
}

data class DaySummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

data class HomeUiState(
    val allTasks: List<UnifiedTask> = emptyList(),  // Toutes les tâches unifiées et triées
    val runningTasks: List<RunningTaskWithOccurrence> = emptyList(),
    val daySummary: DaySummary = DaySummary(),
    val dailyMotivation: String = "",
    val scheduledTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            val today = LocalDateTime.now()
            val startOfDay = today.toLocalDate().atStartOfDay()
            val endOfDay = today.toLocalDate().atTime(23, 59, 59)
            
            // Combiner les flows pour mettre à jour l'UI
            combine(
                occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay),
                taskRepository.getAllTasks()
            ) { occurrences, allTasks ->
                // Créer une liste unifiée de toutes les tâches
                val unifiedTasks = mutableListOf<UnifiedTask>()
                
                // 1. Ajouter les occurrences (tâches PLAGE)
                occurrences.forEach { occurrence ->
                    val task = allTasks.find { it.id == occurrence.taskId }
                    unifiedTasks.add(
                        UnifiedTask.OccurrenceTask(
                            occurrence = occurrence,
                            taskTitle = task?.title ?: "Tâche inconnue"
                        )
                    )
                }
                
                // 2. Ajouter les tâches DUREE
                allTasks.filter { task ->
                    task.mode == com.propentatech.moncoin.data.model.TaskMode.DUREE
                }.forEach { task ->
                    unifiedTasks.add(UnifiedTask.DurationTask(task))
                }
                
                // 3. Ajouter les tâches FLEXIBLE
                allTasks.filter { task ->
                    task.mode == com.propentatech.moncoin.data.model.TaskMode.FLEXIBLE
                }.forEach { task ->
                    unifiedTasks.add(UnifiedTask.FlexibleTask(task))
                }
                
                // Trier toutes les tâches ensemble :
                // 0 = en cours (RUNNING) en premier
                // 1 = programmées (SCHEDULED) au milieu
                // 2 = terminées (COMPLETED/MISSED/CANCELLED) en dernier
                val sortedTasks = unifiedTasks.sortedBy { it.sortPriority }
                
                // Compter les occurrences par état
                val scheduledCount = occurrences.count { it.state == TaskState.SCHEDULED }
                val runningCount = occurrences.count { it.state == TaskState.RUNNING }
                val completedCount = occurrences.count { it.state == TaskState.COMPLETED }
                
                // Récupérer les tâches en cours pour affichage avec leurs occurrences
                val runningOccurrences = occurrences.filter { it.state == TaskState.RUNNING }
                val runningTasks = runningOccurrences.mapNotNull { occurrence ->
                    val task = allTasks.find { it.id == occurrence.taskId }
                    task?.let { RunningTaskWithOccurrence(it, occurrence) }
                }
                
                // Calculer le résumé du jour
                val totalTasks = unifiedTasks.size
                val completedTasksToday = unifiedTasks.count { it.isCompleted }
                
                val daySummary = DaySummary(
                    totalTasks = totalTasks,
                    completedTasks = completedTasksToday
                )
                
                // Obtenir la motivation du jour
                val dayOfYear = today.dayOfYear
                val motivation = com.propentatech.moncoin.data.DailyMotivations.getMotivationOfTheDay(dayOfYear)
                
                HomeUiState(
                    allTasks = sortedTasks,
                    runningTasks = runningTasks,
                    daySummary = daySummary,
                    dailyMotivation = motivation,
                    scheduledTasksCount = scheduledCount,
                    completedTasksCount = completedCount,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun startTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            
            // Only for DUREE mode tasks
            if (task.mode == com.propentatech.moncoin.data.model.TaskMode.DUREE) {
                val durationMinutes = task.durationMinutes ?: 60
                val startAt = LocalDateTime.now()
                val endAt = startAt.plusMinutes(durationMinutes.toLong())
                
                // Create occurrence
                val occurrence = OccurrenceEntity(
                    taskId = task.id,
                    startAt = startAt,
                    endAt = endAt,
                    state = TaskState.RUNNING
                )
                occurrenceRepository.insertOccurrence(occurrence)
                
                // Schedule end alarm (when task finishes)
                if (task.alarmsEnabled) {
                    alarmScheduler.scheduleAlarm(occurrence, task.title)
                }
                
                // Update task state
                taskRepository.updateTaskState(task.id, TaskState.RUNNING)
                
                // Start monitoring service to check task completion
                val serviceIntent = Intent(context, TaskMonitorService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            
            // Pour les tâches DUREE, il faut aussi mettre à jour l'occurrence active
            if (task.mode == com.propentatech.moncoin.data.model.TaskMode.DUREE) {
                // Récupérer l'occurrence en cours (RUNNING) pour cette tâche
                val today = LocalDateTime.now()
                val startOfDay = today.toLocalDate().atStartOfDay()
                val endOfDay = today.toLocalDate().atTime(23, 59, 59)
                
                // Obtenir toutes les occurrences du jour pour cette tâche
                val occurrences = occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay)
                    .first() // Prendre la première émission du Flow
                    .filter { it.taskId == taskId && it.state == TaskState.RUNNING }
                
                // Mettre à jour l'occurrence active si elle existe
                occurrences.firstOrNull()?.let { occurrence ->
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.COMPLETED)
                }
            }
            
            // Mettre à jour l'état de la tâche
            taskRepository.updateTaskState(taskId, TaskState.COMPLETED)
        }
    }
    
    /**
     * Marquer manuellement une occurrence comme terminée
     * Utile quand une tâche a été manquée mais faite plus tard,
     * ou quand l'utilisateur veut marquer comme terminée sans attendre l'alarme
     */
    fun completeOccurrence(occurrenceId: String) {
        viewModelScope.launch {
            // Mettre à jour l'état de l'occurrence
            occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.COMPLETED)
            
            // Récupérer l'occurrence pour obtenir le taskId
            val occurrence = occurrenceRepository.getOccurrenceById(occurrenceId)
            if (occurrence != null) {
                // Mettre à jour l'état de la tâche principale
                taskRepository.updateTaskState(occurrence.taskId, TaskState.COMPLETED)
            }
        }
    }
}
