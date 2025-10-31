package com.propentatech.moncoin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MonCoinApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_REMINDER,
                    "Rappels",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications de rappel avant les tâches"
                },
                NotificationChannel(
                    CHANNEL_ALARM,
                    "Alarmes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarmes sonores de fin de tâche"
                    setSound(null, null) // Custom sound will be handled
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "Système",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications système et services en arrière-plan"
                }
            )
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }
    
    companion object {
        const val CHANNEL_REMINDER = "reminder_channel"
        const val CHANNEL_ALARM = "alarm_channel"
        const val CHANNEL_SYSTEM = "system_channel"
    }
}
