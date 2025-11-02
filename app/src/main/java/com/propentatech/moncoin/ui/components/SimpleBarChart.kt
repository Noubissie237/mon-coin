package com.propentatech.moncoin.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun SimpleBarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null
) {
    if (data.isEmpty()) return
    
    val actualMaxValue = maxValue ?: data.maxOf { it.value }
    
    Column(modifier = modifier) {
        // Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Value text
                    Text(
                        text = item.value.toInt().toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.color
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Bar
                    Canvas(
                        modifier = Modifier
                            .width(40.dp)
                            .height(
                                if (actualMaxValue > 0) {
                                    ((item.value / actualMaxValue) * 180).dp
                                } else 0.dp
                            )
                    ) {
                        drawRect(
                            color = item.color,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SimplePieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie chart
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            var startAngle = -90f
            data.forEach { item ->
                val sweepAngle = (item.value / total) * 360f
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }
        }
        
        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawRect(
                            color = item.color,
                            size = Size(size.width, size.height)
                        )
                    }
                    Text(
                        text = "${item.label}: ${item.value.toInt()} (${String.format("%.1f", (item.value / total) * 100)}%)",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
