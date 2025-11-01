package com.propentatech.moncoin.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.ui.components.BottomNavigationBar
import com.propentatech.moncoin.ui.components.Screen
import com.propentatech.moncoin.ui.components.TaskTimer
import com.propentatech.moncoin.util.TimeFormatUtils
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTaskCreate: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color(0xFF38B6FF), fontWeight = FontWeight.ExtraBold)) {
                                append("Mon")
                            }
                            append(" ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Coin")
                            }
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToTaskCreate) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle tâche")
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = Screen.HOME,
                onNavigateToHome = { },
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToNotes = onNavigateToNotes,
                onNavigateToStats = onNavigateToStatistics
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards
                item {
                    Text(
                        text = "Aujourd'hui",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Programmées",
                            count = uiState.scheduledTasksCount,
                            icon = Icons.Default.DateRange,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "En cours",
                            count = uiState.runningTasks.size,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Terminées",
                            count = uiState.completedTasksCount,
                            icon = Icons.Default.Check,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // All Today's Tasks (unified section)
                if (uiState.todayOccurrences.isNotEmpty() || uiState.durationTasks.isNotEmpty() || uiState.flexibleTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Tâches du jour",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Duration Tasks (ready to start)
                    items(uiState.durationTasks) { task ->
                        DurationTaskCard(
                            title = task.title,
                            description = task.description,
                            durationMinutes = task.durationMinutes ?: 60,
                            onStart = { viewModel.startTask(task.id) },
                            onClick = { onNavigateToTaskDetail(task.id) }
                        )
                    }
                    
                    // Today's Occurrences
                    items(uiState.todayOccurrences) { occurrenceWithTask ->
                        OccurrenceCard(
                            title = occurrenceWithTask.taskTitle,
                            startTime = occurrenceWithTask.occurrence.startAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                            endTime = occurrenceWithTask.occurrence.endAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                            endDateTime = occurrenceWithTask.occurrence.endAt,
                            state = occurrenceWithTask.occurrence.state,
                            onClick = { onNavigateToTaskDetail(occurrenceWithTask.occurrence.taskId) }
                        )
                    }
                    
                    // Flexible Tasks (to complete anytime)
                    items(uiState.flexibleTasks) { task ->
                        FlexibleTaskCard(
                            title = task.title,
                            description = task.description,
                            isCompleted = task.state == TaskState.COMPLETED,
                            onComplete = { viewModel.completeTask(task.id) },
                            onClick = { onNavigateToTaskDetail(task.id) }
                        )
                    }
                }
                
                // Empty state
                if (uiState.todayOccurrences.isEmpty() && uiState.durationTasks.isEmpty() && uiState.flexibleTasks.isEmpty()) {
                    item {
                        EmptyState()
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OccurrenceCard(
    title: String,
    startTime: String,
    endTime: String,
    endDateTime: java.time.LocalDateTime,
    state: TaskState,
    onClick: () -> Unit
) {
    val isRunning = state == TaskState.RUNNING
    val isCompleted = state == TaskState.COMPLETED
    val isMissed = state == TaskState.MISSED
    val isCancelled = state == TaskState.CANCELLED
    
    val borderColor = when {
        isRunning -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isMissed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        isCancelled -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isRunning) 2.dp else 0.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône d'état à gauche
            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terminée",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                isMissed -> {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Manquée",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                isCancelled -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Annulée",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                isRunning -> {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "En cours",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textDecoration = if (isCompleted || isCancelled) 
                        androidx.compose.ui.text.style.TextDecoration.LineThrough 
                    else 
                        null,
                    color = if (isCompleted || isCancelled) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$startTime - $endTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Timer si la tâche est en cours
            if (isRunning) {
                TaskTimer(
                    endTime = endDateTime,
                    isCompact = true
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Voir détails",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DurationTaskCard(
    title: String,
    description: String,
    durationMinutes: Int,
    onStart: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TimeFormatUtils.formatDuration(durationMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = onStart,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Démarrer", style = MaterialTheme.typography.labelLarge)
            }
            
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Détails", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aucune tâche pour aujourd'hui",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Appuyez sur + pour créer une nouvelle tâche",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FlexibleTaskCard(
    title: String,
    description: String,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted) 
                        androidx.compose.ui.text.style.TextDecoration.LineThrough 
                    else 
                        null,
                    color = if (isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textDecoration = if (isCompleted) 
                            androidx.compose.ui.text.style.TextDecoration.LineThrough 
                        else 
                            null
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isCompleted) 
                            MaterialTheme.colorScheme.tertiary 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isCompleted) "Terminée" else "Flexible",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) 
                            MaterialTheme.colorScheme.tertiary 
                        else 
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Complete button (only show if not completed)
            if (!isCompleted) {
                IconButton(
                    onClick = { onComplete() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Marquer comme terminée",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                // Show completed icon
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Tâche terminée",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
