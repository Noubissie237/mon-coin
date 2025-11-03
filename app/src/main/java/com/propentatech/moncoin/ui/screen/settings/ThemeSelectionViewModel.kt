package com.propentatech.moncoin.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.data.preferences.AppPreferences
import com.propentatech.moncoin.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSelectionViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    
    val selectedTheme: StateFlow<AppTheme> = appPreferences.selectedTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.INDIGO_SOFT
        )
    
    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            appPreferences.saveTheme(theme)
        }
    }
}
