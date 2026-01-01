package com.example.dailytasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey val id: Int,
    val name: String
)

object StatusDefaults {
    const val RECORDED = 1
    const val IN_PROGRESS = 2
    const val EXPIRED = 3
    const val COMPLETED = 4

    val predefined = listOf(
        StatusEntity(RECORDED, "recorded"),
        StatusEntity(IN_PROGRESS, "in-progress"),
        StatusEntity(EXPIRED, "expired"),
        StatusEntity(COMPLETED, "completed")
    )
}

