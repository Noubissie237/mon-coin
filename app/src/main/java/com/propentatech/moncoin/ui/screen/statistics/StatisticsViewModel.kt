package com.propentatech.moncoin.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

data class DailyStats(
    val dayName: String,
    val completed: Int,
    val missed: Int,
    val total: Int
)

data class StatisticsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val missedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val completionRate: Float = 0f,
    val totalTimeMinutes: Long = 0,
    val averageTimePerTask: Long = 0,
    val dailyStats: List<DailyStats> = emptyList(),
    val interpretation: String = "",
    val isLoading: Boolean = true,
    val period: StatisticsPeriod = StatisticsPeriod.ALL
)

enum class StatisticsPeriod {
    WEEK, MONTH, ALL
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val occurrenceRepository: OccurrenceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    fun setPeriod(period: StatisticsPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val startOfToday = now.toLocalDate().atStartOfDay()
            val endOfToday = now.toLocalDate().atTime(23, 59, 59)
            
            val (startDate, endDate) = when (_uiState.value.period) {
                StatisticsPeriod.WEEK -> Pair(now.minusWeeks(1), now.plusYears(10))
                StatisticsPeriod.MONTH -> Pair(now.minusMonths(1), now.plusYears(10))
                StatisticsPeriod.ALL -> Pair(LocalDateTime.of(2000, 1, 1, 0, 0), now.plusYears(10))
            }
            
            // Récupérer toutes les occurrences (passées, présentes et futures)
            occurrenceRepository.getOccurrencesBetween(startDate, endDate)
                .collect { allOccurrences ->
                    // Filtrer les occurrences selon la période
                    val occurrences = when (_uiState.value.period) {
                        StatisticsPeriod.ALL -> allOccurrences
                        else -> allOccurrences.filter { it.startAt >= startDate }
                    }
                    
                    val completed = occurrences.count { it.state == TaskState.COMPLETED }
                    val missed = occurrences.count { it.state == TaskState.MISSED }
                    
                    // "En attente" = tâches SCHEDULED d'aujourd'hui uniquement
                    val pending = occurrences.count { 
                        it.state == TaskState.SCHEDULED && 
                        it.startAt >= startOfToday && 
                        it.startAt <= endOfToday
                    }
                    
                    val total = occurrences.size
                    
                    val completionRate = if (total > 0) {
                        (completed.toFloat() / total.toFloat()) * 100
                    } else 0f
                    
                    // Calculate total time for completed tasks
                    val totalMinutes = occurrences
                        .filter { it.state == TaskState.COMPLETED }
                        .sumOf { ChronoUnit.MINUTES.between(it.startAt, it.endAt) }
                    
                    val avgTime = if (completed > 0) totalMinutes / completed else 0
                    
                    // Calculate daily statistics
                    val dailyStats = calculateDailyStats(occurrences)
                    
                    // Generate interpretation
                    val interpretation = generateInterpretation(
                        total, completed, missed, completionRate, dailyStats
                    )
                    
                    _uiState.value = StatisticsUiState(
                        totalTasks = total,
                        completedTasks = completed,
                        missedTasks = missed,
                        pendingTasks = pending,
                        completionRate = completionRate,
                        totalTimeMinutes = totalMinutes,
                        averageTimePerTask = avgTime,
                        dailyStats = dailyStats,
                        interpretation = interpretation,
                        isLoading = false,
                        period = _uiState.value.period
                    )
                }
        }
    }
    
    private fun calculateDailyStats(occurrences: List<OccurrenceEntity>): List<DailyStats> {
        val dayOrder = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
        
        val statsByDay = occurrences.groupBy { it.startAt.dayOfWeek }
        
        return dayOrder.map { day ->
            val dayOccurrences = statsByDay[day] ?: emptyList()
            DailyStats(
                dayName = day.getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                completed = dayOccurrences.count { it.state == TaskState.COMPLETED },
                missed = dayOccurrences.count { it.state == TaskState.MISSED },
                total = dayOccurrences.size
            )
        }
    }
    
    private fun generateInterpretation(
        total: Int,
        completed: Int,
        missed: Int,
        completionRate: Float,
        dailyStats: List<DailyStats>
    ): String {
        if (total == 0) {
            return "Aucune tâche enregistrée pour le moment. Commencez à créer des tâches pour voir vos statistiques !"
        }
        
        val interpretation = buildString {
            // Performance globale
            when {
                completionRate >= 80 -> append("🎉 Excellente performance ! ")
                completionRate >= 60 -> append("👍 Bonne performance ! ")
                completionRate >= 40 -> append("💪 Performance moyenne. ")
                else -> append("⚠️ Performance à améliorer. ")
            }
            
            append("Vous avez complété $completed tâches sur $total")
            if (missed > 0) {
                append(", avec $missed tâche${if (missed > 1) "s" else ""} manquée${if (missed > 1) "s" else ""}")
            }
            append(".\n\n")
            
            // Analyse par jour
            val bestDay = dailyStats.maxByOrNull { 
                if (it.total > 0) (it.completed.toFloat() / it.total) else 0f 
            }
            val worstDay = dailyStats.filter { it.total > 0 }.minByOrNull { 
                (it.completed.toFloat() / it.total)
            }
            
            if (bestDay != null && bestDay.total > 0) {
                val rate = (bestDay.completed.toFloat() / bestDay.total * 100).toInt()
                append("Votre meilleur jour : ${bestDay.dayName} ($rate% de réussite).\n")
            }
            
            if (worstDay != null && worstDay != bestDay) {
                val rate = (worstDay.completed.toFloat() / worstDay.total * 100).toInt()
                append("Jour à améliorer : ${worstDay.dayName} ($rate% de réussite).\n")
            }
            
            // Conseil
            append("\nConseil : ")
            when {
                completionRate >= 80 -> append("Continuez sur cette lancée ! Votre discipline est exemplaire.")
                completionRate >= 60 -> append("Bon travail ! Essayez de maintenir cette régularité.")
                completionRate >= 40 -> append("Vous pouvez faire mieux ! Concentrez-vous sur vos priorités.")
                else -> append("Commencez par de petites tâches pour reprendre confiance.")
            }
        }
        
        return interpretation
    }
}
