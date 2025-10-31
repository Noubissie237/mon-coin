package com.propentatech.moncoin.ui.screen.history

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
    val task: TaskEntity
)

data class HistoryUiState(
    val occurrences: List<OccurrenceWithTask> = emptyList(),
    val selectedFilter: TaskState? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val occurrenceRepository: OccurrenceRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val startDate = now.minusMonths(1)
            
            occurrenceRepository.getOccurrencesBetween(startDate, now)
                .collect { occurrences ->
                    val occurrencesWithTasks = occurrences.mapNotNull { occurrence ->
                        val task = taskRepository.getTaskById(occurrence.taskId)
                        task?.let { OccurrenceWithTask(occurrence, it) }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        occurrences = filterOccurrences(occurrencesWithTasks),
                        isLoading = false
                    )
                }
        }
    }
    
    fun setFilter(state: TaskState?) {
        _uiState.value = _uiState.value.copy(selectedFilter = state)
        loadHistory()
    }
    
    private fun filterOccurrences(occurrences: List<OccurrenceWithTask>): List<OccurrenceWithTask> {
        val filter = _uiState.value.selectedFilter
        return if (filter != null) {
            occurrences.filter { it.occurrence.state == filter }
        } else {
            occurrences
        }
    }
}
