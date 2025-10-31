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
        val notification = NotificationCompat.Builder(context, MonCoinApplication.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rappel : $taskTitle")
            .setContentText("Commence dans $minutesBefore minutes")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
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
        val notification = NotificationCompat.Builder(context, MonCoinApplication.CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tâche manquée")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(occurrenceId.hashCode(), notification)
    }
    
    /**
     * Cancel a notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
