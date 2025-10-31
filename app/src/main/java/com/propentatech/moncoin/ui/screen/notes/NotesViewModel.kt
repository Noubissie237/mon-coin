package com.propentatech.moncoin.ui.screen.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.data.local.entity.NoteEntity
import com.propentatech.moncoin.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        loadNotes()
    }
    
    private fun loadNotes() {
        viewModelScope.launch {
            combine(
                noteRepository.getAllNotes(),
                _searchQuery
            ) { notes, query ->
                val filtered = if (query.isBlank()) {
                    notes
                } else {
                    notes.filter { note ->
                        note.content.contains(query, ignoreCase = true) ||
                        note.tags.any { it.contains(query, ignoreCase = true) }
                    }
                }
                
                val selectedTag = _uiState.value.selectedTag
                val tagFiltered = if (selectedTag != null) {
                    filtered.filter { it.tags.contains(selectedTag) }
                } else {
                    filtered
                }
                
                NotesUiState(
                    notes = tagFiltered.sortedByDescending { it.date },
                    searchQuery = query,
                    selectedTag = selectedTag,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectTag(tag: String?) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
        loadNotes()
    }
    
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(noteId)
        }
    }
    
    fun getAllTags(): List<String> {
        return _uiState.value.notes
            .flatMap { it.tags }
            .distinct()
            .sorted()
    }
}
