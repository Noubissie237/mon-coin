package com.propentatech.moncoin.data.repository

import com.propentatech.moncoin.data.local.dao.SleepScheduleDao
import com.propentatech.moncoin.data.local.entity.SleepScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepScheduleRepository @Inject constructor(
    private val sleepScheduleDao: SleepScheduleDao
) {
    fun getSleepSchedule(): Flow<SleepScheduleEntity?> = sleepScheduleDao.getSleepSchedule()
    
    suspend fun getSleepScheduleOnce(): SleepScheduleEntity? = 
        sleepScheduleDao.getSleepScheduleOnce()
    
    suspend fun insertSleepSchedule(schedule: SleepScheduleEntity) = 
        sleepScheduleDao.insertSleepSchedule(schedule)
    
    suspend fun updateSleepSchedule(schedule: SleepScheduleEntity) = 
        sleepScheduleDao.updateSleepSchedule(schedule)
    
    suspend fun deleteSleepSchedule() = sleepScheduleDao.deleteSleepSchedule()
}
