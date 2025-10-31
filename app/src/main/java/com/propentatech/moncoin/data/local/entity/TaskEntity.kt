package com.propentatech.moncoin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.propentatech.moncoin.data.local.converter.Converters
import com.propentatech.moncoin.data.model.*
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val type: TaskType,
    val recurrence: Recurrence? = null,
    val mode: TaskMode,
    val durationMinutes: Int? = null,        // For DUREE mode
    val startTime: LocalDateTime? = null,    // For PLAGE mode
    val endTime: LocalDateTime? = null,      // For PLAGE mode
    val sleepConflictPolicy: SleepConflictPolicy = SleepConflictPolicy.BLOCK,
    val reminders: List<Int> = emptyList(),  // Minutes before task
    val alarmSoundUri: String? = null,
    val priority: Int = 0,
    val state: TaskState = TaskState.SCHEDULED,
    val notificationsEnabled: Boolean = true,
    val alarmsEnabled: Boolean = true,
    val color: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
