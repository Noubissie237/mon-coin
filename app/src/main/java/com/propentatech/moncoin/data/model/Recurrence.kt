package com.propentatech.moncoin.data.model

import java.time.DayOfWeek
import java.time.LocalDateTime

data class Recurrence(
    val daysOfWeek: List<DayOfWeek> = emptyList(),  // For PERIODIQUE tasks
    val interval: Int = 1,                           // Interval in days
    val endDate: LocalDateTime? = null               // Optional end date for recurrence
)
