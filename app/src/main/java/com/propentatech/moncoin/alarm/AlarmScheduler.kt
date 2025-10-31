package com.propentatech.moncoin.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * Schedule an exact alarm for a task occurrence
     */
    fun scheduleAlarm(occurrence: OccurrenceEntity, taskTitle: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_OCCURRENCE_ID, occurrence.id)
            putExtra(AlarmReceiver.EXTRA_TASK_ID, occurrence.taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            occurrence.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = occurrence.endAt.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
        
        // Use exact alarm that works even in Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * Schedule a reminder notification before the task starts
     */
    fun scheduleReminder(
        occurrence: OccurrenceEntity,
        taskTitle: String,
        minutesBefore: Int
    ) {
        val reminderTime = occurrence.startAt.minusMinutes(minutesBefore.toLong())
        
        // Don't schedule if reminder time is in the past
        if (reminderTime.isBefore(LocalDateTime.now())) {
            return
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER_TRIGGER
            putExtra(AlarmReceiver.EXTRA_OCCURRENCE_ID, occurrence.id)
            putExtra(AlarmReceiver.EXTRA_TASK_ID, occurrence.taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(AlarmReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (occurrence.id + "_reminder_$minutesBefore").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = reminderTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancel an alarm for a specific occurrence
     */
    fun cancelAlarm(occurrenceId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Cancel a reminder for a specific occurrence
     */
    fun cancelReminder(occurrenceId: String, minutesBefore: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (occurrenceId + "_reminder_$minutesBefore").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Check if exact alarms are allowed (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
