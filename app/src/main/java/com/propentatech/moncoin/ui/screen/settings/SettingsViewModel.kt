package com.propentatech.moncoin.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.domain.export.DataExporter
import com.propentatech.moncoin.domain.export.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedData: String? = null,
    val importResult: ImportResult? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataExporter: DataExporter
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun exportData(includeTasks: Boolean = true, includeNotes: Boolean = true) {
        android.util.Log.d("SettingsViewModel", "exportData called: tasks=$includeTasks, notes=$includeNotes")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null)
            
            try {
                android.util.Log.d("SettingsViewModel", "Starting export...")
                val json = dataExporter.exportToJson(includeTasks, includeNotes)
                android.util.Log.d("SettingsViewModel", "Export successful, JSON length: ${json.length}")
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportedData = json
                )
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Export error", e)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Erreur lors de l'export: ${e.message}"
                )
            }
        }
    }
    
    fun importData(jsonString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, error = null)
            
            // Validate JSON first
            if (!dataExporter.validateJson(jsonString)) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Format JSON invalide"
                )
                return@launch
            }
            
            try {
                // Import everything that's in the file
                val result = dataExporter.importFromJson(jsonString)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Erreur lors de l'import: ${e.message}"
                )
            }
        }
    }
    
    fun clearExportedData() {
        _uiState.value = _uiState.value.copy(exportedData = null)
    }
    
    fun clearImportResult() {
        _uiState.value = _uiState.value.copy(importResult = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
