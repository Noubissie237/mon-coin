package com.propentatech.moncoin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "sleep_schedule")
data class SleepScheduleEntity(
    @PrimaryKey
    val id: Int = 1,  // Single row
    val startTime: String,  // LocalTime as String (HH:mm)
    val endTime: String,    // LocalTime as String (HH:mm)
    val targetDurationMinutes: Int = 360  // 6 hours default
) {
    fun getStartLocalTime(): LocalTime = LocalTime.parse(startTime)
    fun getEndLocalTime(): LocalTime = LocalTime.parse(endTime)
}
