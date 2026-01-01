package com.example.dailytasks.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = StatusEntity::class,
            parentColumns = ["id"],
            childColumns = ["statusId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("statusId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val shortName: String,
    val description: String,
    val difficulty: Int,
    val date: String,      // yyyy-MM-dd
    val startTime: String, // HH:mm
    val durationHours: Int,
    val statusId: Int,
    val location: String?
)

