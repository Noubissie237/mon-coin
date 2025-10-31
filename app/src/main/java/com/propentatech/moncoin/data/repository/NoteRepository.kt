package com.propentatech.moncoin.data.repository

import com.propentatech.moncoin.data.local.dao.NoteDao
import com.propentatech.moncoin.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(noteId: String): NoteEntity? = noteDao.getNoteById(noteId)
    
    fun getNotesByTaskId(taskId: String): Flow<List<NoteEntity>> = 
        noteDao.getNotesByTaskId(taskId)
    
    fun getNotesBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<NoteEntity>> = noteDao.getNotesBetween(startDate, endDate)
    
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
    
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun deleteNoteById(noteId: String) = noteDao.deleteNoteById(noteId)
}
