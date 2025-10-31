package com.propentatech.moncoin.ui.screen.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.data.local.entity.SleepScheduleEntity
import com.propentatech.moncoin.data.repository.SleepScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

data class SleepScheduleUiState(
    val sleepSchedule: SleepScheduleEntity? = null,
    val startTime: LocalTime = LocalTime.of(22, 0),
    val endTime: LocalTime = LocalTime.of(6, 0),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SleepScheduleViewModel @Inject constructor(
    private val sleepScheduleRepository: SleepScheduleRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SleepScheduleUiState())
    val uiState: StateFlow<SleepScheduleUiState> = _uiState.asStateFlow()
    
    init {
        loadSleepSchedule()
    }
    
    private fun loadSleepSchedule() {
        viewModelScope.launch {
            sleepScheduleRepository.getSleepSchedule()
                .collect { schedule ->
                    if (schedule != null) {
                        _uiState.value = SleepScheduleUiState(
                            sleepSchedule = schedule,
                            startTime = schedule.getStartLocalTime(),
                            endTime = schedule.getEndLocalTime(),
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
        }
    }
    
    fun updateStartTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(startTime = time)
    }
    
    fun updateEndTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(endTime = time)
    }
    
    fun saveSleepSchedule() {
        val state = _uiState.value
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val schedule = SleepScheduleEntity(
                    id = 1, // Always use ID 1 for single row
                    startTime = state.startTime.toString(),
                    endTime = state.endTime.toString()
                )
                
                // Use insert with REPLACE strategy instead of update
                sleepScheduleRepository.insertSleepSchedule(schedule)
                
                _uiState.value = state.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Erreur lors de la sauvegarde: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetSaved() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
