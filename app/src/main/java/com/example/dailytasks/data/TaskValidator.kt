package com.example.dailytasks.data

object TaskValidator {

    fun validate(
        shortName: String,
        description: String,
        difficulty: Int,
        durationHours: Int
    ): List<String> {
        val errors = mutableListOf<String>()

        if (shortName.isBlank()) errors += "Short name is required"
        if (shortName.length > 20) errors += "Short name max 20 chars"
        if (description.length > 150) errors += "Description max 150 chars"
        if (difficulty !in 0..10) errors += "Difficulty must be 0..10"
        if (durationHours <= 0) errors += "Duration must be > 0"

        return errors
    }
}

