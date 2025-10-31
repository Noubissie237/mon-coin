package com.propentatech.moncoin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.propentatech.moncoin.worker.DailyOccurrenceWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MonCoinApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleDailyOccurrenceWorker()
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    /**
     * Planifie le worker qui s'exécute chaque jour à minuit
     * pour créer les occurrences du jour et mettre à jour les statuts
     */
    private fun scheduleDailyOccurrenceWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false) // Peut s'exécuter même avec batterie faible
            .build()
        
        // Calculer le délai jusqu'à minuit
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val delayUntilMidnight = Duration.between(now, midnight).toMillis()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyOccurrenceWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(delayUntilMidnight, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag("daily_occurrence_worker")
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyOccurrenceGeneration",
            ExistingPeriodicWorkPolicy.KEEP, // Garder le worker existant si déjà planifié
            dailyWorkRequest
        )
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
