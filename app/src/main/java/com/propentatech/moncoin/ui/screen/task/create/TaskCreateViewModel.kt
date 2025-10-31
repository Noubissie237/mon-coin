package com.propentatech.moncoin.ui.screen.task.create

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class TaskCreateUiState(
    val taskId: String? = null, // null = création, non-null = édition
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
    private val schedulingService: SchedulingService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: String? = savedStateHandle["taskId"]
    
    private val _uiState = MutableStateFlow(TaskCreateUiState())
    val uiState: StateFlow<TaskCreateUiState> = _uiState.asStateFlow()
    
    init {
        // Si taskId est fourni, charger la tâche pour édition
        taskId?.let { loadTask(it) }
    }
    
    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = TaskCreateUiState(
                        taskId = task.id,
                        title = task.title,
                        description = task.description,
                        tags = task.tags,
                        taskType = task.type,
                        taskMode = task.mode,
                        durationMinutes = task.durationMinutes ?: 60,
                        startTime = task.startTime?.toLocalTime(),
                        endTime = task.endTime?.toLocalTime(),
                        selectedDate = task.startTime ?: LocalDateTime.now(),
                        selectedDaysOfWeek = task.recurrence?.daysOfWeek ?: emptyList(),
                        reminders = task.reminders,
                        alarmsEnabled = task.alarmsEnabled,
                        notificationsEnabled = task.notificationsEnabled,
                        priority = task.priority,
                        color = task.color
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors du chargement de la tâche: ${e.message}"
                )
            }
        }
    }
    
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
                
                // Create or update task entity
                val recurrence = if (state.taskType != TaskType.PONCTUELLE) {
                    Recurrence(
                        daysOfWeek = state.selectedDaysOfWeek,
                        interval = 1
                    )
                } else null
                
                val task = if (state.taskId != null) {
                    // Mode édition : récupérer la tâche existante et la mettre à jour
                    val existingTask = taskRepository.getTaskById(state.taskId)
                    existingTask?.copy(
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
                    ) ?: return@launch
                } else {
                    // Mode création : créer une nouvelle tâche
                    TaskEntity(
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
                }
                
                // Save or update task
                taskRepository.insertTask(task)
                
                // Create occurrences based on task type
                if (state.taskMode == TaskMode.PLAGE) {
                    if (state.taskType == TaskType.PONCTUELLE) {
                        // PONCTUELLE : une seule occurrence
                        val occurrence = OccurrenceEntity(
                            taskId = task.id,
                            startAt = task.startTime!!,
                            endAt = task.endTime!!
                        )
                        occurrenceRepository.insertOccurrence(occurrence)
                    } else {
                        // QUOTIDIENNE ou PERIODIQUE : générer les occurrences pour aujourd'hui uniquement
                        // Les occurrences futures seront créées automatiquement chaque jour à minuit
                        val today = LocalDate.now()
                        val shouldCreateToday = when (state.taskType) {
                            TaskType.QUOTIDIENNE -> true
                            TaskType.PERIODIQUE -> state.selectedDaysOfWeek.contains(today.dayOfWeek)
                            else -> false
                        }
                        
                        if (shouldCreateToday) {
                            val occurrence = OccurrenceEntity(
                                taskId = task.id,
                                startAt = today.atTime(state.startTime!!),
                                endAt = today.atTime(state.endTime!!),
                                state = TaskState.SCHEDULED
                            )
                            occurrenceRepository.insertOccurrence(occurrence)
                        }
                    }
                    
                    // Planifier les alarmes pour l'occurrence créée
                    if (state.taskMode == TaskMode.PLAGE) {
                        val createdOccurrences = occurrenceRepository.getOccurrencesByTaskId(task.id).first()
                        createdOccurrences.forEach { occurrence ->
                            // Schedule start alarm (when task should begin)
                            alarmScheduler.scheduleStartAlarm(occurrence, task.title)
                            
                            // Schedule end alarm (when task finishes)
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
                    }
                }
                // Note: DUREE mode tasks don't create occurrence at creation time
                // They will be started manually by the user, and occurrence will be created then
                
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
