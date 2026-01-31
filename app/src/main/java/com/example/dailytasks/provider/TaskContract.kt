package com.example.dailytasks.provider

import android.net.Uri

object TaskContract {
    const val AUTHORITY = "com.example.dailytasks.provider"
    const val TASKS_TABLE = "tasks"

    val BASE_URI: Uri = Uri.parse("content://$AUTHORITY")
    val TASKS_URI: Uri = BASE_URI.buildUpon().appendPath(TASKS_TABLE).build()
}
