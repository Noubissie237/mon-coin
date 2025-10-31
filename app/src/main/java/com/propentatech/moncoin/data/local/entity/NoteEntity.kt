package com.propentatech.moncoin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.propentatech.moncoin.data.local.converter.Converters
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedTaskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("relatedTaskId"), Index("date")]
)
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime = LocalDateTime.now(),
    val content: String,
    val tags: List<String> = emptyList(),
    val relatedTaskId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
