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
import kotlinx.coroutines.flow.first
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
        
        Log.d(TAG, "Starting to reschedule all alarms after boot...")
        
        try {
            // Get all future occurrences (SCHEDULED or RUNNING)
            val allOccurrences = occurrenceRepository.getOccurrencesBetween(
                now.minusDays(1), // Include occurrences from yesterday that might still be running
                futureDate
            ).first()
            
            Log.d(TAG, "Found ${allOccurrences.size} occurrences to reschedule")
            
            // For each occurrence, get the task and reschedule alarms
            allOccurrences.forEach { occurrence ->
                // Only reschedule if occurrence is in the future or currently running
                if (occurrence.endAt.isAfter(now) && 
                    (occurrence.state == TaskState.SCHEDULED || occurrence.state == TaskState.RUNNING)) {
                    
                    val task = taskRepository.getTaskById(occurrence.taskId)
                    if (task != null) {
                        Log.d(TAG, "Rescheduling alarms for: ${task.title}")
                        
                        // Schedule start alarm if not yet started and start time is in the future
                        if (occurrence.state == TaskState.SCHEDULED && occurrence.startAt.isAfter(now)) {
                            alarmScheduler.scheduleStartAlarm(occurrence, task.title)
                            Log.d(TAG, "  - Start alarm scheduled for ${occurrence.startAt}")
                        }
                        
                        // Schedule end alarm if end time is in the future
                        if (occurrence.endAt.isAfter(now) && task.alarmsEnabled) {
                            alarmScheduler.scheduleAlarm(occurrence, task.title)
                            Log.d(TAG, "  - End alarm scheduled for ${occurrence.endAt}")
                        }
                        
                        // Schedule reminders if notifications are enabled and start time is in the future
                        if (task.notificationsEnabled && occurrence.startAt.isAfter(now)) {
                            task.reminders.forEach { minutesBefore ->
                                alarmScheduler.scheduleReminder(occurrence, task.title, minutesBefore)
                                Log.d(TAG, "  - Reminder scheduled for $minutesBefore minutes before")
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "All alarms rescheduled successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling alarms", e)
        }
    }
    
    companion object {
        private const val TAG = "AlarmRescheduleService"
    }
}
