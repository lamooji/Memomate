package com.cs407.memoMate

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.Data.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DummyDataInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false
        val taskDao = NoteDatabase.getDatabase(context).taskDao()

        CoroutineScope(Dispatchers.IO).launch {
            if (taskDao.getAllTasks().isEmpty()) {
                populateDatabaseWithDummyData(taskDao)
                Log.d("DummyDataInitializer", "Dummy data populated in the database.")
            } else {
                Log.d("DummyDataInitializer", "Database already contains data.")
            }
        }
        return true
    }

    suspend fun populateDatabaseWithDummyData(taskDao: TaskDao) {
        val baseDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 10)
        }.time

        val dummyTasks = listOf(
            Task(
                noteId = 0, // Auto-generate ID
                noteTitle = "High Priority Task",
                noteAbstract = "This is a high-priority task description.",
                ddl = formatDate(baseDate, 2), // 2 days from now
                significance = 3,
                finished = false,
                importance = 3
            ),
            Task(
                noteId = 0,
                noteTitle = "Medium Priority Task",
                noteAbstract = "This is a medium-priority task description.",
                ddl = formatDate(baseDate, 5), // 5 days from now
                significance = 2,
                finished = false,
                importance = 2
            ),
            Task(
                noteId = 0,
                noteTitle = "Low Priority Task",
                noteAbstract = "This is a low-priority task description.",
                ddl = formatDate(baseDate, 10), // 10 days from now
                significance = 1,
                finished = false,
                importance = 1
            ),
            Task(
                noteId = 0,
                noteTitle = "Completed Task",
                noteAbstract = "This task is already finished.",
                ddl = formatDate(baseDate, -1), // 1 day ago
                significance = 3,
                finished = true,
                importance = 3
            ),
            Task(
                noteId = 0,
                noteTitle = "Future High Priority Task",
                noteAbstract = "This task has a far-off deadline.",
                ddl = formatDate(baseDate, 30), // 30 days later
                significance = 3,
                finished = false,
                importance = 3
            ),
            Task(
                noteId = 0,
                noteTitle = "Future Medium Priority Task",
                noteAbstract = "This task has a medium deadline.",
                ddl = formatDate(baseDate, 21), // 21 days later
                significance = 2,
                finished = false,
                importance = 2
            ),
            Task(
                noteId = 0,
                noteTitle = "Future Low Priority Task",
                noteAbstract = "This task has a far-off deadline.",
                ddl = formatDate(baseDate, 30), // 30 days later
                significance = 1,
                finished = false,
                importance = 1
            )
        )

        dummyTasks.forEach { task ->
            taskDao.insertTask(task)
        }

        Log.d("DummyDataInitializer", "Inserted ${dummyTasks.size} dummy tasks into the database.")
    }

    private fun formatDate(baseDate: Date, daysToAdd: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = baseDate
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return sdf.format(calendar.time)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun getType(uri: Uri): String? = null
}

