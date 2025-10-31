package com.propentatech.moncoin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.propentatech.moncoin.data.local.converter.Converters
import com.propentatech.moncoin.data.local.dao.*
import com.propentatech.moncoin.data.local.entity.*

@Database(
    entities = [
        TaskEntity::class,
        OccurrenceEntity::class,
        NoteEntity::class,
        SleepScheduleEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MonCoinDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun noteDao(): NoteDao
    abstract fun sleepScheduleDao(): SleepScheduleDao
    
    companion object {
        const val DATABASE_NAME = "moncoin_database"
    }
}
