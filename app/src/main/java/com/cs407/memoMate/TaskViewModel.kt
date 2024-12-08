import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.Data.TaskDao
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    // Helper function to check if a task is urgent (due within the next 7 days)
    private fun isUrgent(ddl: String): Boolean {
        val sdf = SimpleDateFormat("MM/dd", Locale.US)
        return try {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            dueDate.time = sdf.parse(ddl) ?: return false

            // Calculate 7 days from now
            val nextWeek = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
            }

            // Check if due date is within the next 7 days
            dueDate.time <= nextWeek.time
        } catch (e: Exception) {
            false // Return false if date parsing fails
        }
    }

    // Fetch urgent and important tasks
    fun getUrgentAndImportantTasks(): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getAllTasks().filter {
            isUrgent(it.ddl) && it.importance == 3 // Urgent and most important
        }
        emit(tasks)
    }

    // Fetch urgent but not important tasks
    fun getUrgentButNotImportantTasks(): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getAllTasks().filter {
            isUrgent(it.ddl) && it.importance < 3 // Urgent but less important
        }
        emit(tasks)
    }

    // Fetch important but not urgent tasks
    fun getImportantButNotUrgentTasks(): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getAllTasks().filter {
            !isUrgent(it.ddl) && it.importance == 3 // Important but not urgent
        }
        emit(tasks)
    }

    // Fetch neither urgent nor important tasks
    fun getNeitherUrgentNorImportantTasks(): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getAllTasks().filter {
            !isUrgent(it.ddl) && it.importance < 3 // Neither urgent nor important
        }
        emit(tasks)
    }
}
