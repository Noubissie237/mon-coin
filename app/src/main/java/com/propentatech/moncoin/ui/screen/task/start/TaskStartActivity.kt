package com.propentatech.moncoin.ui.screen.task.start

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.ui.theme.MonCoinTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskStartActivity : ComponentActivity() {
    
    @Inject
    lateinit var occurrenceRepository: OccurrenceRepository
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        val occurrenceId = intent.getStringExtra("occurrence_id") ?: run {
            finish()
            return
        }
        
        val taskTitle = intent.getStringExtra("task_title") ?: "Tâche"
        
        // Start alarm sound and vibration
        startAlarm()
        
        setContent {
            MonCoinTheme {
                TaskStartScreen(
                    taskTitle = taskTitle,
                    onStart = {
                        stopAlarm()
                        lifecycleScope.launch {
                            occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.RUNNING)
                            finish()
                        }
                    },
                    onCancel = {
                        stopAlarm()
                        lifecycleScope.launch {
                            occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.CANCELLED)
                            finish()
                        }
                    },
                    onDismiss = {
                        stopAlarm()
                        finish()
                    }
                )
            }
        }
    }
    
    private fun startAlarm() {
        try {
            // Start sound
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@TaskStartActivity, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            
            // Start vibration
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        
        vibrator?.cancel()
        vibrator = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}

@Composable
fun TaskStartScreen(
    taskTitle: String,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Il est temps de commencer",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Commencer", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Annuler", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fermer", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
