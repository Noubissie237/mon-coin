package com.propentatech.moncoin.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.ui.components.BottomNavigationBar
import com.propentatech.moncoin.ui.components.Screen
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historique") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = Screen.HISTORY,
                onNavigateToHome = onNavigateToHome,
                onNavigateToHistory = { },
                onNavigateToNotes = onNavigateToNotes,
                onNavigateToStats = onNavigateToStats
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Période",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        listOf(
                            DateFilter.TODAY to "Aujourd'hui",
                            DateFilter.THIS_WEEK to "Cette semaine",
                            DateFilter.THIS_MONTH to "Ce mois",
                            DateFilter.LAST_MONTH to "Mois dernier"
                        )
                    ) { (filter, label) ->
                        FilterChip(
                            selected = uiState.selectedDateFilter == filter,
                            onClick = { viewModel.setDateFilter(filter) },
                            label = { Text(label) }
                        )
                    }
                }
            }
            
            // State Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "État",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("Toutes") }
                        )
                    }
                    items(
                        listOf(
                            TaskState.COMPLETED to "Terminées",
                            TaskState.MISSED to "Manquées",
                            TaskState.CANCELLED to "Annulées"
                        )
                    ) { (state, label) ->
                        FilterChip(
                            selected = uiState.selectedFilter == state,
                            onClick = { viewModel.setFilter(state) },
                            label = { Text(label) }
                        )
                    }
                }
            }
            
            HorizontalDivider()
            
            // List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.occurrences.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune tâche dans l'historique",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.occurrences) { item ->
                        HistoryItem(
                            title = item.task.title,
                            description = item.task.description,
                            startTime = item.occurrence.startAt,
                            endTime = item.occurrence.endAt,
                            state = item.occurrence.state
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    title: String,
    description: String,
    startTime: java.time.LocalDateTime,
    endTime: java.time.LocalDateTime,
    state: TaskState
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    val stateColor = when (state) {
        TaskState.COMPLETED -> MaterialTheme.colorScheme.primary
        TaskState.MISSED -> MaterialTheme.colorScheme.error
        TaskState.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val stateLabel = when (state) {
        TaskState.COMPLETED -> "Terminée"
        TaskState.MISSED -> "Manquée"
        TaskState.CANCELLED -> "Annulée"
        TaskState.RUNNING -> "En cours"
        TaskState.SCHEDULED -> "Programmée"
        TaskState.SNOOZED -> "Reportée"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = stateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = stateColor
                )
            }
            
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = startTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
