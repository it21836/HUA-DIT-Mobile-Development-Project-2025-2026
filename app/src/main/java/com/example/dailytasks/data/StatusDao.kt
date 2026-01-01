package com.example.dailytasks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatusDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(statuses: List<StatusEntity>)

    @Query("SELECT * FROM statuses")
    suspend fun getAll(): List<StatusEntity>
}

