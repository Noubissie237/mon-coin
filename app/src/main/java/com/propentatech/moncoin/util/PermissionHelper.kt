package com.propentatech.moncoin.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    /**
     * Check if exact alarms permission is granted (Android 12+)
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /**
     * Open settings to allow exact alarms (Android 12+)
     */
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
    
    /**
     * Check if battery optimization is disabled
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }
    
    /**
     * Request to disable battery optimization
     */
    fun requestDisableBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general battery optimization settings
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(fallbackIntent)
            }
        }
    }
    
    /**
     * Check all critical permissions for alarms
     */
    fun hasAllAlarmPermissions(context: Context): Boolean {
        return canScheduleExactAlarms(context) && isBatteryOptimizationDisabled(context)
    }
    
    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()
        
        if (!canScheduleExactAlarms(context)) {
            missing.add("Alarmes exactes")
        }
        
        if (!isBatteryOptimizationDisabled(context)) {
            missing.add("Optimisation de batterie désactivée")
        }
        
        return missing
    }
}
