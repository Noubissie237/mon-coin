package com.propentatech.moncoin.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.propentatech.moncoin.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * Gestion des préférences de l'application
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
        val SELECTED_THEME = intPreferencesKey("selected_theme")
    }
    
    /**
     * Flow qui indique si l'utilisateur a déjà vu le tutoriel
     */
    val hasSeenTutorial: Flow<Boolean> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_TUTORIAL] ?: false
        }
    
    /**
     * Flow du thème sélectionné
     */
    val selectedTheme: Flow<AppTheme> = context.appDataStore.data
        .map { preferences ->
            val themeOrdinal = preferences[PreferencesKeys.SELECTED_THEME] ?: 0
            AppTheme.fromOrdinal(themeOrdinal)
        }
    
    /**
     * Marquer le tutoriel comme vu
     */
    suspend fun markTutorialAsSeen() {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_TUTORIAL] = true
        }
    }
    
    /**
     * Réinitialiser le tutoriel (pour les tests ou paramètres)
     */
    suspend fun resetTutorial() {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_TUTORIAL] = false
        }
    }
    
    /**
     * Sauvegarder le thème sélectionné
     */
    suspend fun saveTheme(theme: AppTheme) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME] = theme.ordinal
        }
    }
}
