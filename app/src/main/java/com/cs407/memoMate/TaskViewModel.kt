package com.cs407.memoMate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.Data.TaskDao
import kotlinx.coroutines.launch

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    // LiveData to observe all tasks
    val allTasks: LiveData<List<Task>> = liveData {
        emit(taskDao.getAllTasks())
    }

    // Insert a new task
    fun insertTask(task: Task) {
        viewModelScope.launch {
            taskDao.insertTask(task)
        }
    }

    // Delete a task by its object
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    // Delete a task by ID
    fun deleteTaskById(taskId: Int) {
        viewModelScope.launch {
            taskDao.deleteTaskById(taskId)
        }
    }

    // Get a task by ID
    fun getTaskById(taskId: Int): LiveData<Task?> = liveData {
        emit(taskDao.getTaskById(taskId))
    }

    // Get tasks within a specific date range
    fun getTasksWithinThreeDays(currentDate: String): LiveData<List<Task>> = liveData {
        emit(taskDao.getTasksWithinThreeDays(currentDate))
    }
}
