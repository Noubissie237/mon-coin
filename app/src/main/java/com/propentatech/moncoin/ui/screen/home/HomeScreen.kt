package com.propentatech.moncoin.ui.screen.home

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.propentatech.moncoin.R.drawable.logo_move),
                            contentDescription = "Logo Move",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "ove",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTaskCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    hoveredElevation = 10.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nouvelle tâche",
                    modifier = Modifier.size(28.dp)
                )
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
                // Motivation and Summary
                item(key = "motivation") {
                    MotivationAndSummaryCard(
                        motivation = uiState.dailyMotivation,
                        daySummary = uiState.daySummary
                    )
                }
                
                // All Today's Tasks (unified section)
                if (uiState.allTasks.isNotEmpty()) {
                    item(key = "title") {
                        Text(
                            text = "Tâches du jour",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Afficher toutes les tâches unifiées et triées
                    items(uiState.allTasks, key = { task ->
                        when (task) {
                            is UnifiedTask.OccurrenceTask -> "occ_${task.occurrence.id}"
                            is UnifiedTask.DurationTask -> "dur_${task.task.id}"
                            is UnifiedTask.FlexibleTask -> "flex_${task.task.id}"
                        }
                    }) { unifiedTask ->
                        when (unifiedTask) {
                            is UnifiedTask.OccurrenceTask -> {
                                OccurrenceCard(
                                    title = unifiedTask.taskTitle,
                                    startTime = unifiedTask.occurrence.startAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    endTime = unifiedTask.occurrence.endAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    endDateTime = unifiedTask.occurrence.endAt,
                                    state = unifiedTask.occurrence.state,
                                    onComplete = { viewModel.completeOccurrence(unifiedTask.occurrence.id) },
                                    onUncomplete = { viewModel.uncompleteOccurrence(unifiedTask.occurrence.id) },
                                    onClick = { onNavigateToTaskDetail(unifiedTask.occurrence.taskId) }
                                )
                            }
                            is UnifiedTask.DurationTask -> {
                                DurationTaskCard(
                                    title = unifiedTask.task.title,
                                    description = unifiedTask.task.description,
                                    durationMinutes = unifiedTask.task.durationMinutes ?: 60,
                                    state = unifiedTask.task.state,
                                    onStart = { viewModel.startTask(unifiedTask.task.id) },
                                    onComplete = { viewModel.completeTask(unifiedTask.task.id) },
                                    onUncomplete = { viewModel.uncompleteTask(unifiedTask.task.id) },
                                    onClick = { onNavigateToTaskDetail(unifiedTask.task.id) }
                                )
                            }
                            is UnifiedTask.FlexibleTask -> {
                                FlexibleTaskCard(
                                    title = unifiedTask.task.title,
                                    description = unifiedTask.task.description,
                                    isCompleted = unifiedTask.task.state == TaskState.COMPLETED,
                                    onComplete = { viewModel.completeTask(unifiedTask.task.id) },
                                    onUncomplete = { viewModel.uncompleteTask(unifiedTask.task.id) },
                                    onClick = { onNavigateToTaskDetail(unifiedTask.task.id) }
                                )
                            }
                        }
                    }
                }
                
                // Empty state
                if (uiState.allTasks.isEmpty()) {
                    item {
                        EmptyState()
                    }
                }
            }
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
    onComplete: () -> Unit,
    onUncomplete: () -> Unit = {},
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
        isCompleted || isMissed || isCancelled -> MaterialTheme.colorScheme.surfaceVariant
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
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRunning) 4.dp else 2.dp,
            pressedElevation = 1.dp,
            hoveredElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            
            // Bouton pour marquer comme terminée ou annuler la complétion
            if (isCompleted) {
                // Bouton pour repasser à l'état programmé
                IconButton(
                    onClick = { onUncomplete() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Marquer comme non faite",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (!isCancelled) {
                // Bouton pour marquer comme terminée
                IconButton(
                    onClick = { onComplete() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Marquer comme terminée",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
    state: TaskState,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit = {},
    onClick: () -> Unit
) {
    val isCompleted = state == TaskState.COMPLETED
    val isRunning = state == TaskState.RUNNING
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isRunning) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else 
            null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRunning) 4.dp else 2.dp,
            pressedElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icône d'état
            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terminée",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                isRunning -> {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "En cours",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textDecoration = if (isCompleted) 
                        androidx.compose.ui.text.style.TextDecoration.LineThrough 
                    else 
                        null,
                    color = if (isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
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
            
            // Bouton Démarrer (seulement si pas en cours et pas terminée)
            if (!isRunning && !isCompleted) {
                Button(
                    onClick = onStart,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Démarrer", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            // Bouton Terminer (seulement si en cours)
            if (isRunning) {
                Button(
                    onClick = onComplete,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Terminer", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            // Bouton pour repasser à l'état programmé (si terminée)
            if (isCompleted) {
                IconButton(
                    onClick = onUncomplete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Marquer comme non faite",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Bouton Info
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
    onUncomplete: () -> Unit = {},
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
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            hoveredElevation = 4.dp
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
                // Bouton pour repasser à l'état programmé
                IconButton(
                    onClick = { onUncomplete() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Marquer comme non faite",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
