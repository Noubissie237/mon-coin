package com.propentatech.moncoin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class Screen {
    HOME,
    HISTORY,
    NOTES,
    STATS
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Accueil") },
            selected = currentScreen == Screen.HOME,
            onClick = onNavigateToHome
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            label = { Text("Historique") },
            selected = currentScreen == Screen.HISTORY,
            onClick = onNavigateToHistory
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Create, contentDescription = null) },
            label = { Text("Notes") },
            selected = currentScreen == Screen.NOTES,
            onClick = onNavigateToNotes
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Stats") },
            selected = currentScreen == Screen.STATS,
            onClick = onNavigateToStats
        )
    }
}
