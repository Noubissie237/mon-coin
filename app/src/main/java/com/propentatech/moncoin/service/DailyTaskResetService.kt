package com.propentatech.moncoin.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.propentatech.moncoin.data.model.TaskMode
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * Service qui réinitialise automatiquement le statut des tâches flexibles chaque jour
 */
@AndroidEntryPoint
class DailyTaskResetService : Service() {
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    private var lastResetDate: LocalDate? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DailyTaskResetService started")
        startMonitoring()
        return START_STICKY
    }
    
    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkAndResetTasks()
                    delay(60000) // Vérifier chaque minute
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking tasks", e)
                }
            }
        }
    }
    
    private suspend fun checkAndResetTasks() {
        val today = LocalDate.now()
        
        // Si c'est un nouveau jour et qu'on n'a pas encore fait le reset aujourd'hui
        if (lastResetDate == null || lastResetDate != today) {
            Log.d(TAG, "New day detected, resetting flexible tasks")
            
            // Récupérer toutes les tâches flexibles terminées
            val allTasks = taskRepository.getAllTasksOnce()
            val flexibleCompletedTasks = allTasks.filter { task ->
                task.mode == TaskMode.FLEXIBLE && task.state == TaskState.COMPLETED
            }
            
            // Réinitialiser leur statut à SCHEDULED
            flexibleCompletedTasks.forEach { task ->
                taskRepository.updateTaskState(task.id, TaskState.SCHEDULED)
                Log.d(TAG, "Reset task: ${task.title}")
            }
            
            lastResetDate = today
            Log.d(TAG, "Reset completed for ${flexibleCompletedTasks.size} flexible tasks")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DailyTaskResetService destroyed")
        monitorJob?.cancel()
        serviceScope.cancel()
    }
    
    companion object {
        private const val TAG = "DailyTaskResetService"
    }
}
