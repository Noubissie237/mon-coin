package com.propentatech.moncoin.data.repository

import com.propentatech.moncoin.data.local.dao.TaskDao
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    
    suspend fun getTaskById(taskId: String): TaskEntity? = taskDao.getTaskById(taskId)
    
    fun getTaskByIdFlow(taskId: String): Flow<TaskEntity?> = taskDao.getTaskByIdFlow(taskId)
    
    fun getTasksByState(state: TaskState): Flow<List<TaskEntity>> = taskDao.getTasksByState(state)
    
    fun getTasksByType(type: TaskType): Flow<List<TaskEntity>> = taskDao.getTasksByType(type)
    
    fun getTasksByStates(states: List<TaskState>): Flow<List<TaskEntity>> = 
        taskDao.getTasksByStates(states)
    
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    
    suspend fun insertTasks(tasks: List<TaskEntity>) = taskDao.insertTasks(tasks)
    
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    
    suspend fun deleteTaskById(taskId: String) = taskDao.deleteTaskById(taskId)
    
    suspend fun updateTaskState(taskId: String, state: TaskState) = 
        taskDao.updateTaskState(taskId, state)
    
    fun getTaskCountByState(state: TaskState): Flow<Int> = taskDao.getTaskCountByState(state)
}
