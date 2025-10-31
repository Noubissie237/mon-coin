package com.propentatech.moncoin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.propentatech.moncoin.data.local.converter.Converters
import com.propentatech.moncoin.data.model.TaskState
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "occurrences",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId"), Index("startAt"), Index("state")]
)
@TypeConverters(Converters::class)
data class OccurrenceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val state: TaskState = TaskState.SCHEDULED,
    val actualStartTime: LocalDateTime? = null,  // When user actually started
    val actualEndTime: LocalDateTime? = null,    // When user actually finished
    val snoozeCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
