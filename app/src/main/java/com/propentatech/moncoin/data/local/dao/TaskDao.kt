package com.propentatech.moncoin.data.local.dao

import androidx.room.*
import com.propentatech.moncoin.data.local.entity.TaskEntity
import com.propentatech.moncoin.data.model.TaskState
import com.propentatech.moncoin.data.model.TaskType
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasksOnce(): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskByIdFlow(taskId: String): Flow<TaskEntity?>
    
    @Query("SELECT * FROM tasks WHERE state = :state ORDER BY createdAt DESC")
    fun getTasksByState(state: TaskState): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE type = :type ORDER BY createdAt DESC")
    fun getTasksByType(type: TaskType): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE state IN (:states) ORDER BY priority DESC, createdAt DESC")
    fun getTasksByStates(states: List<TaskState>): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    @Query("UPDATE tasks SET state = :state WHERE id = :taskId")
    suspend fun updateTaskState(taskId: String, state: TaskState)
    
    @Query("SELECT COUNT(*) FROM tasks WHERE state = :state")
    fun getTaskCountByState(state: TaskState): Flow<Int>
}
