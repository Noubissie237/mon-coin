package com.propentatech.moncoin.ui.screen.task.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
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

data class TaskDetailUiState(
    val task: TaskEntity? = null,
    val occurrences: List<OccurrenceEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadTaskDetails()
    }
    
    private fun loadTaskDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Observer les changements de la tâche en temps réel
            combine(
                taskRepository.getTaskByIdFlow(taskId),
                occurrenceRepository.getOccurrencesBetween(
                    LocalDateTime.now().minusMonths(1),
                    LocalDateTime.now().plusMonths(1)
                )
            ) { task, allOccurrences ->
                if (task != null) {
                    val taskOccurrences = allOccurrences.filter { it.taskId == taskId }
                    TaskDetailUiState(
                        task = task,
                        occurrences = taskOccurrences.sortedBy { it.startAt },
                        isLoading = false
                    )
                } else {
                    TaskDetailUiState(
                        isLoading = false,
                        error = "Tâche introuvable"
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    /**
     * Recharge manuellement les détails de la tâche
     * Utile après une modification
     */
    fun refreshTaskDetails() {
        loadTaskDetails()
    }
    
    fun deleteTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            
            // Cancel all alarms for this task
            _uiState.value.occurrences.forEach { occurrence ->
                alarmScheduler.cancelAlarm(occurrence.id)
            }
            
            // Delete all occurrences
            _uiState.value.occurrences.forEach { occurrence ->
                occurrenceRepository.deleteOccurrence(occurrence)
            }
            
            // Delete the task
            taskRepository.deleteTask(task)
            
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
    
    fun completeOccurrence(occurrenceId: String) {
        viewModelScope.launch {
            occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.COMPLETED)
        }
    }
    
    fun cancelOccurrence(occurrenceId: String) {
        viewModelScope.launch {
            occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.CANCELLED)
            alarmScheduler.cancelAlarm(occurrenceId)
        }
    }
    
    fun startTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            
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
    
    fun completeFlexibleTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            
            // Only for FLEXIBLE mode tasks
            if (task.mode == com.propentatech.moncoin.data.model.TaskMode.FLEXIBLE) {
                // Update task state to completed
                // The UI will update automatically thanks to the Flow in loadTaskDetails()
                taskRepository.updateTaskState(task.id, TaskState.COMPLETED)
            }
        }
    }
    
    fun completeDurationTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            
            // Only for DUREE mode tasks
            if (task.mode == com.propentatech.moncoin.data.model.TaskMode.DUREE) {
                // Récupérer l'occurrence en cours (RUNNING) pour cette tâche
                val today = LocalDateTime.now()
                val startOfDay = today.toLocalDate().atStartOfDay()
                val endOfDay = today.toLocalDate().atTime(23, 59, 59)
                
                // Obtenir toutes les occurrences du jour pour cette tâche
                val occurrences = occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay)
                    .first() // Prendre la première émission du Flow
                    .filter { it.taskId == task.id && it.state == TaskState.RUNNING }
                
                // Mettre à jour l'occurrence active si elle existe
                occurrences.firstOrNull()?.let { occurrence ->
                    occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.COMPLETED)
                }
                
                // Mettre à jour l'état de la tâche
                taskRepository.updateTaskState(task.id, TaskState.COMPLETED)
            }
        }
    }
    
    /**
     * Repasser une tâche flexible de COMPLETED à SCHEDULED
     * Utile pour annuler une complétion accidentelle
     */
    fun uncompleteFlexibleTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            
            // Only for FLEXIBLE mode tasks
            if (task.mode == com.propentatech.moncoin.data.model.TaskMode.FLEXIBLE) {
                taskRepository.updateTaskState(task.id, TaskState.SCHEDULED)
            }
        }
    }
    
    /**
     * Repasser une occurrence de COMPLETED à SCHEDULED
     * Utile pour annuler une complétion accidentelle
     */
    fun uncompleteOccurrence(occurrenceId: String) {
        viewModelScope.launch {
            occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.SCHEDULED)
        }
    }
}
