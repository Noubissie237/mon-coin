package com.propentatech.moncoin.di

import android.content.Context
import androidx.room.Room
import com.propentatech.moncoin.data.local.MonCoinDatabase
import com.propentatech.moncoin.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideMonCoinDatabase(
        @ApplicationContext context: Context
    ): MonCoinDatabase {
        return Room.databaseBuilder(
            context,
            MonCoinDatabase::class.java,
            MonCoinDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: MonCoinDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    @Singleton
    fun provideOccurrenceDao(database: MonCoinDatabase): OccurrenceDao {
        return database.occurrenceDao()
    }
    
    @Provides
    @Singleton
    fun provideNoteDao(database: MonCoinDatabase): NoteDao {
        return database.noteDao()
    }
    
    @Provides
    @Singleton
    fun provideSleepScheduleDao(database: MonCoinDatabase): SleepScheduleDao {
        return database.sleepScheduleDao()
    }
}
