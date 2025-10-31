package com.propentatech.moncoin.data.model

enum class TaskState {
    SCHEDULED,   // Programmed
    RUNNING,     // In progress
    COMPLETED,   // Finished
    MISSED,      // Not started and end time passed
    CANCELLED,   // Cancelled by user
    SNOOZED      // Snoozed
}
