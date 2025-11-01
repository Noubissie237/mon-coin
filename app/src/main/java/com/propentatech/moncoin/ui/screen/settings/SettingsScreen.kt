package com.propentatech.moncoin.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.propentatech.moncoin.domain.export.ImportResult
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSleepSchedule: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // File picker launcher for import
    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonContent = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (jsonContent != null) {
                    viewModel.importData(jsonContent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    // Show import result
    LaunchedEffect(uiState.importResult) {
        when (val result = uiState.importResult) {
            is ImportResult.Success -> {
                snackbarHostState.showSnackbar(
                    "Import réussi: ${result.tasksCount} tâches, ${result.notesCount} notes"
                )
                viewModel.clearImportResult()
            }
            is ImportResult.Error -> {
                snackbarHostState.showSnackbar("Erreur: ${result.message}")
                viewModel.clearImportResult()
            }
            null -> {}
        }
    }
    
    // Handle export data
    LaunchedEffect(uiState.exportedData) {
        uiState.exportedData?.let { json ->
            android.util.Log.d("SettingsScreen", "Export data received, sharing file...")
            shareJsonFile(context, json)
            viewModel.clearExportedData()
            snackbarHostState.showSnackbar("Export réussi ! Choisissez où sauvegarder le fichier.")
        }
    }
    
    // Debug: Log dialog state
    LaunchedEffect(showExportDialog) {
        android.util.Log.d("SettingsScreen", "Export dialog state: $showExportDialog")
    }
    
    // Export Dialog - Outside Scaffold
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { 
                android.util.Log.d("SettingsScreen", "Export dialog dismissed")
                showExportDialog = false 
            },
            onExport = { includeTasks, includeNotes ->
                android.util.Log.d("SettingsScreen", "Export confirmed: tasks=$includeTasks, notes=$includeNotes")
                viewModel.exportData(includeTasks, includeNotes)
                showExportDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Sommeil",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsClickableItem(
                    title = "Plage de sommeil",
                    description = "Configurer votre horaire de sommeil",
                    onClick = onNavigateToSleepSchedule
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                var showDataInfoDialog by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Données",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = { showDataInfoDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Information sur les données",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (showDataInfoDialog) {
                    DataInfoDialog(
                        onDismiss = { showDataInfoDialog = false }
                    )
                }
            }
            
            item {
                SettingsClickableItem(
                    title = "Exporter les données",
                    description = "Sauvegarder vos tâches et notes",
                    onClick = { 
                        android.util.Log.d("SettingsScreen", "Export button clicked")
                        showExportDialog = true 
                    },
                    isLoading = uiState.isExporting
                )
            }
            
            item {
                SettingsClickableItem(
                    title = "Importer les données",
                    description = "Restaurer vos tâches et notes",
                    onClick = { filePickerLauncher.launch("application/json") },
                    isLoading = uiState.isImporting
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Text(
                    text = "Support",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsClickableItem(
                    title = "Contacter le développeur",
                    description = "Besoin d'aide ? Une suggestion ?",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/message/5M3EXSM2BMNKD1"))
                        context.startActivity(intent)
                    }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Text(
                    text = "À propos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                AboutCard(
                    onPortfolioClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://noubissie.propentatech.com"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

// Helper function to share JSON file
private fun shareJsonFile(context: Context, jsonContent: String) {
    try {
        val fileName = "moncoin_export_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonContent)
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Partager l'export"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun AboutCard(
    onPortfolioClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App info
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Move",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            // Developer info
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Développé par",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Noubissie Wilfried",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Portfolio button
            Button(
                onClick = onPortfolioClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Visiter mon portfolio")
            }
            
            // Copyright
            Text(
                text = "© 2025 Tous droits réservés.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (includeTasks: Boolean, includeNotes: Boolean) -> Unit
) {
    var includeTasks by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exporter les données") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sélectionnez ce que vous souhaitez exporter :")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Tâches")
                    Checkbox(
                        checked = includeTasks,
                        onCheckedChange = { includeTasks = it }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Notes")
                    Checkbox(
                        checked = includeNotes,
                        onCheckedChange = { includeNotes = it }
                    )
                }
                
                if (!includeTasks && !includeNotes) {
                    Text(
                        "Veuillez sélectionner au moins un type de données",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onExport(includeTasks, includeNotes) },
                enabled = includeTasks || includeNotes
            ) {
                Text("Exporter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun DataInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Gestion des données",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sauvegardez et restaurez vos données pour ne jamais perdre vos tâches et notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                HorizontalDivider()
                
                // Export
                DataInfoItem(
                    icon = Icons.Default.Upload,
                    title = "Exporter les données",
                    description = "Créez une sauvegarde de vos tâches et notes dans un fichier JSON. Vous pouvez choisir d'exporter uniquement les tâches, uniquement les notes, ou les deux.",
                    example = "Astuce : Exportez vos données après chaque modification ou ajout important."
                )
                
                HorizontalDivider()
                
                // Import
                DataInfoItem(
                    icon = Icons.Default.Download,
                    title = "Importer les données",
                    description = "Restaurez vos données à partir d'un fichier JSON précédemment exporté. Vos données actuelles seront remplacées par celles du fichier.",
                    example = "Attention : L'import remplacera toutes vos données actuelles."
                )
                
                HorizontalDivider()
                
                Text(
                    text = "Conseil",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "Avant de changer de téléphone, pensez à exporter vos données puis à les importer sur le nouveau, afin d’éviter de tout ressaisir manuellement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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
fun DataInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    example: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Top
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
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
