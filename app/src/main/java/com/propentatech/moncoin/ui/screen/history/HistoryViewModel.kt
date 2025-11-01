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

enum class DateFilter {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}

data class HistoryUiState(
    val occurrences: List<OccurrenceWithTask> = emptyList(),
    val selectedFilter: TaskState? = null,
    val selectedDateFilter: DateFilter = DateFilter.THIS_MONTH,
    val customStartDate: LocalDateTime? = null,
    val customEndDate: LocalDateTime? = null,
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
            val (startDate, endDate) = getDateRange()
            
            occurrenceRepository.getOccurrencesBetween(startDate, endDate)
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
    
    fun setDateFilter(dateFilter: DateFilter) {
        _uiState.value = _uiState.value.copy(selectedDateFilter = dateFilter)
        loadHistory()
    }
    
    fun setCustomDateRange(startDate: LocalDateTime, endDate: LocalDateTime) {
        _uiState.value = _uiState.value.copy(
            selectedDateFilter = DateFilter.CUSTOM,
            customStartDate = startDate,
            customEndDate = endDate
        )
        loadHistory()
    }
    
    private fun getDateRange(): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDateTime.now()
        return when (_uiState.value.selectedDateFilter) {
            DateFilter.TODAY -> {
                val startOfDay = now.toLocalDate().atStartOfDay()
                val endOfDay = now.toLocalDate().atTime(23, 59, 59)
                startOfDay to endOfDay
            }
            DateFilter.THIS_WEEK -> {
                val startOfWeek = now.toLocalDate().minusDays(now.dayOfWeek.value.toLong() - 1).atStartOfDay()
                val endOfWeek = startOfWeek.plusDays(6).toLocalDate().atTime(23, 59, 59)
                startOfWeek to endOfWeek
            }
            DateFilter.THIS_MONTH -> {
                val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
                val endOfMonth = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth()).atTime(23, 59, 59)
                startOfMonth to endOfMonth
            }
            DateFilter.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                val startOfLastMonth = lastMonth.toLocalDate().withDayOfMonth(1).atStartOfDay()
                val endOfLastMonth = lastMonth.toLocalDate().withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth()).atTime(23, 59, 59)
                startOfLastMonth to endOfLastMonth
            }
            DateFilter.CUSTOM -> {
                val start = _uiState.value.customStartDate ?: now.minusMonths(1)
                val end = _uiState.value.customEndDate ?: now
                start to end
            }
        }
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
