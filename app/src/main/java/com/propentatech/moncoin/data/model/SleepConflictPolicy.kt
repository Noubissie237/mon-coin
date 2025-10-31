package com.propentatech.moncoin.data.model

enum class SleepConflictPolicy {
    BLOCK,          // Refuse to save
    PROPOSE_SHIFT,  // Propose to move task
    FORCE           // Force and override sleep
}
