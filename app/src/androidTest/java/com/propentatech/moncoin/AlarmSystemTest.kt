package com.propentatech.moncoin

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.propentatech.moncoin.alarm.AlarmScheduler
import com.propentatech.moncoin.data.local.entity.OccurrenceEntity
import com.propentatech.moncoin.data.model.TaskState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Tests d'intégration pour le système d'alarmes
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AlarmSystemTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager

    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Test
    fun canScheduleExactAlarms_returnsTrue() {
        // Test que l'application peut planifier des alarmes exactes
        val canSchedule = alarmScheduler.canScheduleExactAlarms()
        assertTrue("L'application devrait pouvoir planifier des alarmes exactes", canSchedule)
    }

    @Test
    fun scheduleAlarm_forFutureTime_success() {
        // Given
        val futureTime = LocalDateTime.now().plusHours(2)
        val occurrence = OccurrenceEntity(
            id = "test-alarm-1",
            taskId = "task-1",
            startAt = futureTime.minusHours(1),
            endAt = futureTime,
            state = TaskState.SCHEDULED
        )

        // When
        try {
            alarmScheduler.scheduleAlarm(occurrence, "Test Alarm Task")
            // Si aucune exception n'est levée, le test passe
            assertTrue(true)
        } catch (e: Exception) {
            fail("La planification de l'alarme ne devrait pas échouer: ${e.message}")
        }
    }

    @Test
    fun scheduleStartAlarm_forFutureTime_success() {
        // Given
        val futureTime = LocalDateTime.now().plusHours(1)
        val occurrence = OccurrenceEntity(
            id = "test-start-alarm-1",
            taskId = "task-1",
            startAt = futureTime,
            endAt = futureTime.plusHours(1),
            state = TaskState.SCHEDULED
        )

        // When
        try {
            alarmScheduler.scheduleStartAlarm(occurrence, "Test Start Alarm")
            assertTrue(true)
        } catch (e: Exception) {
            fail("La planification de l'alarme de début ne devrait pas échouer: ${e.message}")
        }
    }

    @Test
    fun scheduleReminder_forFutureTime_success() {
        // Given
        val futureTime = LocalDateTime.now().plusHours(1)
        val occurrence = OccurrenceEntity(
            id = "test-reminder-1",
            taskId = "task-1",
            startAt = futureTime,
            endAt = futureTime.plusHours(1),
            state = TaskState.SCHEDULED
        )

        // When
        try {
            alarmScheduler.scheduleReminder(occurrence, "Test Reminder", 10)
            assertTrue(true)
        } catch (e: Exception) {
            fail("La planification du rappel ne devrait pas échouer: ${e.message}")
        }
    }

    @Test
    fun cancelAlarm_success() {
        // Given
        val occurrenceId = "test-cancel-alarm-1"

        // When
        try {
            alarmScheduler.cancelAlarm(occurrenceId)
            assertTrue(true)
        } catch (e: Exception) {
            fail("L'annulation de l'alarme ne devrait pas échouer: ${e.message}")
        }
    }

    @Test
    fun scheduleMultipleAlarms_success() {
        // Given
        val futureTime = LocalDateTime.now().plusHours(2)
        val occurrences = listOf(
            OccurrenceEntity(
                id = "multi-1",
                taskId = "task-1",
                startAt = futureTime,
                endAt = futureTime.plusHours(1),
                state = TaskState.SCHEDULED
            ),
            OccurrenceEntity(
                id = "multi-2",
                taskId = "task-2",
                startAt = futureTime.plusHours(3),
                endAt = futureTime.plusHours(4),
                state = TaskState.SCHEDULED
            ),
            OccurrenceEntity(
                id = "multi-3",
                taskId = "task-3",
                startAt = futureTime.plusHours(6),
                endAt = futureTime.plusHours(7),
                state = TaskState.SCHEDULED
            )
        )

        // When
        try {
            occurrences.forEach { occurrence ->
                alarmScheduler.scheduleAlarm(occurrence, "Multi Task ${occurrence.id}")
                alarmScheduler.scheduleStartAlarm(occurrence, "Multi Task ${occurrence.id}")
            }
            assertTrue(true)
        } catch (e: Exception) {
            fail("La planification de plusieurs alarmes ne devrait pas échouer: ${e.message}")
        }
    }
}
