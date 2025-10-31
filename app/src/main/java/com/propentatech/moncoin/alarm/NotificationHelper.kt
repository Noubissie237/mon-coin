package com.propentatech.moncoin.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.propentatech.moncoin.MonCoinApplication
import com.propentatech.moncoin.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Show a reminder notification before task starts
     */
    fun showReminderNotification(
        occurrenceId: String,
        taskTitle: String,
        minutesBefore: Int
    ) {
        // Create intent to open the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MonCoinApplication.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rappel : $taskTitle")
            .setContentText("Commence dans $minutesBefore minutes")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(occurrenceId.hashCode(), notification)
    }
    
    /**
     * Show a notification for missed task
     */
    fun showMissedTaskNotification(
        occurrenceId: String,
        taskTitle: String
    ) {
        // Create intent to open the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MonCoinApplication.CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tâche manquée")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(occurrenceId.hashCode(), notification)
    }
    
    /**
     * Show a full-screen notification for task start
     * This works even when the app is killed
     */
    fun showTaskStartNotification(
        occurrenceId: String,
        taskId: String,
        taskTitle: String
    ) {
        // Create full-screen intent
        val fullScreenIntent = Intent(context, com.propentatech.moncoin.ui.screen.task.start.TaskStartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("occurrence_id", occurrenceId)
            putExtra("task_id", taskId)
            putExtra("task_title", taskTitle)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            occurrenceId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create notification with full-screen intent
        val notification = NotificationCompat.Builder(context, MonCoinApplication.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle)
            .setContentText("Il est temps de commencer")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()
        
        notificationManager.notify(occurrenceId.hashCode(), notification)
    }
    
    /**
     * Show a full-screen notification for task end
     * This works even when the app is killed
     */
    fun showTaskEndNotification(
        occurrenceId: String,
        taskId: String,
        taskTitle: String
    ) {
        // Create full-screen intent
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_OCCURRENCE_ID, occurrenceId)
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            occurrenceId.hashCode() + 1000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create notification with full-screen intent
        val notification = NotificationCompat.Builder(context, MonCoinApplication.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle)
            .setContentText("Temps écoulé !")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()
        
        notificationManager.notify(occurrenceId.hashCode() + 1000, notification)
    }
    
    /**
     * Cancel a notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
