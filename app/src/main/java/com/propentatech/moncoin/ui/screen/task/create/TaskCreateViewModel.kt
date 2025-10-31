package com.propentatech.moncoin.ui.screen.task.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.alarm.AlarmScheduler
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.*
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import com.propentatech.moncoin.domain.scheduler.SchedulingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class TaskCreateUiState(
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val taskType: TaskType = TaskType.PONCTUELLE,
    val taskMode: TaskMode = TaskMode.PLAGE,
    val durationMinutes: Int = 60,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val selectedDate: LocalDateTime = LocalDateTime.now(),
    val selectedDaysOfWeek: List<DayOfWeek> = emptyList(),
    val reminders: List<Int> = listOf(10), // Minutes before
    val alarmsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val priority: Int = 0,
    val color: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class TaskCreateViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val alarmScheduler: AlarmScheduler,
    private val schedulingService: SchedulingService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskCreateUiState())
    val uiState: StateFlow<TaskCreateUiState> = _uiState.asStateFlow()
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun addTag(tag: String) {
        val currentTags = _uiState.value.tags.toMutableList()
        if (!currentTags.contains(tag) && tag.isNotBlank()) {
            currentTags.add(tag)
            _uiState.value = _uiState.value.copy(tags = currentTags)
        }
    }
    
    fun removeTag(tag: String) {
        val currentTags = _uiState.value.tags.toMutableList()
        currentTags.remove(tag)
        _uiState.value = _uiState.value.copy(tags = currentTags)
    }
    
    fun updateTaskType(type: TaskType) {
        _uiState.value = _uiState.value.copy(taskType = type)
    }
    
    fun updateTaskMode(mode: TaskMode) {
        _uiState.value = _uiState.value.copy(taskMode = mode)
    }
    
    fun updateDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = minutes)
    }
    
    fun updateStartTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(startTime = time)
    }
    
    fun updateEndTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(endTime = time)
    }
    
    fun updateSelectedDate(date: LocalDateTime) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
    
    fun toggleDayOfWeek(day: DayOfWeek) {
        val currentDays = _uiState.value.selectedDaysOfWeek.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(selectedDaysOfWeek = currentDays)
    }
    
    fun addReminder(minutes: Int) {
        val currentReminders = _uiState.value.reminders.toMutableList()
        if (!currentReminders.contains(minutes)) {
            currentReminders.add(minutes)
            currentReminders.sort()
            _uiState.value = _uiState.value.copy(reminders = currentReminders)
        }
    }
    
    fun removeReminder(minutes: Int) {
        val currentReminders = _uiState.value.reminders.toMutableList()
        currentReminders.remove(minutes)
        _uiState.value = _uiState.value.copy(reminders = currentReminders)
    }
    
    fun toggleAlarms(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(alarmsEnabled = enabled)
    }
    
    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }
    
    fun updatePriority(priority: Int) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }
    
    fun updateColor(color: String?) {
        _uiState.value = _uiState.value.copy(color = color)
    }
    
    fun saveTask() {
        val state = _uiState.value
        
        // Validation
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Le titre est obligatoire")
            return
        }
        
        if (state.taskMode == TaskMode.PLAGE && (state.startTime == null || state.endTime == null)) {
            _uiState.value = state.copy(error = "Veuillez définir les horaires de début et fin")
            return
        }
        
        if (state.taskType == TaskType.PERIODIQUE && state.selectedDaysOfWeek.isEmpty()) {
            _uiState.value = state.copy(error = "Veuillez sélectionner au moins un jour de la semaine")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                // Check for conflicts if PLAGE mode
                if (state.taskMode == TaskMode.PLAGE && state.startTime != null && state.endTime != null) {
                    val startDateTime = state.selectedDate.toLocalDate().atTime(state.startTime)
                    val endDateTime = state.selectedDate.toLocalDate().atTime(state.endTime)
                    
                    // Check for conflicts with existing tasks
                    val hasConflict = schedulingService.hasConflict(startDateTime, endDateTime)
                    if (hasConflict) {
                        _uiState.value = state.copy(
                            isLoading = false,
                            error = "Conflit détecté avec une autre tâche. Veuillez choisir un autre horaire."
                        )
                        return@launch
                    }
                    
                    // Check for conflicts with sleep schedule
                    val conflictsWithSleep = schedulingService.conflictsWithSleep(startDateTime, endDateTime)
                    if (conflictsWithSleep) {
                        _uiState.value = state.copy(
                            isLoading = false,
                            error = "Cette tâche chevauche votre plage de sommeil."
                        )
                        return@launch
                    }
                }
                
                // Create task entity
                val recurrence = if (state.taskType != TaskType.PONCTUELLE) {
                    Recurrence(
                        daysOfWeek = state.selectedDaysOfWeek,
                        interval = 1
                    )
                } else null
                
                val task = TaskEntity(
                    title = state.title,
                    description = state.description,
                    tags = state.tags,
                    type = state.taskType,
                    recurrence = recurrence,
                    mode = state.taskMode,
                    durationMinutes = if (state.taskMode == TaskMode.DUREE) state.durationMinutes else null,
                    startTime = if (state.taskMode == TaskMode.PLAGE) {
                        state.selectedDate.toLocalDate().atTime(state.startTime!!)
                    } else null,
                    endTime = if (state.taskMode == TaskMode.PLAGE) {
                        state.selectedDate.toLocalDate().atTime(state.endTime!!)
                    } else null,
                    reminders = state.reminders,
                    alarmsEnabled = state.alarmsEnabled,
                    notificationsEnabled = state.notificationsEnabled,
                    priority = state.priority,
                    color = state.color
                )
                
                // Save task
                taskRepository.insertTask(task)
                
                // Create occurrence for non-recurring or first occurrence
                if (state.taskMode == TaskMode.PLAGE) {
                    val occurrence = OccurrenceEntity(
                        taskId = task.id,
                        startAt = task.startTime!!,
                        endAt = task.endTime!!
                    )
                    occurrenceRepository.insertOccurrence(occurrence)
                    
                    // Schedule alarm
                    if (state.alarmsEnabled) {
                        alarmScheduler.scheduleAlarm(occurrence, task.title)
                    }
                    
                    // Schedule reminders
                    if (state.notificationsEnabled) {
                        state.reminders.forEach { minutesBefore ->
                            alarmScheduler.scheduleReminder(occurrence, task.title, minutesBefore)
                        }
                    }
                }
                
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
}
