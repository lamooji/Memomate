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
import java.util.Calendar
import java.util.Date

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

    public suspend fun populateDatabaseWithDummyData(taskDao: TaskDao) {
        val baseDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 10)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val dummyTasks = listOf(
            Task(
                noteId = 0, // Auto-generate ID
                noteTitle = "High Priority Task",
                noteAbstract = "This is a high priority task description.",
                ddl = Date(System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000)), // 2 days from now
                significance = 3,
                finished = false
            ),
            Task(
                noteId = 0,
                noteTitle = "Medium Priority Task",
                noteAbstract = "This is a medium priority task description.",
                ddl = Date(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)), // 5 days from now
                significance = 2,
                finished = false
            ),
            Task(
                noteId = 0,
                noteTitle = "Low Priority Task",
                noteAbstract = "This is a low priority task description.",
                ddl = Date(System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000)), // 10 days from now
                significance = 1,
                finished = false
            ),
            Task(
                noteId = 0,
                noteTitle = "Completed Task",
                noteAbstract = "This task is already finished.",
                ddl = Date(System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)), // 1 day ago
                significance = 3,
                finished = false
            ),
            Task(
                noteId = 0,
                noteTitle = "high Task",
                noteAbstract = "This task does not wanted to be finished.",
                ddl  = Calendar.getInstance().apply {
                    time = baseDate
                    add(Calendar.DAY_OF_MONTH, 27)
                }.time, // 30 days later
                significance = 3,
                finished = false
            ),
            Task(
                noteId = 0,
                noteTitle = "high Task11",
                noteAbstract = "This task is not finished.",
                ddl = Calendar.getInstance().apply {
                    time = baseDate
                    add(Calendar.DAY_OF_MONTH, 22)
                }.time, // 21 days later
                significance = 1,
                finished = false
            )
            ,
            Task(
                noteId = 0,
                noteTitle = "high Task11",
                noteAbstract = "This task is not yet finished.",
                ddl = Calendar.getInstance().apply {
                    time = baseDate
                    add(Calendar.DAY_OF_MONTH, 30)
                }.time, // 20 days later
                significance = 2,
                finished = false
            )


        )

        dummyTasks.forEach { task ->
            taskDao.insertTask(task)
        }

        Log.d("DummyDataInitializer", "Inserted ${dummyTasks.size} dummy tasks into the database.")
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
