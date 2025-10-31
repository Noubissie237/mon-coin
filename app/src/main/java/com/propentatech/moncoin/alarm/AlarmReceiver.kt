package com.propentatech.moncoin.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
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
        
        // Start AlarmActivity to show full-screen alarm
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OCCURRENCE_ID, occurrenceId)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        context.startActivity(alarmIntent)
    }
    
    private fun handleStartTrigger(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        
        Log.d(TAG, "Start trigger for occurrence: $occurrenceId")
        
        // Start TaskStartActivity to show full-screen start prompt
        val startIntent = Intent(context, com.propentatech.moncoin.ui.screen.task.start.TaskStartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OCCURRENCE_ID, occurrenceId)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        context.startActivity(startIntent)
    }
    
    private fun handleReminderTrigger(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 10)
        
        Log.d(TAG, "Reminder triggered for occurrence: $occurrenceId")
        
        // Show reminder notification
        scope.launch {
            notificationHelper.showReminderNotification(
                occurrenceId = occurrenceId,
                taskTitle = taskTitle,
                minutesBefore = minutesBefore
            )
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "Boot completed - rescheduling alarms")
        
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
