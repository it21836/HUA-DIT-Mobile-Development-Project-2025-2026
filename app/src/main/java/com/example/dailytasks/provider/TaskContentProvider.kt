package com.example.dailytasks.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.dailytasks.data.AppDatabase
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskEntity
import kotlinx.coroutines.runBlocking

class TaskContentProvider : ContentProvider() {

    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(TaskContract.AUTHORITY, TaskContract.TASKS_TABLE, TASKS)
        addURI(TaskContract.AUTHORITY, "${TaskContract.TASKS_TABLE}/#", TASK_ID)
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = AppDatabase.getInstance(requireNotNull(context))
        val readable = db.openHelper.readableDatabase

        return when (matcher.match(uri)) {
            TASKS -> readable.query(
                "SELECT * FROM tasks ${selectionClause(selection)}",
                selectionArgs ?: emptyArray()
            )
            TASK_ID -> {
                val id = ContentUris.parseId(uri)
                readable.query(
                    "SELECT * FROM tasks WHERE uid = ?",
                    arrayOf(id.toString())
                )
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? = when (matcher.match(uri)) {
        TASKS -> "vnd.android.cursor.dir/vnd.${TaskContract.AUTHORITY}.${TaskContract.TASKS_TABLE}"
        TASK_ID -> "vnd.android.cursor.item/vnd.${TaskContract.AUTHORITY}.${TaskContract.TASKS_TABLE}"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (matcher.match(uri) != TASKS) return null
        val context = context ?: return null
        val db = AppDatabase.getInstance(context)
        val dao = db.taskDao()

        val entity = values?.toTaskEntity() ?: return null
        val id = runBlocking { dao.insert(entity) }
        return ContentUris.withAppendedId(TaskContract.TASKS_URI, id)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val context = context ?: return 0
        val db = AppDatabase.getInstance(context)
        val dao = db.taskDao()
        return when (matcher.match(uri)) {
            TASK_ID -> {
                val id = ContentUris.parseId(uri).toInt()
                val existing = runBlocking { dao.getById(id) } ?: return 0
                runBlocking { dao.delete(existing) }
                1
            }
            else -> 0
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val context = context ?: return 0
        val db = AppDatabase.getInstance(context)
        val dao = db.taskDao()
        return when (matcher.match(uri)) {
            TASK_ID -> {
                val id = ContentUris.parseId(uri).toInt()
                val existing = runBlocking { dao.getById(id) } ?: return 0
                val updated = values?.mergeInto(existing) ?: return 0
                runBlocking { dao.update(updated) }
                1
            }
            else -> 0
        }
    }

    private fun selectionClause(selection: String?): String =
        if (selection.isNullOrBlank()) "" else "WHERE $selection"

    private fun ContentValues.toTaskEntity(): TaskEntity? {
        val shortName = getAsString("shortName") ?: return null
        val description = getAsString("description") ?: ""
        val difficulty = getAsInteger("difficulty") ?: 0
        val date = getAsString("date") ?: return null
        val startTime = getAsString("startTime") ?: return null
        val duration = getAsInteger("durationHours") ?: 1
        val statusId = getAsInteger("statusId") ?: StatusDefaults.RECORDED
        val location = getAsString("location")

        return TaskEntity(
            uid = 0,
            shortName = shortName,
            description = description,
            difficulty = difficulty,
            date = date,
            startTime = startTime,
            durationHours = duration,
            statusId = statusId,
            location = location
        )
    }

    private fun ContentValues.mergeInto(old: TaskEntity): TaskEntity {
        return old.copy(
            shortName = getAsString("shortName") ?: old.shortName,
            description = getAsString("description") ?: old.description,
            difficulty = getAsInteger("difficulty") ?: old.difficulty,
            date = getAsString("date") ?: old.date,
            startTime = getAsString("startTime") ?: old.startTime,
            durationHours = getAsInteger("durationHours") ?: old.durationHours,
            statusId = getAsInteger("statusId") ?: old.statusId,
            location = getAsString("location") ?: old.location
        )
    }

    companion object {
        private const val TASKS = 1
        private const val TASK_ID = 2
    }
}
