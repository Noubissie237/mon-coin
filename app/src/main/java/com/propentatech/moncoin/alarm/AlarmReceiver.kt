package com.propentatech.moncoin.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmReceiverEntryPoint {
    fun notificationHelper(): NotificationHelper
    fun taskStateChecker(): TaskStateChecker
    fun occurrenceRepository(): OccurrenceRepository
}

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver onReceive: ${intent.action}")
        
        when (intent.action) {
            ACTION_ALARM_TRIGGER -> handleAlarmTrigger(context, intent)
            ACTION_START_TRIGGER -> handleStartTrigger(context, intent)
            ACTION_REMINDER_TRIGGER -> handleReminderTrigger(context, intent)
            Intent.ACTION_BOOT_COMPLETED -> handleBootCompleted(context)
        }
    }
    
    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        
        Log.d(TAG, "Alarm triggered for occurrence: $occurrenceId")
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        
        scope.launch {
            val taskStateChecker = entryPoint.taskStateChecker()
            val occurrenceRepository = entryPoint.occurrenceRepository()
            val notificationHelper = entryPoint.notificationHelper()
            
            // Check if we should trigger the alarm (only if RUNNING)
            if (taskStateChecker.shouldTriggerEndAlarm(occurrenceId)) {
                Log.d(TAG, "Launching full-screen alarm activity for task end...")
                
                // Launch AlarmActivity directly - this works even when app is killed
                val alarmIntent = Intent(context, com.propentatech.moncoin.alarm.AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                           Intent.FLAG_ACTIVITY_CLEAR_TOP or
                           Intent.FLAG_ACTIVITY_NO_USER_ACTION
                    putExtra(EXTRA_OCCURRENCE_ID, occurrenceId)
                    putExtra(EXTRA_TASK_ID, taskId)
                    putExtra(EXTRA_TASK_TITLE, taskTitle)
                }
                context.startActivity(alarmIntent)
                
                Log.d(TAG, "Alarm activity launched successfully!")
            } else {
                // Task was not started, mark as MISSED
                val occurrence = occurrenceRepository.getOccurrenceById(occurrenceId)
                if (occurrence?.state == com.propentatech.moncoin.data.model.TaskState.SCHEDULED) {
                    Log.d(TAG, "Task was not started, marking as MISSED")
                    occurrenceRepository.updateOccurrenceState(occurrenceId, com.propentatech.moncoin.data.model.TaskState.MISSED)
                    notificationHelper.showMissedTaskNotification(occurrenceId, taskTitle)
                }
            }
        }
    }
    
    private fun handleStartTrigger(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: run {
            Log.e(TAG, "Start trigger: EXTRA_OCCURRENCE_ID is null!")
            return
        }
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: run {
            Log.e(TAG, "Start trigger: EXTRA_TASK_ID is null!")
            return
        }
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        
        Log.d(TAG, "Start trigger for occurrence: $occurrenceId, task: $taskTitle")
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        
        scope.launch {
            val taskStateChecker = entryPoint.taskStateChecker()
            val occurrenceRepository = entryPoint.occurrenceRepository()
            val notificationHelper = entryPoint.notificationHelper()
            
            val occurrence = occurrenceRepository.getOccurrenceById(occurrenceId)
            Log.d(TAG, "Current state: ${occurrence?.state}")
            
            // Check if we should trigger the start alarm (only if SCHEDULED)
            if (taskStateChecker.shouldTriggerStartAlarm(occurrenceId)) {
                Log.d(TAG, "Launching full-screen start activity...")
                
                // Launch TaskStartActivity directly - this works even when app is killed
                val startIntent = Intent(context, com.propentatech.moncoin.ui.screen.task.start.TaskStartActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                           Intent.FLAG_ACTIVITY_CLEAR_TOP or
                           Intent.FLAG_ACTIVITY_NO_USER_ACTION
                    putExtra("occurrence_id", occurrenceId)
                    putExtra("task_id", taskId)
                    putExtra("task_title", taskTitle)
                }
                context.startActivity(startIntent)
                
                Log.d(TAG, "Start activity launched successfully!")
            } else {
                Log.w(TAG, "Task is not in SCHEDULED state, not triggering start alarm")
            }
        }
    }
    
    private fun handleReminderTrigger(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 10)
        
        Log.d(TAG, "Reminder triggered for occurrence: $occurrenceId")
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        
        // Show reminder notification
        scope.launch {
            val notificationHelper = entryPoint.notificationHelper()
            notificationHelper.showReminderNotification(
                occurrenceId = occurrenceId,
                taskTitle = taskTitle,
                minutesBefore = minutesBefore
            )
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "Boot completed - rescheduling alarms and starting foreground service")
        
        // Start the foreground service to keep app running in background
        val foregroundServiceIntent = Intent(context, com.propentatech.moncoin.service.AlarmForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(foregroundServiceIntent)
        } else {
            context.startService(foregroundServiceIntent)
        }
        
        // Start a service to reschedule all alarms
        val serviceIntent = Intent(context, AlarmRescheduleService::class.java)
        context.startService(serviceIntent)
    }
    
    companion object {
        private const val TAG = "AlarmReceiver"
        
        const val ACTION_ALARM_TRIGGER = "com.propentatech.moncoin.ALARM_TRIGGER"
        const val ACTION_START_TRIGGER = "com.propentatech.moncoin.START_TRIGGER"
        const val ACTION_REMINDER_TRIGGER = "com.propentatech.moncoin.REMINDER_TRIGGER"
        
        const val EXTRA_OCCURRENCE_ID = "occurrence_id"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_MINUTES_BEFORE = "minutes_before"
    }
}
