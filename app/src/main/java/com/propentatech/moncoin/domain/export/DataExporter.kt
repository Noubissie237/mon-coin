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
     * Export app data to JSON string
     * @param includeTasks Whether to include tasks and occurrences
     * @param includeNotes Whether to include notes
     */
    suspend fun exportToJson(includeTasks: Boolean = true, includeNotes: Boolean = true): String {
        android.util.Log.d("DataExporter", "Starting export: tasks=$includeTasks, notes=$includeNotes")
        
        val tasks = if (includeTasks) {
            val t = taskRepository.getAllTasks().first()
            android.util.Log.d("DataExporter", "Tasks count: ${t.size}")
            t
        } else {
            emptyList()
        }
        
        val occurrences = if (includeTasks) {
            val o = occurrenceRepository.getOccurrencesBetween(
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now().plusYears(1)
            ).first()
            android.util.Log.d("DataExporter", "Occurrences count: ${o.size}")
            o
        } else {
            emptyList()
        }
        
        val notes = if (includeNotes) {
            val n = noteRepository.getAllNotes().first()
            android.util.Log.d("DataExporter", "Notes count: ${n.size}")
            n
        } else {
            emptyList()
        }
        
        val sleepSchedule = sleepScheduleRepository.getSleepScheduleOnce()
        
        val export = AppDataExport(
            exportDate = LocalDateTime.now().toString(),
            tasks = tasks,
            occurrences = occurrences,
            notes = notes,
            sleepSchedule = sleepSchedule
        )
        
        val json = gson.toJson(export)
        android.util.Log.d("DataExporter", "Export JSON created, length: ${json.length}")
        
        return json
    }
    
    /**
     * Import data from JSON string
     * Automatically detects and imports everything present in the file
     */
    suspend fun importFromJson(jsonString: String): ImportResult {
        return try {
            val export = gson.fromJson(jsonString, AppDataExport::class.java)
            
            var tasksCount = 0
            var occurrencesCount = 0
            var notesCount = 0
            
            // Import tasks if present
            export.tasks.forEach { task ->
                taskRepository.insertTask(task)
                tasksCount++
            }
            
            // Import occurrences if present
            export.occurrences.forEach { occurrence ->
                occurrenceRepository.insertOccurrence(occurrence)
                occurrencesCount++
            }
            
            // Import notes if present
            export.notes.forEach { note ->
                noteRepository.insertNote(note)
                notesCount++
            }
            
            // Import sleep schedule if present
            export.sleepSchedule?.let { schedule ->
                sleepScheduleRepository.updateSleepSchedule(schedule)
            }
            
            ImportResult.Success(
                tasksCount = tasksCount,
                occurrencesCount = occurrencesCount,
                notesCount = notesCount
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
