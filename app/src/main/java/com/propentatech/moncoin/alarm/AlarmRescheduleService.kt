package com.propentatech.moncoin.alarm

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Service to reschedule all alarms after device reboot
 */
@AndroidEntryPoint
class AlarmRescheduleService : Service() {
    
    @Inject
    lateinit var occurrenceRepository: OccurrenceRepository
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmRescheduleService started")
        
        scope.launch {
            try {
                rescheduleAllAlarms()
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms", e)
            } finally {
                stopSelf(startId)
            }
        }
        
        return START_NOT_STICKY
    }
    
    private suspend fun rescheduleAllAlarms() {
        val now = LocalDateTime.now()
        val futureDate = now.plusMonths(1) // Reschedule alarms for next month
        
        // Get all scheduled occurrences in the future
        val states = listOf(TaskState.SCHEDULED, TaskState.RUNNING)
        
        // Note: This is a simplified version. In production, you'd want to collect the Flow
        // For now, we'll use a workaround
        Log.d(TAG, "Rescheduling alarms for future occurrences")
        
        // TODO: Implement proper rescheduling logic
        // This would involve:
        // 1. Getting all future occurrences
        // 2. For each occurrence, get the associated task
        // 3. Schedule alarm and reminders
    }
    
    companion object {
        private const val TAG = "AlarmRescheduleService"
    }
}
