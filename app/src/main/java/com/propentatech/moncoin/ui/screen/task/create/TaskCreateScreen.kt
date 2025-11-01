package com.propentatech.moncoin.ui.screen.task.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.propentatech.moncoin.data.model.TaskMode
import com.propentatech.moncoin.data.model.TaskType
import com.propentatech.moncoin.ui.components.TimePickerDialog
import com.propentatech.moncoin.util.TimeFormatUtils
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateScreen(
    viewModel: TaskCreateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate back when saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (uiState.taskId != null) "Modifier la tâche" else "Nouvelle tâche") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveTask() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Sauvegarder")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            item {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Titre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // Description
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description (Optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
            
            // Task Type
            item {
                var showTaskTypeInfoDialog by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Type de tâche",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { showTaskTypeInfoDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Information sur les types de tâches",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (showTaskTypeInfoDialog) {
                    TaskTypeInfoDialog(
                        onDismiss = { showTaskTypeInfoDialog = false }
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskTypeChip(
                        label = "Ponctuelle",
                        selected = uiState.taskType == TaskType.PONCTUELLE,
                        onClick = { viewModel.updateTaskType(TaskType.PONCTUELLE) },
                        modifier = Modifier.weight(1f)
                    )
                    TaskTypeChip(
                        label = "Quotidienne",
                        selected = uiState.taskType == TaskType.QUOTIDIENNE,
                        onClick = { viewModel.updateTaskType(TaskType.QUOTIDIENNE) },
                        modifier = Modifier.weight(1f)
                    )
                    TaskTypeChip(
                        label = "Périodique",
                        selected = uiState.taskType == TaskType.PERIODIQUE,
                        onClick = { viewModel.updateTaskType(TaskType.PERIODIQUE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Date selection (for PONCTUELLE)
            if (uiState.taskType == TaskType.PONCTUELLE) {
                item {
                    DatePickerSection(
                        selectedDate = uiState.selectedDate,
                        onDateChange = { viewModel.updateSelectedDate(it) }
                    )
                }
            }
            
            // Days of week (for PERIODIQUE)
            if (uiState.taskType == TaskType.PERIODIQUE) {
                item {
                    Text(
                        text = "Jours de la semaine",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DayOfWeek.values().toList()) { day ->
                            FilterChip(
                                selected = uiState.selectedDaysOfWeek.contains(day),
                                onClick = { viewModel.toggleDayOfWeek(day) },
                                label = {
                                    Text(
                                        day.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                                            .uppercase()
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Task Mode
            item {
                Text(
                    text = "Mode de programmation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskTypeChip(
                        label = "Durée fixe",
                        selected = uiState.taskMode == TaskMode.DUREE,
                        onClick = { viewModel.updateTaskMode(TaskMode.DUREE) },
                        modifier = Modifier.weight(1f)
                    )
                    TaskTypeChip(
                        label = "Plage horaire",
                        selected = uiState.taskMode == TaskMode.PLAGE,
                        onClick = { viewModel.updateTaskMode(TaskMode.PLAGE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Duration or Time Range
            if (uiState.taskMode == TaskMode.DUREE) {
                item {
                    DurationPicker(
                        durationMinutes = uiState.durationMinutes,
                        onDurationChange = { viewModel.updateDuration(it) }
                    )
                }
            } else {
                item {
                    TimeRangePicker(
                        startTime = uiState.startTime,
                        endTime = uiState.endTime,
                        onStartTimeChange = { viewModel.updateStartTime(it) },
                        onEndTimeChange = { viewModel.updateEndTime(it) }
                    )
                }
            }
            
            // Reminders
            item {
                Text(
                    text = "Rappels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                ReminderSection(
                    reminders = uiState.reminders,
                    onAddReminder = { viewModel.addReminder(it) },
                    onRemoveReminder = { viewModel.removeReminder(it) }
                )
            }
            
            // Options
            item {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activer les alarmes")
                    Switch(
                        checked = uiState.alarmsEnabled,
                        onCheckedChange = { viewModel.toggleAlarms(it) }
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activer les notifications")
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            }
            
            // Priority
            item {
                PrioritySelector(
                    priority = uiState.priority,
                    onPriorityChange = { viewModel.updatePriority(it) }
                )
            }
        }
    }
}

@Composable
fun TaskTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier
    )
}

@Composable
fun DurationPicker(
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit
) {
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Durée",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heures
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Heures",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = hours.toString(),
                        onValueChange = { newValue ->
                            val newHours = newValue.toIntOrNull()?.coerceIn(0, 8) ?: 0
                            onDurationChange(newHours * 60 + minutes)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    // Boutons + et -
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                if (hours > 0) {
                                    onDurationChange((hours - 1) * 60 + minutes)
                                }
                            },
                            enabled = hours > 0
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuer heures")
                        }
                        IconButton(
                            onClick = { 
                                if (hours < 8) {
                                    onDurationChange((hours + 1) * 60 + minutes)
                                }
                            },
                            enabled = hours < 8
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Augmenter heures")
                        }
                    }
                }
                
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Minutes
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Minutes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = String.format("%02d", minutes),
                        onValueChange = { newValue ->
                            val newMinutes = newValue.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            onDurationChange(hours * 60 + newMinutes)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    // Boutons + et -
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                if (minutes > 0) {
                                    onDurationChange(hours * 60 + minutes - 1)
                                }
                            },
                            enabled = minutes > 0
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuer minutes")
                        }
                        IconButton(
                            onClick = { 
                                if (minutes < 59) {
                                    onDurationChange(hours * 60 + minutes + 1)
                                }
                            },
                            enabled = minutes < 59
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Augmenter minutes")
                        }
                    }
                }
            }
            
            // Affichage de la durée totale
            Text(
                text = "Durée totale : ${TimeFormatUtils.formatDuration(durationMinutes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TimeRangePicker(
    startTime: LocalTime?,
    endTime: LocalTime?,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showStartTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = startTime?.let { String.format("%02d:%02d", it.hour, it.minute) } 
                        ?: "Heure de début"
                )
            }
            
            OutlinedButton(
                onClick = { showEndTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = endTime?.let { String.format("%02d:%02d", it.hour, it.minute) } 
                        ?: "Heure de fin"
                )
            }
        }
    }
    
    if (showStartTimePicker) {
        TimePickerDialog(
            title = "Heure de début",
            onDismiss = { showStartTimePicker = false },
            onConfirm = { onStartTimeChange(it) },
            initialTime = startTime ?: LocalTime.now()
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            title = "Heure de fin",
            onDismiss = { showEndTimePicker = false },
            onConfirm = { onEndTimeChange(it) },
            initialTime = endTime ?: LocalTime.now().plusHours(1)
        )
    }
}

@Composable
fun ReminderSection(
    reminders: List<Int>,
    onAddReminder: (Int) -> Unit,
    onRemoveReminder: (Int) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders) { minutes ->
                    AssistChip(
                        onClick = { onRemoveReminder(minutes) },
                        label = { Text("$minutes min") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Supprimer",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30).forEach { minutes ->
                    if (!reminders.contains(minutes)) {
                        AssistChip(
                            onClick = { onAddReminder(minutes) },
                            label = { Text("$minutes min") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerSection(
    selectedDate: java.time.LocalDateTime,
    onDateChange: (java.time.LocalDateTime) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Date de la tâche",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick date selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = java.time.LocalDateTime.now()
                val tomorrow = today.plusDays(1)
                
                FilterChip(
                    selected = selectedDate.toLocalDate() == today.toLocalDate(),
                    onClick = { onDateChange(today) },
                    label = { Text("Aujourd'hui") },
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    selected = selectedDate.toLocalDate() == tomorrow.toLocalDate(),
                    onClick = { onDateChange(tomorrow) },
                    label = { Text("Demain") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Custom date button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH))
                )
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = java.time.Instant.ofEpochMilli(millis)
                            val newDate = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                            onDateChange(newDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PrioritySelector(
    priority: Int,
    onPriorityChange: (Int) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Priorité",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Basse",
                    1 to "Normale",
                    2 to "Haute"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = priority == value,
                        onClick = { onPriorityChange(value) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskTypeInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Types de tâches",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ponctuelle
                TaskTypeInfoItem(
                    icon = Icons.Default.DateRange,
                    title = "Ponctuelle",
                    description = "Une tâche qui se produit une seule fois à une date précise.",
                    example = "Exemple : Rendez-vous chez le médecin le 15 novembre"
                )
                
                HorizontalDivider()
                
                // Quotidienne
                TaskTypeInfoItem(
                    icon = Icons.Default.Refresh,
                    title = "Quotidienne",
                    description = "Une tâche qui se répète tous les jours.",
                    example = "Exemple : Faire du sport, étudier, lire un livre"
                )
                
                HorizontalDivider()
                
                // Périodique
                TaskTypeInfoItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Périodique",
                    description = "Une tâche qui se répète certains jours de la semaine.",
                    example = "Exemple : Cours de natation le lundi et mercredi, réunion d'équipe le vendredi"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Compris")
            }
        }
    )
}

@Composable
fun TaskTypeInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    example: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
