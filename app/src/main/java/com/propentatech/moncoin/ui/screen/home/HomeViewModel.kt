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

data class OccurrenceWithTask(
    val occurrence: OccurrenceEntity,
    val taskTitle: String
)

data class HomeUiState(
    val todayOccurrences: List<OccurrenceWithTask> = emptyList(),
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
            
            occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay).collect { occurrences ->
                // Enrichir les occurrences avec les titres des tâches
                val occurrencesWithTasks = occurrences.map { occurrence ->
                    val task = taskRepository.getTaskById(occurrence.taskId)
                    OccurrenceWithTask(
                        occurrence = occurrence,
                        taskTitle = task?.title ?: "Tâche inconnue"
                    )
                }
                
                // Compter les occurrences par état
                val scheduledCount = occurrences.count { it.state == TaskState.SCHEDULED }
                val runningCount = occurrences.count { it.state == TaskState.RUNNING }
                val completedCount = occurrences.count { it.state == TaskState.COMPLETED }
                
                // Récupérer les tâches en cours pour affichage
                val runningOccurrences = occurrences.filter { it.state == TaskState.RUNNING }
                val runningTasks = runningOccurrences.mapNotNull { occurrence ->
                    taskRepository.getTaskById(occurrence.taskId)
                }
                
                _uiState.value = HomeUiState(
                    todayOccurrences = occurrencesWithTasks,
                    runningTasks = runningTasks,
                    scheduledTasksCount = scheduledCount,
                    completedTasksCount = completedCount,
                    isLoading = false
                )
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
