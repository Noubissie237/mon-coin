package com.propentatech.moncoin.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class HomeUiState(
    val todayOccurrences: List<OccurrenceEntity> = emptyList(),
    val runningTasks: List<TaskEntity> = emptyList(),
    val scheduledTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository
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
            
            combine(
                occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay),
                taskRepository.getTasksByState(TaskState.RUNNING),
                taskRepository.getTaskCountByState(TaskState.SCHEDULED),
                taskRepository.getTaskCountByState(TaskState.COMPLETED)
            ) { occurrences, runningTasks, scheduledCount, completedCount ->
                HomeUiState(
                    todayOccurrences = occurrences,
                    runningTasks = runningTasks,
                    scheduledTasksCount = scheduledCount,
                    completedTasksCount = completedCount,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun startTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.updateTaskState(taskId, TaskState.RUNNING)
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.updateTaskState(taskId, TaskState.COMPLETED)
        }
    }
}
