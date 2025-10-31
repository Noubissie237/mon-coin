package com.propentatech.moncoin.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class StatisticsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val missedTasks: Int = 0,
    val completionRate: Float = 0f,
    val totalTimeMinutes: Long = 0,
    val averageTimePerTask: Long = 0,
    val isLoading: Boolean = true,
    val period: StatisticsPeriod = StatisticsPeriod.WEEK
)

enum class StatisticsPeriod {
    WEEK, MONTH, ALL
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val occurrenceRepository: OccurrenceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    fun setPeriod(period: StatisticsPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val startDate = when (_uiState.value.period) {
                StatisticsPeriod.WEEK -> now.minusWeeks(1)
                StatisticsPeriod.MONTH -> now.minusMonths(1)
                StatisticsPeriod.ALL -> now.minusYears(1)
            }
            
            occurrenceRepository.getOccurrencesBetween(startDate, now)
                .collect { occurrences ->
                    val completed = occurrences.count { it.state == TaskState.COMPLETED }
                    val missed = occurrences.count { it.state == TaskState.MISSED }
                    val total = occurrences.size
                    
                    val completionRate = if (total > 0) {
                        (completed.toFloat() / total.toFloat()) * 100
                    } else 0f
                    
                    // Calculate total time for completed tasks
                    val totalMinutes = occurrences
                        .filter { it.state == TaskState.COMPLETED }
                        .sumOf { ChronoUnit.MINUTES.between(it.startAt, it.endAt) }
                    
                    val avgTime = if (completed > 0) totalMinutes / completed else 0
                    
                    _uiState.value = StatisticsUiState(
                        totalTasks = total,
                        completedTasks = completed,
                        missedTasks = missed,
                        completionRate = completionRate,
                        totalTimeMinutes = totalMinutes,
                        averageTimePerTask = avgTime,
                        isLoading = false,
                        period = _uiState.value.period
                    )
                }
        }
    }
}
