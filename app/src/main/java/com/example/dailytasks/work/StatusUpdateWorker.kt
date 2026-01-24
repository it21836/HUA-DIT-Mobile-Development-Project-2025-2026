package com.example.dailytasks.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dailytasks.data.AppDatabase
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class StatusUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db = AppDatabase.getInstance(appContext)
    private val taskDao = db.taskDao()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun doWork(): Result {
        val today = LocalDate.now()

        // Purge old tasks
        taskDao.deleteOlderThan(today.toString())

        val tasks = taskDao.getAllNotCompleted()
        val nowTime = LocalTime.now()

        tasks.forEach { task ->
            if (task.date != today.toString()) {
                // Leave as is for future dates
                return@forEach
            }
            val start = runCatching { LocalTime.parse(task.startTime, timeFormatter) }.getOrNull()
                ?: return@forEach
            val end = start.plusHours(task.durationHours.toLong())

            val newStatus = when {
                nowTime >= end -> StatusDefaults.EXPIRED
                nowTime >= start -> StatusDefaults.IN_PROGRESS
                else -> StatusDefaults.RECORDED
            }
            if (newStatus != task.statusId) {
                taskDao.update(task.copy(statusId = newStatus))
            }
        }

        return Result.success()
    }
}
