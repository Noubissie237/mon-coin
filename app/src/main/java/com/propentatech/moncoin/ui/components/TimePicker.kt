package com.propentatech.moncoin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    var hourText by remember { mutableStateOf(String.format("%02d", initialTime.hour)) }
    var minuteText by remember { mutableStateOf(String.format("%02d", initialTime.minute)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { 
                            selectedHour = if (selectedHour < 23) selectedHour + 1 else 0
                            hourText = String.format("%02d", selectedHour)
                        }) {
                            Text("▲")
                        }
                        
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { newValue ->
                                if (newValue.length <= 2) {
                                    hourText = newValue.filter { it.isDigit() }
                                    val hour = hourText.toIntOrNull()
                                    if (hour != null && hour in 0..23) {
                                        selectedHour = hour
                                    }
                                }
                            },
                            modifier = Modifier.width(70.dp),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        IconButton(onClick = { 
                            selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                            hourText = String.format("%02d", selectedHour)
                        }) {
                            Text("▼")
                        }
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minute picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { 
                            selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                            minuteText = String.format("%02d", selectedMinute)
                        }) {
                            Text("▲")
                        }
                        
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { newValue ->
                                if (newValue.length <= 2) {
                                    minuteText = newValue.filter { it.isDigit() }
                                    val minute = minuteText.toIntOrNull()
                                    if (minute != null && minute in 0..59) {
                                        selectedMinute = minute
                                    }
                                }
                            },
                            modifier = Modifier.width(70.dp),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        IconButton(onClick = { 
                            selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                            minuteText = String.format("%02d", selectedMinute)
                        }) {
                            Text("▼")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tapez directement ou utilisez les flèches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Ensure values are valid before confirming
                val finalHour = selectedHour.coerceIn(0, 23)
                val finalMinute = selectedMinute.coerceIn(0, 59)
                onConfirm(LocalTime.of(finalHour, finalMinute))
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
