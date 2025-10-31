package com.propentatech.moncoin.data.repository

import com.propentatech.moncoin.data.local.dao.OccurrenceDao
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.model.TaskState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OccurrenceRepository @Inject constructor(
    private val occurrenceDao: OccurrenceDao
) {
    fun getAllOccurrences(): Flow<List<OccurrenceEntity>> = occurrenceDao.getAllOccurrences()
    
    suspend fun getOccurrenceById(occurrenceId: String): OccurrenceEntity? = 
        occurrenceDao.getOccurrenceById(occurrenceId)
    
    fun getOccurrencesByTaskId(taskId: String): Flow<List<OccurrenceEntity>> = 
        occurrenceDao.getOccurrencesByTaskId(taskId)
    
    fun getOccurrencesByState(state: TaskState): Flow<List<OccurrenceEntity>> = 
        occurrenceDao.getOccurrencesByState(state)
    
    fun getOccurrencesBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<OccurrenceEntity>> = occurrenceDao.getOccurrencesBetween(startDate, endDate)
    
    fun getOccurrencesByStateAndDateRange(
        state: TaskState,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<OccurrenceEntity>> = 
        occurrenceDao.getOccurrencesByStateAndDateRange(state, startDate, endDate)
    
    fun getOccurrencesByStatesAndDateRange(
        states: List<TaskState>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<OccurrenceEntity>> = 
        occurrenceDao.getOccurrencesByStatesAndDateRange(states, startDate, endDate)
    
    suspend fun getMissedOccurrences(now: LocalDateTime): List<OccurrenceEntity> = 
        occurrenceDao.getMissedOccurrences(now)
    
    suspend fun insertOccurrence(occurrence: OccurrenceEntity): Long = 
        occurrenceDao.insertOccurrence(occurrence)
    
    suspend fun insertOccurrences(occurrences: List<OccurrenceEntity>) = 
        occurrenceDao.insertOccurrences(occurrences)
    
    suspend fun updateOccurrence(occurrence: OccurrenceEntity) = 
        occurrenceDao.updateOccurrence(occurrence)
    
    suspend fun deleteOccurrence(occurrence: OccurrenceEntity) = 
        occurrenceDao.deleteOccurrence(occurrence)
    
    suspend fun deleteOccurrenceById(occurrenceId: String) = 
        occurrenceDao.deleteOccurrenceById(occurrenceId)
    
    suspend fun deleteOccurrencesByTaskId(taskId: String) = 
        occurrenceDao.deleteOccurrencesByTaskId(taskId)
    
    suspend fun updateOccurrenceState(occurrenceId: String, state: TaskState) = 
        occurrenceDao.updateOccurrenceState(occurrenceId, state)
}
