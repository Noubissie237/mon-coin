package com.propentatech.moncoin.data.local.dao

import androidx.room.*
import com.propentatech.moncoin.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?
    
    @Query("SELECT * FROM notes WHERE relatedTaskId = :taskId ORDER BY date DESC")
    fun getNotesByTaskId(taskId: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getNotesBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)
}
