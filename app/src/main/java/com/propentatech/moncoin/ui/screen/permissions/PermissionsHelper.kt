package com.propentatech.moncoin.ui.screen.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Helper pour gérer toutes les permissions nécessaires au bon fonctionnement de l'app
 */
object PermissionsHelper {
    
    /**
     * Vérifie si toutes les permissions critiques sont accordées
     */
    fun hasAllCriticalPermissions(context: Context): Boolean {
        return hasNotificationPermission(context) &&
               hasExactAlarmPermission(context) &&
               hasFullScreenIntentPermission(context) &&
               isBatteryOptimizationDisabled(context)
    }
    
    /**
     * Vérifie la permission de notifications (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Pas besoin sur les versions antérieures
        }
    }
    
    /**
     * Vérifie la permission d'alarmes exactes (Android 12+)
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Pas besoin sur les versions antérieures
        }
    }
    
    /**
     * Vérifie la permission de full-screen intent (Android 14+)
     */
    fun hasFullScreenIntentPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.canUseFullScreenIntent()
        } else {
            true // Accordée automatiquement sur les versions antérieures
        }
    }
    
    /**
     * Vérifie si l'optimisation de batterie est désactivée
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    
    /**
     * Ouvre les paramètres pour autoriser les alarmes exactes
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun openExactAlarmSettings(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
    
    /**
     * Ouvre les paramètres pour autoriser les full-screen intents
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun openFullScreenIntentSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
    
    /**
     * Ouvre les paramètres pour désactiver l'optimisation de batterie
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
    
    /**
     * Ouvre les paramètres de notifications de l'app
     */
    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }
    
    /**
     * Liste des permissions manquantes avec leurs descriptions
     */
    fun getMissingPermissions(context: Context): List<PermissionInfo> {
        val missing = mutableListOf<PermissionInfo>()
        
        if (!hasNotificationPermission(context)) {
            missing.add(
                PermissionInfo(
                    name = "Notifications",
                    description = "Nécessaire pour afficher les alarmes et rappels",
                    isCritical = true,
                    action = { openNotificationSettings(context) }
                )
            )
        }
        
        if (!hasExactAlarmPermission(context)) {
            missing.add(
                PermissionInfo(
                    name = "Alarmes exactes",
                    description = "Permet de déclencher les alarmes à l'heure précise",
                    isCritical = true,
                    action = { 
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            openExactAlarmSettings(context)
                        }
                    }
                )
            )
        }
        
        if (!hasFullScreenIntentPermission(context)) {
            missing.add(
                PermissionInfo(
                    name = "Alarmes plein écran",
                    description = "Permet d'afficher les alarmes en plein écran même si l'écran est verrouillé",
                    isCritical = true,
                    action = { 
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            openFullScreenIntentSettings(context)
                        }
                    }
                )
            )
        }
        
        if (!isBatteryOptimizationDisabled(context)) {
            missing.add(
                PermissionInfo(
                    name = "Optimisation de batterie",
                    description = "Désactiver pour que l'app fonctionne en arrière-plan",
                    isCritical = true,
                    action = { openBatteryOptimizationSettings(context) }
                )
            )
        }
        
        return missing
    }
}

data class PermissionInfo(
    val name: String,
    val description: String,
    val isCritical: Boolean,
    val action: () -> Unit
)
