package com.example.dailytasks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query(
        """
        SELECT * FROM tasks
        WHERE statusId != :completedId
        ORDER BY 
            CASE statusId 
                WHEN :expiredId THEN 1
                WHEN :inProgressId THEN 2
                WHEN :recordedId THEN 3
                ELSE 4
            END,
            startTime ASC
        """
    )
    suspend fun getPendingOrdered(
        expiredId: Int = StatusDefaults.EXPIRED,
        inProgressId: Int = StatusDefaults.IN_PROGRESS,
        recordedId: Int = StatusDefaults.RECORDED,
        completedId: Int = StatusDefaults.COMPLETED
    ): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE uid = :id LIMIT 1")
    suspend fun getById(id: Int): TaskEntity?

    @Query("DELETE FROM tasks WHERE date < :today")
    suspend fun deleteOlderThan(today: String)

    @Query("SELECT * FROM tasks WHERE statusId != :completedId")
    suspend fun getAllNotCompleted(
        completedId: Int = StatusDefaults.COMPLETED
    ): List<TaskEntity>
}

