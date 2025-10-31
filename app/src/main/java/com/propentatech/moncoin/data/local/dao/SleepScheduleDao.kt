package com.propentatech.moncoin.data.local.dao

import androidx.room.*
import com.propentatech.moncoin.data.local.entity.SleepScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepScheduleDao {
    
    @Query("SELECT * FROM sleep_schedule WHERE id = 1")
    fun getSleepSchedule(): Flow<SleepScheduleEntity?>
    
    @Query("SELECT * FROM sleep_schedule WHERE id = 1")
    suspend fun getSleepScheduleOnce(): SleepScheduleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSchedule(schedule: SleepScheduleEntity)
    
    @Update
    suspend fun updateSleepSchedule(schedule: SleepScheduleEntity)
    
    @Query("DELETE FROM sleep_schedule")
    suspend fun deleteSleepSchedule()
}
