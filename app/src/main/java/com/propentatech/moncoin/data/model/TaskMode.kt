package com.propentatech.moncoin.data.model

enum class TaskMode {
    DUREE,    // Duration-based (e.g., 5 hours)
    PLAGE,    // Time range (e.g., 12:00-14:30)
    FLEXIBLE  // No fixed time/duration - flexible task to complete anytime
}
