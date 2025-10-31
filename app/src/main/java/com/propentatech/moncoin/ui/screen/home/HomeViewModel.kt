package com.propentatech.moncoin.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.alarm.AlarmScheduler
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

data class HomeUiState(
    val todayOccurrences: List<OccurrenceWithTask> = emptyList(),
    val runningTasks: List<RunningTaskWithOccurrence> = emptyList(),
    val durationTasks: List<TaskEntity> = emptyList(),  // DUREE mode tasks ready to start
    val scheduledTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
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
                // Enrichir les occurrences avec les titres des tâches
                val occurrencesWithTasks = occurrences.map { occurrence ->
                    val task = allTasks.find { it.id == occurrence.taskId }
                    OccurrenceWithTask(
                        occurrence = occurrence,
                        taskTitle = task?.title ?: "Tâche inconnue"
                    )
                }
                
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
                
                // Récupérer les tâches en mode DUREE qui peuvent être démarrées
                val durationTasks = allTasks.filter { task ->
                    task.mode == com.propentatech.moncoin.data.model.TaskMode.DUREE 
                    && task.state == TaskState.SCHEDULED
                }
                
                HomeUiState(
                    todayOccurrences = occurrencesWithTasks,
                    runningTasks = runningTasks,
                    durationTasks = durationTasks,
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
            }
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.updateTaskState(taskId, TaskState.COMPLETED)
        }
    }
}
