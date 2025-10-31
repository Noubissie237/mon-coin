package com.propentatech.moncoin.ui.screen.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.data.local.entity.NoteEntity
import com.propentatech.moncoin.data.repository.NoteRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class NoteDetailUiState(
    val content: String = "",
    val tags: List<String> = emptyList(),
    val relatedTaskId: String? = null,
    val relatedTaskTitle: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isNewNote: Boolean = true
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val noteId: String? = savedStateHandle["noteId"]
    
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()
    
    init {
        noteId?.let { loadNote(it) }
    }
    
    private fun loadNote(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val note = noteRepository.getNoteById(id)
            if (note != null) {
                val taskTitle = note.relatedTaskId?.let { taskId ->
                    taskRepository.getTaskById(taskId)?.title
                }
                
                _uiState.value = NoteDetailUiState(
                    content = note.content,
                    tags = note.tags,
                    relatedTaskId = note.relatedTaskId,
                    relatedTaskTitle = taskTitle,
                    isLoading = false,
                    isNewNote = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Note introuvable"
                )
            }
        }
    }
    
    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }
    
    fun addTag(tag: String) {
        val currentTags = _uiState.value.tags.toMutableList()
        val trimmedTag = tag.trim()
        if (trimmedTag.isNotBlank() && !currentTags.contains(trimmedTag)) {
            currentTags.add(trimmedTag)
            _uiState.value = _uiState.value.copy(tags = currentTags)
        }
    }
    
    fun removeTag(tag: String) {
        val currentTags = _uiState.value.tags.toMutableList()
        currentTags.remove(tag)
        _uiState.value = _uiState.value.copy(tags = currentTags)
    }
    
    fun setRelatedTask(taskId: String?) {
        viewModelScope.launch {
            val taskTitle = taskId?.let { taskRepository.getTaskById(it)?.title }
            _uiState.value = _uiState.value.copy(
                relatedTaskId = taskId,
                relatedTaskTitle = taskTitle
            )
        }
    }
    
    fun saveNote() {
        val state = _uiState.value
        
        if (state.content.isBlank()) {
            _uiState.value = state.copy(error = "Le contenu ne peut pas être vide")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                if (state.isNewNote) {
                    // Create new note
                    val note = NoteEntity(
                        content = state.content,
                        tags = state.tags,
                        relatedTaskId = state.relatedTaskId,
                        date = LocalDateTime.now()
                    )
                    noteRepository.insertNote(note)
                } else {
                    // Update existing note
                    noteId?.let { id ->
                        val existingNote = noteRepository.getNoteById(id)
                        existingNote?.let { note ->
                            val updatedNote = note.copy(
                                content = state.content,
                                tags = state.tags,
                                relatedTaskId = state.relatedTaskId,
                                updatedAt = LocalDateTime.now()
                            )
                            noteRepository.updateNote(updatedNote)
                        }
                    }
                }
                
                _uiState.value = state.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Erreur lors de la sauvegarde: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
