package com.propentatech.moncoin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    initialTime: LocalTime = LocalTime.now()
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { 
                        selectedHour = if (selectedHour < 23) selectedHour + 1 else 0 
                    }) {
                        Text("▲")
                    }
                    Text(
                        text = String.format("%02d", selectedHour),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { 
                        selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 
                    }) {
                        Text("▼")
                    }
                }
                
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Minute picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { 
                        selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0 
                    }) {
                        Text("▲")
                    }
                    Text(
                        text = String.format("%02d", selectedMinute),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { 
                        selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59 
                    }) {
                        Text("▼")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(selectedHour, selectedMinute))
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
