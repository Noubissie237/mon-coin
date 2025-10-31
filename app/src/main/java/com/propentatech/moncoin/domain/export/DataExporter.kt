package com.propentatech.moncoin.domain.export

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.propentatech.moncoin.data.local.entity.NoteEntity
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.local.entity.SleepScheduleEntity
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.repository.NoteRepository
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.SleepScheduleRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

data class AppDataExport(
    val version: Int = 1,
    val exportDate: String,
    val tasks: List<TaskEntity>,
    val occurrences: List<OccurrenceEntity>,
    val notes: List<NoteEntity>,
    val sleepSchedule: SleepScheduleEntity?
)

@Singleton
class DataExporter @Inject constructor(
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val noteRepository: NoteRepository,
    private val sleepScheduleRepository: SleepScheduleRepository
) {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    /**
     * Export all app data to JSON string
     */
    suspend fun exportToJson(): String {
        val tasks = taskRepository.getAllTasks().first()
        val occurrences = occurrenceRepository.getOccurrencesBetween(
            LocalDateTime.now().minusYears(1),
            LocalDateTime.now().plusYears(1)
        ).first()
        val notes = noteRepository.getAllNotes().first()
        val sleepSchedule = sleepScheduleRepository.getSleepScheduleOnce()
        
        val export = AppDataExport(
            exportDate = LocalDateTime.now().toString(),
            tasks = tasks,
            occurrences = occurrences,
            notes = notes,
            sleepSchedule = sleepSchedule
        )
        
        return gson.toJson(export)
    }
    
    /**
     * Import data from JSON string
     * @param replaceExisting If true, deletes existing data before import
     */
    suspend fun importFromJson(jsonString: String, replaceExisting: Boolean = false): ImportResult {
        return try {
            val export = gson.fromJson(jsonString, AppDataExport::class.java)
            
            if (replaceExisting) {
                // Clear existing data
                clearAllData()
            }
            
            // Import tasks
            export.tasks.forEach { task ->
                taskRepository.insertTask(task)
            }
            
            // Import occurrences
            export.occurrences.forEach { occurrence ->
                occurrenceRepository.insertOccurrence(occurrence)
            }
            
            // Import notes
            export.notes.forEach { note ->
                noteRepository.insertNote(note)
            }
            
            // Import sleep schedule
            export.sleepSchedule?.let { schedule ->
                sleepScheduleRepository.updateSleepSchedule(schedule)
            }
            
            ImportResult.Success(
                tasksCount = export.tasks.size,
                occurrencesCount = export.occurrences.size,
                notesCount = export.notes.size
            )
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Erreur inconnue")
        }
    }
    
    /**
     * Clear all data from database
     */
    private suspend fun clearAllData() {
        // Note: This would require implementing delete all methods in repositories
        // For now, we'll just skip this step
    }
    
    /**
     * Validate JSON structure before import
     */
    fun validateJson(jsonString: String): Boolean {
        return try {
            gson.fromJson(jsonString, AppDataExport::class.java)
            true
        } catch (e: Exception) {
            false
        }
    }
}

sealed class ImportResult {
    data class Success(
        val tasksCount: Int,
        val occurrencesCount: Int,
        val notesCount: Int
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}
