package com.propentatech.moncoin.ui.screen.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.propentatech.moncoin.ui.components.BottomNavigationBar
import com.propentatech.moncoin.ui.components.ChartData
import com.propentatech.moncoin.ui.components.Screen
import com.propentatech.moncoin.ui.components.SimpleBarChart
import com.propentatech.moncoin.ui.components.SimplePieChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = Screen.STATS,
                onNavigateToHome = onNavigateToHome,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToNotes = onNavigateToNotes,
                onNavigateToStats = { }
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
                // Period selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.period == StatisticsPeriod.WEEK,
                            onClick = { viewModel.setPeriod(StatisticsPeriod.WEEK) },
                            label = { Text("Semaine") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.period == StatisticsPeriod.MONTH,
                            onClick = { viewModel.setPeriod(StatisticsPeriod.MONTH) },
                            label = { Text("Mois") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.period == StatisticsPeriod.ALL,
                            onClick = { viewModel.setPeriod(StatisticsPeriod.ALL) },
                            label = { Text("Tout") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Overview cards
                item {
                    Column {
                        Text(
                            text = "Vue d'ensemble",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Statistiques basées sur toutes les occurrences de tâches générées",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Total",
                            value = uiState.totalTasks.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Terminées",
                            value = uiState.completedTasks.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Manquées",
                            value = uiState.missedTasks.toString(),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Aujourd'hui",
                            value = uiState.pendingTasks.toString(),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Completion rate
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Taux de réussite",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LinearProgressIndicator(
                                progress = { uiState.completionRate / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp),
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${String.format("%.1f", uiState.completionRate)}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Pie chart - Distribution des tâches
                if (uiState.totalTasks > 0) {
                    item {
                        Text(
                            text = "Répartition des tâches",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                SimplePieChart(
                                    data = listOf(
                                        ChartData(
                                            label = "Terminées",
                                            value = uiState.completedTasks.toFloat(),
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        ChartData(
                                            label = "Manquées",
                                            value = uiState.missedTasks.toFloat(),
                                            color = MaterialTheme.colorScheme.error
                                        ),
                                        ChartData(
                                            label = "Aujourd'hui",
                                            value = uiState.pendingTasks.toFloat(),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    ).filter { it.value > 0 }
                                )
                            }
                        }
                    }
                }
                
                // Bar chart - Statistiques par jour
                if (uiState.dailyStats.any { it.total > 0 }) {
                    item {
                        Text(
                            text = "Performance par jour de la semaine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Tâches terminées",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SimpleBarChart(
                                    data = uiState.dailyStats.map { day ->
                                        ChartData(
                                            label = day.dayName,
                                            value = day.completed.toFloat(),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Tâches manquées",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SimpleBarChart(
                                    data = uiState.dailyStats.map { day ->
                                        ChartData(
                                            label = day.dayName,
                                            value = day.missed.toFloat(),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Interpretation
                if (uiState.interpretation.isNotEmpty()) {
                    item {
                        Text(
                            text = "Analyse de vos performances",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = uiState.interpretation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Empty state
                if (uiState.totalTasks == 0) {
                    item {
                        Card {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucune donnée pour cette période",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimeStatCard(
    title: String,
    minutes: Long,
    modifier: Modifier = Modifier
) {
    val hours = minutes / 60
    val mins = minutes % 60
    
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = hours.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "h ",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = mins.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "min",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
