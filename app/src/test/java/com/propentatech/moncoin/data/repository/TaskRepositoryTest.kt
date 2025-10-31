package com.propentatech.moncoin.data.repository

import com.propentatech.moncoin.data.local.dao.TaskDao
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskMode
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class TaskRepositoryTest {

    @Mock
    private lateinit var taskDao: TaskDao

    private lateinit var taskRepository: TaskRepository

    private val testTask = TaskEntity(
        id = "test-task-1",
        title = "Test Task",
        description = "Test Description",
        type = TaskType.PONCTUELLE,
        mode = TaskMode.PLAGE,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        alarmsEnabled = true,
        notificationsEnabled = true,
        state = TaskState.SCHEDULED
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        taskRepository = TaskRepository(taskDao)
    }

    @Test
    fun `insertTask should call dao insert`() = runTest {
        // When
        taskRepository.insertTask(testTask)

        // Then
        verify(taskDao).insertTask(testTask)
    }

    @Test
    fun `getTaskById should return task from dao`() = runTest {
        // Given
        `when`(taskDao.getTaskById(testTask.id)).thenReturn(testTask)

        // When
        val result = taskRepository.getTaskById(testTask.id)

        // Then
        assertEquals(testTask, result)
        verify(taskDao).getTaskById(testTask.id)
    }

    @Test
    fun `getAllTasks should return flow of tasks`() = runTest {
        // Given
        val tasks = listOf(testTask)
        `when`(taskDao.getAllTasks()).thenReturn(flowOf(tasks))

        // When
        val result = taskRepository.getAllTasks().first()

        // Then
        assertEquals(tasks, result)
        verify(taskDao).getAllTasks()
    }

    @Test
    fun `updateTaskState should call dao update`() = runTest {
        // When
        taskRepository.updateTaskState(testTask.id, TaskState.RUNNING)

        // Then
        verify(taskDao).updateTaskState(testTask.id, TaskState.RUNNING)
    }

    @Test
    fun `deleteTaskById should call dao delete`() = runTest {
        // When
        taskRepository.deleteTaskById(testTask.id)

        // Then
        verify(taskDao).deleteTaskById(testTask.id)
    }

    @Test
    fun `getTasksByType should return filtered tasks`() = runTest {
        // Given
        val tasks = listOf(testTask)
        `when`(taskDao.getTasksByType(TaskType.PONCTUELLE)).thenReturn(flowOf(tasks))

        // When
        val result = taskRepository.getTasksByType(TaskType.PONCTUELLE).first()

        // Then
        assertEquals(tasks, result)
        verify(taskDao).getTasksByType(TaskType.PONCTUELLE)
    }

    @Test
    fun `getTasksByState should return filtered tasks`() = runTest {
        // Given
        val tasks = listOf(testTask)
        `when`(taskDao.getTasksByState(TaskState.SCHEDULED)).thenReturn(flowOf(tasks))

        // When
        val result = taskRepository.getTasksByState(TaskState.SCHEDULED).first()

        // Then
        assertEquals(tasks, result)
        verify(taskDao).getTasksByState(TaskState.SCHEDULED)
    }
}
