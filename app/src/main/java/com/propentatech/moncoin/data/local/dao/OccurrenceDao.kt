package com.propentatech.moncoin.data.local.dao

import androidx.room.*
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.model.TaskState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface OccurrenceDao {
    
    @Query("SELECT * FROM occurrences ORDER BY startAt DESC")
    fun getAllOccurrences(): Flow<List<OccurrenceEntity>>
    
    @Query("SELECT * FROM occurrences WHERE id = :occurrenceId")
    suspend fun getOccurrenceById(occurrenceId: String): OccurrenceEntity?
    
    @Query("SELECT * FROM occurrences WHERE taskId = :taskId ORDER BY startAt ASC")
    fun getOccurrencesByTaskId(taskId: String): Flow<List<OccurrenceEntity>>
    
    @Query("SELECT * FROM occurrences WHERE state = :state ORDER BY startAt ASC")
    fun getOccurrencesByState(state: TaskState): Flow<List<OccurrenceEntity>>
    
    @Query("SELECT * FROM occurrences WHERE startAt >= :startDate AND startAt <= :endDate ORDER BY startAt ASC")
    fun getOccurrencesBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<OccurrenceEntity>>
    
    @Query("SELECT * FROM occurrences WHERE state = :state AND startAt >= :startDate AND startAt <= :endDate ORDER BY startAt ASC")
    fun getOccurrencesByStateAndDateRange(
        state: TaskState,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<OccurrenceEntity>>
    
    @Query("SELECT * FROM occurrences WHERE state IN (:states) AND startAt >= :startDate AND startAt <= :endDate ORDER BY startAt ASC")
    fun getOccurrencesByStatesAndDateRange(
        states: List<TaskState>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<OccurrenceEntity>>
    
    @Query("SELECT * FROM occurrences WHERE endAt <= :now AND state = :scheduledState")
    suspend fun getMissedOccurrences(now: LocalDateTime, scheduledState: TaskState = TaskState.SCHEDULED): List<OccurrenceEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: OccurrenceEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrences(occurrences: List<OccurrenceEntity>)
    
    @Update
    suspend fun updateOccurrence(occurrence: OccurrenceEntity)
    
    @Delete
    suspend fun deleteOccurrence(occurrence: OccurrenceEntity)
    
    @Query("DELETE FROM occurrences WHERE id = :occurrenceId")
    suspend fun deleteOccurrenceById(occurrenceId: String)
    
    @Query("DELETE FROM occurrences WHERE taskId = :taskId")
    suspend fun deleteOccurrencesByTaskId(taskId: String)
    
    @Query("UPDATE occurrences SET state = :state WHERE id = :occurrenceId")
    suspend fun updateOccurrenceState(occurrenceId: String, state: TaskState)
}
