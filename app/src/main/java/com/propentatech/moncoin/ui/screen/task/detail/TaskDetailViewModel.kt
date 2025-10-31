package com.propentatech.moncoin.ui.screen.task.detail

import androidx.lifecycle.SavedStateHandle
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

data class TaskDetailUiState(
    val task: TaskEntity? = null,
    val occurrences: List<OccurrenceEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
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
            
            val task = taskRepository.getTaskById(taskId)
            if (task != null) {
                // Load occurrences for this task
                val now = LocalDateTime.now()
                occurrenceRepository.getOccurrencesBetween(
                    now.minusMonths(1),
                    now.plusMonths(1)
                ).collect { allOccurrences ->
                    val taskOccurrences = allOccurrences.filter { it.taskId == taskId }
                    
                    _uiState.value = TaskDetailUiState(
                        task = task,
                        occurrences = taskOccurrences.sortedBy { it.startAt },
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Tâche introuvable"
                )
            }
        }
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
            }
        }
    }
}
