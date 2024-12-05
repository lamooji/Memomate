import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.Data.TaskDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.TaskViewModel
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import java.util.Date


@RunWith(AndroidJUnit4::class)
class NoteViewModelTest {

    private lateinit var noteDatabase: NoteDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var taskViewModel: TaskViewModel

    @Before
    fun setUp() {
        noteDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoteDatabase::class.java
        ).allowMainThreadQueries().build()
        taskDao = noteDatabase.taskDao()
    }

    @After
    fun tearDown() {
        if (::noteDatabase.isInitialized) {
            noteDatabase.close()
        }
    }

    @Test
    fun insertTaskAndRetrieveIt() = runBlocking {
        // Create a task
        val task = Task(
            noteId = 0, // ID will auto-generate
            significance = 10,
            ddl = Date(), // Current date
            finished = false,
            noteTitle = "Test Task",
            noteAbstract = "This is a test task."
        )

        // Insert the task using ViewModel
        val id = taskDao.insertTask(task)
        assertTrue(id > 0)

        val retrievedTask = taskDao.getTaskById(id.toInt())
        assertNotNull(retrievedTask)
        assertEquals(task.noteTitle, retrievedTask?.noteTitle)
    }

    @Test
    fun deleteTask() = runBlocking {
        // Insert a task
        val task = Task(
            noteId = 0,
            significance = 5,
            ddl = Date(),
            finished = false,
            noteTitle = "Task to Delete",
            noteAbstract = "This will be deleted."
        )

        val generatedId = taskDao.insertTask(task)

        val taskWithId = task.copy(noteId = generatedId.toInt())

        taskDao.deleteTask(taskWithId)

        // Retrieve all tasks and assert none exist
        val tasks = taskDao.getAllTasks()
        println("Tasks in database: $tasks")
        Log.d("TestLog", "Tasks in database: $task")

        assertTrue(tasks.isEmpty())
    }
}
