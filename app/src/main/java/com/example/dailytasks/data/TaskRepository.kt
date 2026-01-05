package com.example.dailytasks.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository private constructor(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()

    suspend fun addTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.insert(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.update(task)
    }

    suspend fun getPendingOrdered(): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.getPendingOrdered()
    }

    suspend fun getById(id: Int): TaskEntity? = withContext(Dispatchers.IO) {
        taskDao.getById(id)
    }

    suspend fun deleteOld(dateToday: String) = withContext(Dispatchers.IO) {
        taskDao.deleteOlderThan(dateToday)
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun get(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

