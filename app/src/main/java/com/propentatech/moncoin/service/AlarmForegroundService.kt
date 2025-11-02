package com.propentatech.moncoin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.propentatech.moncoin.MainActivity
import com.propentatech.moncoin.MonCoinApplication
import com.propentatech.moncoin.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Service en premier plan qui garantit que l'application reste active en arrière-plan
 * pour déclencher les alarmes même si le téléphone est éteint/rallumé
 */
@AndroidEntryPoint
class AlarmForegroundService : Service() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AlarmForegroundService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmForegroundService started")
        
        // Créer la notification pour le service en premier plan
        val notification = createNotification()
        
        // Démarrer en premier plan
        startForeground(NOTIFICATION_ID, notification)
        
        // START_STICKY garantit que le service redémarre après avoir été tué
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotification(): Notification {
        // Créer le canal de notification si nécessaire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Service d'alarmes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintient l'application active pour les alarmes"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        // Intent pour ouvrir l'app au clic
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Créer la notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MonCoin actif")
            .setContentText("Surveillance des alarmes en cours")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Ne peut pas être balayée
            .setShowWhen(false)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AlarmForegroundService destroyed")
    }
    
    companion object {
        private const val TAG = "AlarmForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "alarm_foreground_service"
    }
}
